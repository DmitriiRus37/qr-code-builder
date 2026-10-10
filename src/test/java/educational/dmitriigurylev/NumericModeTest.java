package educational.dmitriigurylev;

import educational.dmitriigurylev.custom_exceptions.InsuffiecientQrLengthToEncode;
import educational.dmitriigurylev.custom_exceptions.InvalidInputFormatException;
import educational.dmitriigurylev.encoders.IntegerEncoder;
import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.Version;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

/** Numeric mode (Step 6): the group width follows the digit count, so groups with leading zeros keep their width. */
class NumericModeTest {

    private static String[] encode(Object input) {
        return new IntegerEncoder().setValueAndVersion(input, Version.V_1).transformToBinaryArray();
    }

    @Test
    void zeroGroupTakesTenBits() {
        // "000" used to be stripped to "" and crash with NumberFormatException
        assertArrayEquals(new String[]{"0001", "0000000011", "0000000000"}, encode("000"));
    }

    @Test
    void groupWithLeadingZerosKeepsItsWidth() {
        // "1000012" = 100 | 001 | 2: the middle group used to shrink to 4 bits
        assertArrayEquals(new String[]{"0001", "0000000111", "0001100100", "0000000001", "0010"}, encode("1000012"));
        assertArrayEquals(new String[]{"0001", "0000000011", "0000000111"}, encode("007"));
    }

    @Test
    void shortTrailingGroupsKeepTheirWidth() {
        assertArrayEquals(new String[]{"0001", "0000000001", "0000"}, encode("0"));
        assertArrayEquals(new String[]{"0001", "0000000010", "0000101"}, encode("05"));
        assertArrayEquals(new String[]{"0001", "0000000101", "0001111011", "0000000"}, encode("12300"));
    }

    @Test
    void integerAndLongInputAreTheirDecimalDigits() {
        assertArrayEquals(encode("1000000"), encode(1000000));
        assertArrayEquals(encode("1000000"), encode(1000000L));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-5", "12a", "", " 1", "1 2", "+1", "１２"})
    void nonDigitStringIsRejected(String input) {
        assertThrowsExactly(InvalidInputFormatException.class, () -> new IntegerEncoder().setValueAndVersion(input, Version.V_1));
    }

    @Test
    void negativeNumberAndUnsupportedTypeAreRejectedByTheEncoder() {
        assertThrowsExactly(InvalidInputFormatException.class, () -> new IntegerEncoder().setValueAndVersion(-5, Version.V_1));
        assertThrowsExactly(InvalidInputFormatException.class, () -> new IntegerEncoder().setValueAndVersion(-5L, Version.V_1));
        assertThrowsExactly(InvalidInputFormatException.class, () -> new IntegerEncoder().setValueAndVersion(1.5, Version.V_1));
    }

    @Test
    void negativeNumberIsRoutedToAlphanumeric() throws Exception {
        for (Object input : List.of(-5, -5L)) {
            Cell[][] field = new QrCreator(Version.V_1, CorrectionLevel.HIGH, input).createQr().getQrCodeField().getField();
            assertEquals("-5", QrTestSupport.decode(field));
        }
        // same matrix as the alphanumeric string "-5"
        assertEquals(
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, "-5").createQr().getQrCodeField().getField()),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, -5).createQr().getQrCodeField().getField()));
    }

    @Test
    void seventeenDigitsFitV1HighAndEighteenThrow() throws Exception {
        String fits = "0".repeat(17);
        Cell[][] field = new QrCreator(Version.V_1, CorrectionLevel.HIGH, fits).createQr().getQrCodeField().getField();
        assertEquals(fits, QrTestSupport.decode(field));

        assertThrowsExactly(InsuffiecientQrLengthToEncode.class,
                () -> new QrCreator(Version.V_1, CorrectionLevel.HIGH, "0".repeat(18)).createQr());
    }

    // Done-when inputs plus a 16-digit Long, on every version/level that decodes today (V7+ wait for Steps 7-8).
    // 1000000 and "1000012" are left out: DecodeRoundTripTest already runs both on every combination.
    static Stream<Arguments> doneWhenCases() {
        List<Object> inputs = List.of("007", "000", "0", 9007199254740993L);
        return Stream.of(Version.values()).filter(v -> v.getNumber() <= 6).flatMap(v ->
                Stream.of(CorrectionLevel.values()).flatMap(l ->
                        inputs.stream().map(in -> Arguments.of(v, l, in))));
    }

    @ParameterizedTest(name = "{0} {1} {2}")
    @MethodSource("doneWhenCases")
    void numbersWithZeroGroupsRoundTrip(Version v, CorrectionLevel l, Object input) throws Exception {
        Cell[][] field = new QrCreator(v, l, input).createQr().getQrCodeField().getField();
        assertEquals(String.valueOf(input), QrTestSupport.decode(field));
    }
}
