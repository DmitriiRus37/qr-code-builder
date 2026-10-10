package educational.dmitriigurylev;

import com.google.zxing.WriterException;
import com.google.zxing.qrcode.encoder.QRCode;
import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.EncodingWay;
import educational.dmitriigurylev.enums.Version;
import educational.dmitriigurylev.utility_maps.CharacterCountIndicatorMap;
import educational.dmitriigurylev.utility_maps.InformationBitSizeMap;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Mask selection against ZXing's encoder, which picks its mask with its own implementation of the spec's penalty
 * rules ({@code MaskUtil}). For the same payload, version and level, our chosen mask and our whole matrix must be
 * identical to ZXing's.
 */
class MaskSelectionTest {

    private static final String NUMERIC = "0123456789";
    private static final String ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:";
    private static final String ASCII = "abcdefghijklmnopqrstuvwxyz0123456789 !?#&=_~";
    private static final String NON_LATIN = "абвгдежзиклмнопрстуфхцчшщэюяäöüß€";
    private static final int PAYLOADS_PER_MODE = 3;

    /**
     * For every version x level: {@value #PAYLOADS_PER_MODE} random payloads per kind (numeric, alphanumeric, ASCII
     * bytes, UTF-8 bytes), with a random length up to the capacity. Seeded, so the cases are stable.
     */
    static Stream<Arguments> randomPayloads() {
        Random random = new Random(9);
        List<Arguments> cases = new ArrayList<>();
        for (Version v : Version.values()) {
            for (CorrectionLevel l : CorrectionLevel.values()) {
                for (int i = 0; i < PAYLOADS_PER_MODE; i++) {
                    cases.add(Arguments.of(v, l, randomText(random, NUMERIC, capacity(v, l, EncodingWay.NUMERIC))));
                    cases.add(Arguments.of(v, l, randomText(random, ALPHANUMERIC, capacity(v, l, EncodingWay.ALPHANUMERIC))));
                    // a leading lower-case letter keeps it in byte mode
                    cases.add(Arguments.of(v, l, "a" + randomText(random, ASCII, capacity(v, l, EncodingWay.BYTE) - 1)));
                    // 2 UTF-8 bytes per character (3 for '€'), so a third of the byte capacity always fits
                    cases.add(Arguments.of(v, l, randomText(random, NON_LATIN, capacity(v, l, EncodingWay.BYTE) / 3)));
                }
            }
        }
        return cases.stream();
    }

    private static String randomText(Random random, String alphabet, int maxLength) {
        int length = 1 + random.nextInt(maxLength);
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    /** Characters that fit: data bits minus the 4-bit mode indicator and the character count field. */
    private static int capacity(Version v, CorrectionLevel l, EncodingWay mode) {
        int bits = InformationBitSizeMap.getInformationBitsSizeByVersionAndCorrectionLevel(v, l) - 4
                - CharacterCountIndicatorMap.getCharacterCountIndicatorByVersionAndEncodingWay(v, mode);
        return switch (mode) {
            case NUMERIC -> bits / 10 * 3 + (bits % 10 >= 7 ? 2 : bits % 10 >= 4 ? 1 : 0);
            case ALPHANUMERIC -> bits / 11 * 2 + (bits % 11 >= 6 ? 1 : 0);
            case BYTE -> bits / 8;
        };
    }

    @ParameterizedTest(name = "{0} {1} \"{2}\"")
    @MethodSource("randomPayloads")
    void chosenMaskAndMatrixEqualZxing(Version v, CorrectionLevel l, String payload) throws WriterException {
        QrCreator creator = new QrCreator(v, l, payload).createQr();
        QRCode expected = QrTestSupport.zxingEncode(payload, v, l, null);

        assertEquals(expected.getMaskPattern(), creator.getMaskPattern().getNumber(), "mask");
        assertTrue(QrTestSupport.sameMatrix(expected.getMatrix(), creator.getQrCodeField().getField()), "matrix");
    }

    /** One payload per version x level x mask; the payload rotates through the three modes. */
    static Stream<Arguments> forcedMasks() {
        List<String> payloads = List.of("HELLO", "1000012", "hello");
        List<Arguments> cases = new ArrayList<>();
        for (Version v : Version.values()) {
            for (CorrectionLevel l : CorrectionLevel.values()) {
                for (int mask = 0; mask < 8; mask++) {
                    cases.add(Arguments.of(v, l, payloads.get(cases.size() % payloads.size()), mask));
                }
            }
        }
        return cases.stream();
    }

    /**
     * Every forced mask gives ZXing's matrix for that mask, and its bits decode. Decoding uses PURE_BARCODE on
     * purpose: a forced mask is not the lowest-penalty one, and about 1 % of such symbols defeat ZXing's
     * finder-pattern detector (as they do when ZXing encodes them itself), which says nothing about the bits.
     */
    @ParameterizedTest(name = "{0} {1} \"{2}\" mask {3}")
    @MethodSource("forcedMasks")
    void everyForcedMaskEqualsZxingAndDecodes(Version v, CorrectionLevel l, String payload, int mask) throws Exception {
        QrCreator creator = new QrCreator(v, l, payload).forceMask(mask).createQr();
        Cell[][] field = creator.getQrCodeField().getField();

        assertEquals(mask, creator.getMaskPattern().getNumber());
        assertTrue(QrTestSupport.sameMatrix(QrTestSupport.zxingEncode(payload, v, l, mask).getMatrix(), field), "matrix");
        assertEquals(payload, QrTestSupport.decodePure(field));
    }
}
