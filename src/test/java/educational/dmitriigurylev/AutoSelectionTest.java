package educational.dmitriigurylev;

import educational.dmitriigurylev.custom_exceptions.InsuffiecientQrLengthToEncode;
import educational.dmitriigurylev.custom_exceptions.InvalidInputFormatException;
import educational.dmitriigurylev.encoders.ByteEncoder;
import educational.dmitriigurylev.encoders.IntLetterEncoder;
import educational.dmitriigurylev.encoders.IntegerEncoder;
import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.EncodingWay;
import educational.dmitriigurylev.enums.Version;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

/** Step 10: automatic mode and version selection, upfront capacity check, {@code QrCreator.of(...)} API. */
class AutoSelectionTest {

    private static final String ALPHANUMERIC_SET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:";

    /**
     * Character capacity per version (plan §2 table), in the order L (N, A, B), M (N, A, B), Q (N, A, B), H (N, A, B).
     * Written down independently of the code under test.
     */
    private static final Map<Version, int[]> CAPACITY = Map.of(
            Version.V_1, new int[]{41, 25, 17, 34, 20, 14, 27, 16, 11, 17, 10, 7},
            Version.V_2, new int[]{77, 47, 32, 63, 38, 26, 48, 29, 20, 34, 20, 14},
            Version.V_3, new int[]{127, 77, 53, 101, 61, 42, 77, 47, 32, 58, 35, 24},
            Version.V_4, new int[]{187, 114, 78, 149, 90, 62, 111, 67, 46, 82, 50, 34},
            Version.V_5, new int[]{255, 154, 106, 202, 122, 84, 144, 87, 60, 106, 64, 44},
            Version.V_6, new int[]{322, 195, 134, 255, 154, 106, 178, 108, 74, 139, 84, 58},
            Version.V_7, new int[]{370, 224, 154, 293, 178, 122, 207, 125, 86, 154, 93, 64},
            Version.V_8, new int[]{461, 279, 192, 365, 221, 152, 259, 157, 108, 202, 122, 84},
            Version.V_9, new int[]{552, 335, 230, 432, 262, 180, 312, 189, 130, 235, 143, 98},
            Version.V_10, new int[]{652, 395, 271, 513, 311, 213, 364, 221, 151, 288, 174, 119});

    private static final List<EncodingWay> MODES = List.of(EncodingWay.DIGITS, EncodingWay.LETTERS_DIGITS, EncodingWay.BYTES);

    private static int capacity(Version v, CorrectionLevel l, EncodingWay mode) {
        int level = switch (l) {
            case LOW -> 0;
            case MIDDLE -> 1;
            case QUARTER -> 2;
            case HIGH -> 3;
        };
        return CAPACITY.get(v)[level * 3 + MODES.indexOf(mode)];
    }

    /** A payload of exactly {@code length} characters that selects {@code mode}; byte mode uses ASCII lower case. */
    private static String payload(EncodingWay mode, int length) {
        String alphabet = switch (mode) {
            case DIGITS -> "0123456789";
            case LETTERS_DIGITS -> ALPHANUMERIC_SET;
            case BYTES -> "abcdefghijklmnopqrstuvwxyz";
        };
        var sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(alphabet.charAt((i * 7) % alphabet.length()));
        }
        return sb.toString();
    }

    private static Version next(Version v) {
        return Version.values()[v.ordinal() + 1];
    }

    @Test
    void helloWorldAtMiddlePicksV1Alphanumeric() throws Exception {
        QrCreator creator = QrCreator.of("HELLO WORLD", CorrectionLevel.MIDDLE);
        assertNull(creator.getVersion(), "no version before createQr");
        QrCodeField field = creator.build();

        assertEquals(Version.V_1, creator.getVersion());
        assertEquals(EncodingWay.LETTERS_DIGITS, creator.getEncodingWay());
        // same symbol as the fixed-version form
        QrCreator fixed = new QrCreator(Version.V_1, CorrectionLevel.MIDDLE, "HELLO WORLD").createQr();
        assertEquals(FieldUtil.fieldToString(fixed.getQrCodeField().getField()), FieldUtil.fieldToString(field.getField()));
        QrTestSupport.assertRoundTrip(creator, "HELLO WORLD", "HELLO WORLD");
    }

    // literal "Done when" check of plan Step 10
    @Test
    void alphanumeric154AtLowPicksV5And155PicksV6() throws Exception {
        String fits = payload(EncodingWay.LETTERS_DIGITS, 154);
        String oneMore = payload(EncodingWay.LETTERS_DIGITS, 155);

        QrCreator first = QrCreator.of(fits, CorrectionLevel.LOW).createQr();
        QrCreator second = QrCreator.of(oneMore, CorrectionLevel.LOW).createQr();

        assertEquals(Version.V_5, first.getVersion());
        assertEquals(Version.V_6, second.getVersion());
        QrTestSupport.assertRoundTrip(first, fits, fits);
        QrTestSupport.assertRoundTrip(second, oneMore, oneMore);
    }

    static Stream<Arguments> boundaries() {
        List<Arguments> cases = new ArrayList<>();
        for (Version v : Version.values()) {
            for (CorrectionLevel l : CorrectionLevel.values()) {
                for (EncodingWay mode : MODES) {
                    cases.add(Arguments.of(v, l, mode));
                }
            }
        }
        return cases.stream();
    }

    /** For every version × level × mode: the maximum payload picks that version, one more character the next one. */
    @ParameterizedTest(name = "{0} {1} {2}")
    @MethodSource("boundaries")
    void capacityBoundaryPicksVersion(Version v, CorrectionLevel l, EncodingWay mode) {
        int max = capacity(v, l, mode);
        QrCreator fits = QrCreator.of(payload(mode, max), l).createQr();
        assertEquals(v, fits.getVersion());
        assertEquals(mode, fits.getEncodingWay());

        if (v == Version.V_10) {
            assertThrowsExactly(InsuffiecientQrLengthToEncode.class, () -> QrCreator.of(payload(mode, max + 1), l).createQr());
        } else {
            assertEquals(next(v), QrCreator.of(payload(mode, max + 1), l).createQr().getVersion());
        }
    }

    static Stream<Arguments> roundTripBoundaries() {
        return Stream.of(
                Arguments.of(Version.V_1, CorrectionLevel.LOW, EncodingWay.DIGITS),
                Arguments.of(Version.V_3, CorrectionLevel.HIGH, EncodingWay.DIGITS),
                Arguments.of(Version.V_9, CorrectionLevel.MIDDLE, EncodingWay.DIGITS),
                Arguments.of(Version.V_2, CorrectionLevel.QUARTER, EncodingWay.LETTERS_DIGITS),
                Arguments.of(Version.V_8, CorrectionLevel.HIGH, EncodingWay.LETTERS_DIGITS),
                Arguments.of(Version.V_1, CorrectionLevel.HIGH, EncodingWay.BYTES),
                Arguments.of(Version.V_6, CorrectionLevel.MIDDLE, EncodingWay.BYTES),
                // V9 -> V10 also crosses the character count width change (8 -> 16 bits for bytes)
                Arguments.of(Version.V_9, CorrectionLevel.LOW, EncodingWay.BYTES));
    }

    /** The boundary payloads are real symbols: both sides of the boundary decode. */
    @ParameterizedTest(name = "{0} {1} {2}")
    @MethodSource("roundTripBoundaries")
    void boundaryPayloadsRoundTrip(Version v, CorrectionLevel l, EncodingWay mode) throws Exception {
        String fits = payload(mode, capacity(v, l, mode));
        String oneMore = payload(mode, capacity(v, l, mode) + 1);
        QrCreator first = QrCreator.of(fits, l).createQr();
        QrCreator second = QrCreator.of(oneMore, l).createQr();
        assertEquals(v, first.getVersion());
        assertEquals(next(v), second.getVersion());
        QrTestSupport.assertRoundTrip(first, fits, fits);
        QrTestSupport.assertRoundTrip(second, oneMore, oneMore);
    }

    @Test
    void largestPayloadPerModeRoundTripsOnV10Low() throws Exception {
        for (EncodingWay mode : MODES) {
            String max = payload(mode, capacity(Version.V_10, CorrectionLevel.LOW, mode));
            QrCreator creator = QrCreator.of(max, CorrectionLevel.LOW).createQr();
            assertEquals(Version.V_10, creator.getVersion());
            QrTestSupport.assertRoundTrip(creator, max, max);
        }
    }

    static Stream<Arguments> modeCases() {
        return Stream.of(
                Arguments.of("0123456789", EncodingWay.DIGITS),
                Arguments.of("007", EncodingWay.DIGITS),
                Arguments.of("HELLO WORLD $%*+-./:", EncodingWay.LETTERS_DIGITS),
                Arguments.of("12A", EncodingWay.LETTERS_DIGITS),
                Arguments.of("-5", EncodingWay.LETTERS_DIGITS),
                Arguments.of(" ", EncodingWay.LETTERS_DIGITS),
                Arguments.of("hello", EncodingWay.BYTES),
                Arguments.of("Hello", EncodingWay.BYTES),
                Arguments.of("HELLO!", EncodingWay.BYTES),
                Arguments.of("Хаб", EncodingWay.BYTES),
                Arguments.of("１２", EncodingWay.BYTES), // full-width digits are not ASCII digits
                Arguments.of("https://example.com/?q=1", EncodingWay.BYTES));
    }

    @ParameterizedTest(name = "\"{0}\" -> {1}")
    @MethodSource("modeCases")
    void modeIsTheMostCompactOneCoveringEveryCharacter(String text, EncodingWay expected) throws Exception {
        QrCreator creator = QrCreator.of(text, CorrectionLevel.MIDDLE).createQr();
        assertEquals(expected, creator.getEncodingWay());
        QrTestSupport.assertRoundTrip(creator, text, text);
    }

    @Test
    void byteArrayIsAlwaysByteMode() throws Exception {
        byte[] digits = "123".getBytes(StandardCharsets.US_ASCII);
        QrCreator creator = QrCreator.of(digits, CorrectionLevel.HIGH).createQr();
        assertEquals(EncodingWay.BYTES, creator.getEncodingWay());
        assertEquals(Version.V_1, creator.getVersion());
        assertEquals("123", QrTestSupport.decodeBoofcv(creator.getQrCodeField().getField()));
    }

    @Test
    void numbersInTheFixedVersionFormFollowTheirDigits() {
        assertEquals(EncodingWay.DIGITS, new QrCreator(Version.V_1, CorrectionLevel.HIGH, 1234).getEncodingWay());
        assertEquals(EncodingWay.LETTERS_DIGITS, new QrCreator(Version.V_1, CorrectionLevel.HIGH, -5L).getEncodingWay());
    }

    /** Empty input is rejected: a symbol that carries nothing is almost certainly a caller error. */
    @Test
    void emptyInputIsRejected() {
        var e = assertThrowsExactly(InvalidInputFormatException.class, () -> QrCreator.of("", CorrectionLevel.LOW));
        assertEquals("nothing to encode: the input is empty", e.getMessage());
        assertThrowsExactly(InvalidInputFormatException.class, () -> QrCreator.of(new byte[0], CorrectionLevel.LOW));
        assertThrowsExactly(InvalidInputFormatException.class, () -> new QrCreator(Version.V_1, CorrectionLevel.LOW, ""));
    }

    @Test
    void unsupportedOrMissingInputIsRejected() {
        assertThrowsExactly(InvalidInputFormatException.class, () -> new QrCreator(Version.V_1, CorrectionLevel.LOW, 1.5));
        assertThrowsExactly(InvalidInputFormatException.class, () -> new QrCreator(Version.V_1, CorrectionLevel.LOW, null));
        assertThrowsExactly(NullPointerException.class, () -> QrCreator.of((String) null, CorrectionLevel.LOW));
        assertThrowsExactly(NullPointerException.class, () -> QrCreator.of("A", null));
        assertThrowsExactly(NullPointerException.class, () -> QrCreator.of("A", CorrectionLevel.LOW).version(null));
    }

    @Test
    void loneSurrogateIsRejectedUpfront() {
        assertThrowsExactly(InvalidInputFormatException.class, () -> QrCreator.of("a\uD83Db", CorrectionLevel.LOW));
    }

    @Test
    void overflowBeyondV10HasAMessage() {
        var alphanumeric = assertThrowsExactly(InsuffiecientQrLengthToEncode.class,
                () -> QrCreator.of(payload(EncodingWay.LETTERS_DIGITS, 396), CorrectionLevel.LOW).build());
        assertEquals("alphanumeric payload of 396 characters needs 2193 bits, V10-L holds 2192 (the largest supported version)",
                alphanumeric.getMessage());

        var bytes = assertThrowsExactly(InsuffiecientQrLengthToEncode.class,
                () -> QrCreator.of(new byte[272], CorrectionLevel.HIGH).build());
        assertEquals("byte payload of 272 bytes needs 2196 bits, V10-H holds 976 (the largest supported version)",
                bytes.getMessage());

        var numeric = assertThrowsExactly(InsuffiecientQrLengthToEncode.class,
                () -> QrCreator.of("1".repeat(289), CorrectionLevel.HIGH).build());
        assertEquals("numeric payload of 289 digits needs 980 bits, V10-H holds 976 (the largest supported version)",
                numeric.getMessage());
    }

    @Test
    void explicitVersionTooSmallHasAMessage() {
        var e = assertThrowsExactly(InsuffiecientQrLengthToEncode.class,
                () -> QrCreator.of("HELLO WORLD", CorrectionLevel.HIGH).version(Version.V_1).build());
        assertEquals("alphanumeric payload of 11 characters needs 74 bits, V1-H holds 72", e.getMessage());

        // the fixed-version constructor is checked the same way, before anything is encoded
        var old = assertThrowsExactly(InsuffiecientQrLengthToEncode.class,
                () -> new QrCreator(Version.V_1, CorrectionLevel.LOW, "a".repeat(18)).createQr());
        assertEquals("byte payload of 18 bytes needs 156 bits, V1-L holds 152", old.getMessage());
    }

    @Test
    void explicitVersionLargerThanNeededIsUsed() throws Exception {
        QrCreator creator = QrCreator.of("HELLO", CorrectionLevel.LOW).version(Version.V_7).createQr();
        assertEquals(Version.V_7, creator.getVersion());
        assertEquals("HELLO", QrTestSupport.decodeBoofcv(creator.getQrCodeField().getField()));
    }

    @Test
    void maskOverrideWorksWithAutomaticVersion() {
        QrCreator creator = QrCreator.of("HELLO WORLD", CorrectionLevel.MIDDLE).forceMask(5).createQr();
        assertEquals(5, creator.getMaskPattern().getNumber());
        assertEquals(Version.V_1, creator.getVersion());
    }

    @Test
    void versionSetAfterABuildIsHonoured() {
        QrCreator creator = QrCreator.of("HELLO", CorrectionLevel.LOW);
        creator.build();
        assertEquals(Version.V_1, creator.getVersion());
        creator.version(Version.V_3).build();
        assertEquals(Version.V_3, creator.getVersion());
    }

    @Test
    void drawBeforeBuildIsRejected() {
        var e = assertThrowsExactly(NullPointerException.class,
                () -> QrCreator.of("HELLO", CorrectionLevel.LOW).drawQr(Path.of("never-written.png")));
        assertEquals("call createQr()/build() first", e.getMessage());
    }

    @Test
    void createQrCanBeCalledAgain() {
        QrCreator creator = QrCreator.of("HELLO WORLD", CorrectionLevel.MIDDLE);
        String first = FieldUtil.fieldToString(creator.build().getField());
        assertEquals(first, FieldUtil.fieldToString(creator.build().getField()));
    }

    /** The bit counts used for version selection equal what the (stateless) encoders actually emit. */
    @Test
    void requiredBitsMatchEncoderOutput() {
        for (Version v : List.of(Version.V_1, Version.V_9, Version.V_10)) {
            for (int n = 1; n <= 12; n++) {
                assertBits(new IntegerEncoder().encode(payload(EncodingWay.DIGITS, n), v), EncodingWay.DIGITS, n, v);
                assertBits(new IntLetterEncoder().encode(payload(EncodingWay.LETTERS_DIGITS, n), v), EncodingWay.LETTERS_DIGITS, n, v);
                assertBits(new ByteEncoder().encode(new byte[n], v), EncodingWay.BYTES, n, v);
            }
        }
    }

    private static void assertBits(String[] parts, EncodingWay mode, int n, Version v) {
        int data = Arrays.stream(parts, 2, parts.length).mapToInt(String::length).sum();
        int total = Arrays.stream(parts).mapToInt(String::length).sum();
        assertEquals(data, QrCreator.dataBits(mode, n), mode + " data bits, n=" + n);
        assertEquals(total, QrCreator.requiredBits(mode, n, v), mode + " total bits, n=" + n + ", " + v);
    }
}
