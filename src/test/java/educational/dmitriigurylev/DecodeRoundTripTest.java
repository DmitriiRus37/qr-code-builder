package educational.dmitriigurylev;

import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.Version;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Generates a QR code for every version × correction level × sample and decodes it with ZXing.
 *
 * <p>Combinations that are broken today are listed in {@link #knownBroken}. They are asserted to
 * <b>still fail</b> (strict "expected failure"): when a fix makes one pass, this test turns red and
 * the matching rule must be deleted. Goal of the implementation plan: no rules left.
 */
class DecodeRoundTripTest {

    /** Every sample fits V1-H, the smallest capacity (17 digits / 10 alphanumeric / 7 bytes). */
    record Sample(String name, Object input, String expected, Mode mode) {
        @Override
        public String toString() {
            return name;
        }
    }

    enum Mode { NUMERIC, ALPHANUMERIC, BYTE }

    static final List<Sample> SAMPLES = List.of(
            new Sample("int 1234", 1234, "1234", Mode.NUMERIC),
            new Sample("int 1000000 (000 group)", 1000000, "1000000", Mode.NUMERIC),
            new Sample("\"1000012\" (0xx group)", "1000012", "1000012", Mode.NUMERIC),
            new Sample("\"HELLO\"", "HELLO", "HELLO", Mode.ALPHANUMERIC),
            new Sample("\"A1 $%*+-./\"", "A1 $%*+-./", "A1 $%*+-./", Mode.ALPHANUMERIC),
            new Sample("\"hello\"", "hello", "hello", Mode.BYTE),
            new Sample("\"Хаб\" (UTF-8)", "Хаб", "Хаб", Mode.BYTE),
            new Sample("byte[] \"Ab\"", "Ab".getBytes(StandardCharsets.UTF_8), "Ab", Mode.BYTE));

    static Stream<Arguments> cases() {
        return Stream.of(Version.values()).flatMap(v ->
                Stream.of(CorrectionLevel.values()).flatMap(l ->
                        SAMPLES.stream().map(s -> Arguments.of(v, l, s))));
    }

    /**
     * Known defects, one rule per plan step. Delete a rule in the step that fixes it.
     *
     * @return the plan step expected to fix this combination, or empty if it must decode correctly
     */
    static Optional<String> knownBroken(Version v, CorrectionLevel l, Sample s) {
        // Step 4: Reed–Solomon indexes past the generator when data codewords > EC codewords per block
        if (l == CorrectionLevel.LOW || l == CorrectionLevel.MIDDLE) {
            return Optional.of("Step 4");
        }
        // Step 5: byte mode drops leading zeros of each byte; String input -> ClassCastException
        if (s.mode() == Mode.BYTE) {
            return Optional.of("Step 5");
        }
        // Step 6: numeric mode strips leading zeros inside 3-digit groups
        if (s.expected().equals("1000000") || s.expected().equals("1000012")) {
            return Optional.of("Step 6");
        }
        // Steps 7 + 8: alignment patterns placed on the diagonal only, no version information
        if (v.ordinal() + 1 >= 7) {
            return Optional.of("Steps 7-8");
        }
        // Step 4: ABMap log(241) = 1174 typo gives wrong EC bytes; ZXing "corrects" valid data into "A1 $%*$W./"
        if (s.input().equals("A1 $%*+-./") && List.of("V2-Q", "V3-H", "V3-Q", "V5-H", "V5-Q").contains(key(v, l))) {
            return Optional.of("Step 4");
        }
        // Step 9: fixed mask 2 leaves finder-like patterns in the data area; the bits are valid
        // (ZXing decodes them with PURE_BARCODE) but the detector cannot locate the symbol
        if (s.input().equals("A1 $%*+-./") && List.of("V6-H", "V6-Q").contains(key(v, l))) {
            return Optional.of("Step 9");
        }
        return Optional.empty();
    }

    private static String key(Version v, CorrectionLevel l) {
        return "V" + (v.ordinal() + 1) + "-" + l.name().charAt(0);
    }

    @ParameterizedTest(name = "{0} {1} {2}")
    @MethodSource("cases")
    void generatedCodeDecodesToInput(Version v, CorrectionLevel l, Sample s) {
        String outcome;
        try {
            Cell[][] field = new QrCreator(v, l, s.input()).createQr().getQrCodeField().getField();
            outcome = QrTestSupport.decode(field);
        } catch (Exception e) {
            outcome = null;
            if (knownBroken(v, l, s).isEmpty()) {
                fail("expected a readable code, got " + e, e);
            }
        }

        Optional<String> broken = knownBroken(v, l, s);
        if (broken.isPresent()) {
            if (s.expected().equals(outcome)) {
                fail("Now decodes correctly - remove the '" + broken.get() + "' rule from knownBroken()");
            }
        } else {
            assertEquals(s.expected(), outcome);
        }
    }
}
