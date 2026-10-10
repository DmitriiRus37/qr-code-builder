package educational.dmitriigurylev;

import educational.dmitriigurylev.custom_exceptions.InsuffiecientQrLengthToEncode;
import educational.dmitriigurylev.custom_exceptions.InvalidInputFormatException;
import educational.dmitriigurylev.encoders.ByteEncoder;
import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.Version;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

/** Byte mode (Step 5): 8 bits per byte, String input as UTF-8, count = number of bytes. */
class ByteModeTest {

    private static final String URL = "https://example.com/?q=1"; // 24 bytes

    /** Byte capacity per version, levels in L / M / Q / H order (plan §2 character table). */
    private static final Map<Version, int[]> BYTE_CAPACITY = Map.of(
            Version.V_1, new int[]{17, 14, 11, 7},
            Version.V_2, new int[]{32, 26, 20, 14},
            Version.V_3, new int[]{53, 42, 32, 24},
            Version.V_4, new int[]{78, 62, 46, 34},
            Version.V_5, new int[]{106, 84, 60, 44},
            Version.V_6, new int[]{134, 106, 74, 58});

    private static int byteCapacity(Version v, CorrectionLevel l) {
        int index = switch (l) {
            case LOW -> 0;
            case MIDDLE -> 1;
            case QUARTER -> 2;
            case HIGH -> 3;
        };
        return BYTE_CAPACITY.get(v)[index];
    }

    @Test
    void bytesAreLeftPaddedToEightBits() {
        // 'A' = 0x41, 'b' = 0x62: both below 0x80, so the old code dropped their leading zero
        String[] bits = new ByteEncoder().encode("Ab".getBytes(StandardCharsets.US_ASCII), Version.V_1);
        assertArrayEquals(new String[]{"0100", "00000010", "0100000101100010"}, bits);
    }

    @Test
    void rawBytesAtOrAbove0x80AreEncodedUnsigned() {
        String[] bits = new ByteEncoder().encode(new byte[]{(byte) 0xFF, (byte) 0x80}, Version.V_1);
        assertArrayEquals(new String[]{"0100", "00000010", "1111111110000000"}, bits);
    }

    @Test
    void byteArrayInputIsCopied() {
        // the encoder is stateless now, so the copy is taken where the input is kept: in QrCreator
        byte[] input = {0x41};
        var creator = QrCreator.of(input, CorrectionLevel.LOW);
        input[0] = 0x42;
        assertEquals("A", QrTestSupport.decodeBoofcv(creator.build().getField()));
    }

    @Test
    void loneSurrogateIsRejected() {
        assertThrowsExactly(InvalidInputFormatException.class,
                () -> new ByteEncoder().encode("a\uD83Db", Version.V_1));
    }

    @Test
    void countLargerThanCountFieldIsACapacityError() {
        // 300 bytes need more than the 8-bit count field of V1-V9
        assertThrowsExactly(InsuffiecientQrLengthToEncode.class,
                () -> new QrCreator(Version.V_1, CorrectionLevel.LOW, "a".repeat(300)).createQr());
        assertThrowsExactly(InsuffiecientQrLengthToEncode.class,
                () -> new QrCreator(Version.V_9, CorrectionLevel.LOW, "a".repeat(300)).createQr());
    }

    @Test
    void stringIsEncodedAsUtf8AndCountIsBytes() {
        // "Хаб" = D0 A5 D0 B0 D0 B1: 3 chars, 6 bytes
        String[] bits = new ByteEncoder().encode("Хаб", Version.V_1);
        assertEquals("00000110", bits[1]);
        assertEquals("110100001010010111010000101100001101000010110001", bits[2]);
    }

    @Test
    void characterCountIsSixteenBitsFromV10() {
        String[] bits = new ByteEncoder().encode("hello", Version.V_10);
        assertEquals("0000000000000101", bits[1]);
    }

    @Test
    void unsupportedInputTypeIsRejected() {
        assertThrowsExactly(InvalidInputFormatException.class, () -> new ByteEncoder().encode(1.5, Version.V_1));
    }

    // literal "Done when" check of plan Step 5
    @Test
    void seventeenBytesFitV1LowAndEighteenThrow() throws Exception {
        String fits = "a".repeat(17);
        QrTestSupport.assertRoundTrip(Version.V_1, CorrectionLevel.LOW, fits, fits);

        assertThrowsExactly(InsuffiecientQrLengthToEncode.class,
                () -> new QrCreator(Version.V_1, CorrectionLevel.LOW, "a".repeat(18)).createQr());
    }

    static Stream<Arguments> v1ToV6() {
        return BYTE_CAPACITY.keySet().stream().sorted().flatMap(v ->
                Stream.of(CorrectionLevel.values()).map(l -> Arguments.of(v, l)));
    }

    /** The URL needs 24 bytes: it must decode where it fits and throw where it does not. */
    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("v1ToV6")
    void urlRoundTripsWhereItFits(Version v, CorrectionLevel l) throws Exception {
        int length = URL.getBytes(StandardCharsets.UTF_8).length;
        if (length <= byteCapacity(v, l)) {
            QrTestSupport.assertRoundTrip(v, l, URL, URL);
        } else {
            assertThrowsExactly(InsuffiecientQrLengthToEncode.class, () -> new QrCreator(v, l, URL).createQr());
        }
    }

    /** Each version's capacity boundary: the maximum byte count decodes, one more byte throws. */
    @ParameterizedTest(name = "{0} {1}")
    @MethodSource("v1ToV6")
    void maximumPayloadFitsAndOneMoreThrows(Version v, CorrectionLevel l) throws Exception {
        String max = "x".repeat(byteCapacity(v, l));
        QrTestSupport.assertRoundTrip(v, l, max, max);

        assertThrowsExactly(InsuffiecientQrLengthToEncode.class, () -> new QrCreator(v, l, max + "x").createQr());
    }
}
