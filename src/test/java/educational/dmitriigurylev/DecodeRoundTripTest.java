package educational.dmitriigurylev;

import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.Version;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;


/**
 * Generates a QR code for every version × correction level × sample (plus a list of regression cases)
 * and decodes it with BoofCV and ZXing.
 *
 * <p>The former {@code knownBroken()} rules (one per plan step) are gone: since Step 9 the encoder has no known
 * defect. The only exceptions left are correct symbols that ZXing's detector cannot locate,
 * {@link QrTestSupport#UNDETECTABLE_BY_ZXING}; they must still fail ZXing (strict expected failure) and must
 * decode with BoofCV.
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

    /**
     * Payloads that only fit larger versions, each tied to one version and level. All of them failed ZXing's
     * normal detection under the old fixed mask 2 (plan Steps 6 and 8); the lowest-penalty mask fixes all but one.
     */
    static final List<Arguments> REGRESSIONS = List.of(
            Arguments.of(Version.V_2, CorrectionLevel.HIGH,
                    numeric("0".repeat(33) + "7")),
            Arguments.of(Version.V_3, CorrectionLevel.MEDIUM,
                    numeric("078161009481839")),
            Arguments.of(Version.V_6, CorrectionLevel.QUARTILE,
                    numeric("5074145457")),
            // the rest come from a seeded sweep of random payloads (java.util.Random(42)) on all versions x levels
            Arguments.of(Version.V_7, CorrectionLevel.QUARTILE,
                    alphanumeric("0%$R.2CV++4$S3I/O XJJ8E-P4Y- %GLMFVOYQFESVY7JXXH8/IZVT3$F2")),
            Arguments.of(Version.V_8, CorrectionLevel.HIGH,
                    alphanumeric("N%9ZXB .*GI3/AJ7:DJT:LI1XAKY.HT5HCCKFAP/FB*- WDV0 JPGK78QVKL8A78*Q5$2D+%S")),
            Arguments.of(Version.V_8, CorrectionLevel.HIGH,
                    bytes("a0tncr#g4pgiyt82oban&_1!4oju2wp06")),
            Arguments.of(Version.V_8, CorrectionLevel.QUARTILE,
                    bytes("apny5q=yugc9urgf627rr#ox!me&lg2y#~xq73quqwppi")),
            Arguments.of(Version.V_9, CorrectionLevel.HIGH,
                    numeric("55258294937699303991991207")));

    private static Sample numeric(String text) {
        return new Sample("\"" + text + "\"", text, text, Mode.NUMERIC);
    }

    private static Sample alphanumeric(String text) {
        return new Sample("\"" + text + "\"", text, text, Mode.ALPHANUMERIC);
    }

    private static Sample bytes(String text) {
        return new Sample("\"" + text + "\"", text, text, Mode.BYTE);
    }

    static Stream<Arguments> cases() {
        Stream<Arguments> allCombinations = Stream.of(Version.values()).flatMap(v ->
                Stream.of(CorrectionLevel.values()).flatMap(l ->
                        SAMPLES.stream().map(s -> Arguments.of(v, l, s))));
        return Stream.concat(allCombinations, REGRESSIONS.stream());
    }

    /**
     * Every case must decode to the input with BoofCV and with ZXing. Correct symbols that ZXing's detector cannot
     * locate are listed in {@link QrTestSupport#UNDETECTABLE_BY_ZXING} and get stricter checks instead.
     */
    @ParameterizedTest(name = "{0} {1} {2}")
    @MethodSource("cases")
    void generatedCodeDecodesToInput(Version v, CorrectionLevel l, Sample s) throws Exception {
        QrTestSupport.assertRoundTrip(v, l, s.input(), s.expected());
    }

    /**
     * Every list entry is checked on its own, even if no other test produces it: a stale entry (ZXing reads it now)
     * or a typo (an entry for a symbol ZXing does read) fails here.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("educational.dmitriigurylev.QrTestSupport#undetectableByZxing")
    void undetectableByZxingEntriesStayStrict(QrTestSupport.UndetectableByZxing entry) throws Exception {
        QrTestSupport.assertOnlyZxingDetectorFails(entry.version(), entry.level(), entry.text(), entry.text());
    }
}
