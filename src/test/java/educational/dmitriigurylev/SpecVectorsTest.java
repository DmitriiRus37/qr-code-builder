package educational.dmitriigurylev;

import educational.dmitriigurylev.encoders.IntLetterEncoder;
import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.Version;
import educational.dmitriigurylev.reed_solomon_mapping.GaloisField;
import educational.dmitriigurylev.utility_maps.BlocksCountMap;
import educational.dmitriigurylev.utility_maps.CorrectionBytesPerBlockMap;
import educational.dmitriigurylev.utility_maps.CorrectionLevelAndMaskCodeMap;
import educational.dmitriigurylev.utility_maps.GeneratingPolynomialMap;
import educational.dmitriigurylev.utility_maps.InformationBitSizeMap;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Checks the hand-typed spec tables and arithmetic against values derived from first principles
 * (ISO/IEC 18004, GF(256) with primitive polynomial 0x11D) and the published thonky.com example.
 */
class SpecVectorsTest {

    private static final int PRIMITIVE = 0x11D;

    /** Reference exp table built from the field definition, independent of GaloisField. */
    private static int[] referenceExp() {
        int[] exp = new int[256];
        for (int i = 0, x = 1; i < 256; i++) {
            exp[i] = x;
            x <<= 1;
            if (x > 255) {
                x ^= PRIMITIVE;
            }
        }
        return exp;
    }

    @Test
    void antilogTableMatchesFieldDefinition() {
        int[] exp = referenceExp();
        for (int i = 0; i < 256; i++) {
            assertEquals(exp[i], GaloisField.exp(i), "exp(" + i + ")");
        }
    }

    @Test
    void logTableIsInverseOfAntilog() {
        int[] exp = referenceExp();
        for (int i = 0; i < 255; i++) {
            assertEquals(i, GaloisField.log(exp[i]), "log(" + exp[i] + ")");
        }
    }

    @Test
    void generatorPolynomialsMatchProductOfRoots() {
        int[] exp = referenceExp();
        int[] log = new int[256];
        for (int i = 0; i < 255; i++) {
            log[exp[i]] = i;
        }
        for (int n : new int[]{7, 10, 13, 15, 16, 17, 18, 20, 22, 24, 26, 28, 30}) {
            // g(x) = (x - α^0)(x - α^1)...(x - α^(n-1)), coefficients highest degree first
            int[] g = {1};
            for (int i = 0; i < n; i++) {
                int[] next = new int[g.length + 1];
                for (int j = 0; j < g.length; j++) {
                    next[j] ^= g[j];
                    next[j + 1] ^= g[j] == 0 ? 0 : exp[(log[g[j]] + i) % 255];
                }
                g = next;
            }
            int[] expectedExponents = new int[n];
            for (int j = 0; j < n; j++) {
                expectedExponents[j] = log[g[j + 1]]; // leading α^0 is omitted in the table
            }
            assertArrayEquals(expectedExponents, GeneratingPolynomialMap.getGeneratingPolynomial(n), "generator " + n);
        }
    }

    /** thonky.com worked example: "HELLO WORLD", version 1, level M. */
    @Test
    void helloWorld1MDataCodewords() {
        String[] parts = new IntLetterEncoder().setValueAndVersion("HELLO WORLD", Version.V_1).transformToBinaryArray();
        int maxBits = InformationBitSizeMap.getInformationBitsSizeByVersionAndCorrectionLevel(Version.V_1, CorrectionLevel.MIDDLE);
        StringBuilder bits = UtilityMethods.addTerminator(UtilityMethods.binaryArrayToBitString(parts), maxBits);
        int[] codewords = UtilityMethods.addRotationalBytes(
                UtilityMethods.binaryStringToDecimalArray(UtilityMethods.addLagZeros(bits)), maxBits / 8);

        assertArrayEquals(new int[]{32, 91, 11, 120, 209, 114, 220, 77, 67, 64, 236, 17, 236, 17, 236, 17}, codewords);
    }

    @Test
    void helloWorld1MErrorCorrectionCodewords() {
        int[] data = {32, 91, 11, 120, 209, 114, 220, 77, 67, 64, 236, 17, 236, 17, 236, 17};
        assertArrayEquals(new int[]{196, 35, 39, 119, 235, 215, 231, 226, 93, 23},
                UtilityMethods.getCorrectionBytes(data, 10));
    }

    @Test
    void formatStringsAreBchEncoded() {
        Map<CorrectionLevel, Integer> ecBits = Map.of(
                CorrectionLevel.LOW, 0b01, CorrectionLevel.MIDDLE, 0b00,
                CorrectionLevel.QUARTER, 0b11, CorrectionLevel.HIGH, 0b10);
        for (CorrectionLevel level : CorrectionLevel.values()) {
            for (int mask = 0; mask < 8; mask++) {
                int data = ecBits.get(level) << 3 | mask;
                int rem = data;
                for (int i = 0; i < 10; i++) {
                    rem = (rem << 1) ^ ((rem >>> 9) * 0x537);
                }
                int bits = (data << 10 | rem) ^ 0x5412;
                String expected = String.format("%15s", Integer.toBinaryString(bits)).replace(' ', '0');
                assertEquals(expected, CorrectionLevelAndMaskCodeMap.getMaskAndCorrectionLevelCode(level, (byte) mask),
                        level + " mask " + mask);
            }
        }
    }

    /** data codewords + blocks × EC codewords per block must equal the codewords the symbol can hold. */
    @Test
    void capacityTablesAddUpToSymbolSize() {
        for (Version v : Version.values()) {
            int total = rawDataModules(v.getNumber()) / 8;
            for (CorrectionLevel l : CorrectionLevel.values()) {
                int data = InformationBitSizeMap.getInformationBitsSizeByVersionAndCorrectionLevel(v, l) / 8;
                int ec = BlocksCountMap.getBlocksCountByVersionAndCorrectionLevel(v, l)
                        * CorrectionBytesPerBlockMap.getCorrectionBytesSizeByVersionAndCorrectionLevel(v, l);
                assertEquals(total, data + ec, v + " " + l);
            }
        }
    }

    /** Modules left for data + EC after all function patterns, format and version info. */
    private static int rawDataModules(int version) {
        int result = (16 * version + 128) * version + 64;
        if (version >= 2) {
            int alignCount = version / 7 + 2;
            result -= (25 * alignCount - 10) * alignCount - 55;
            if (version >= 7) {
                result -= 36;
            }
        }
        return result;
    }
}
