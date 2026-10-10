package educational.dmitriigurylev;

import educational.dmitriigurylev.QrImageDrawer.Format;
import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.Version;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@link CliOptions#parse}: no side effects, so every case is a plain unit test. */
class CliOptionsTest {

    @TempDir
    Path tempDir;

    private static CliOptions parse(String... args) {
        return CliOptions.parse(args);
    }

    private static String usageError(String... args) {
        return assertThrows(CliOptions.UsageException.class, () -> CliOptions.parse(args)).getMessage();
    }

    @Test
    void defaults() {
        CliOptions o = parse("HELLO WORLD");
        assertEquals("HELLO WORLD", o.text());
        assertEquals(CorrectionLevel.MEDIUM, o.level());
        assertNull(o.version());
        assertNull(o.mask());
        assertEquals(QrImageDrawer.DEFAULT_SCALE, o.scale());
        assertEquals(QrImageDrawer.DEFAULT_QUIET_ZONE, o.quietZone());
        assertEquals(Path.of("qr.png"), o.output());
        assertEquals(Format.PNG, o.format());
        assertFalse(o.debug());
        assertFalse(o.help());
    }

    @Test
    void everyOptionTogether() {
        CliOptions o = parse("-l", "Q", "-v", "7", "-m", "3", "-s", "4", "-q", "6", "-o", "out/x.svg", "--debug",
                "https://example.com", "--svg");
        assertEquals(new CliOptions("https://example.com", CorrectionLevel.QUARTILE, Version.V_7, 3, 4, 6,
                Path.of("out/x.svg"), Format.SVG, true, false), o);
    }

    @ParameterizedTest
    @CsvSource({"L, LOW", "M, MEDIUM", "Q, QUARTILE", "H, HIGH", "l, LOW", "h, HIGH"})
    void levels(String letter, CorrectionLevel level) {
        assertEquals(level, parse("X", "-l", letter).level());
        assertEquals(letter.toUpperCase(Locale.ROOT), level.letter());
    }

    @Test
    void everyVersionAndMask() {
        for (Version v : Version.values()) {
            assertEquals(v, parse("X", "-v", String.valueOf(v.getNumber())).version());
        }
        for (int m = 0; m <= 7; m++) {
            assertEquals(m, parse("X", "-m", String.valueOf(m)).mask());
        }
    }

    @Test
    void scaleAndQuietZoneBounds() {
        assertEquals(1, parse("X", "-s", "1").scale());
        assertEquals(QrImageDrawer.MAX_SCALE, parse("X", "-s", "50").scale());
        assertEquals(0, parse("X", "-q", "0").quietZone());
        assertEquals(QrImageDrawer.MAX_QUIET_ZONE, parse("X", "-q", "40").quietZone());
    }

    @Test
    void outputAndFormat() {
        assertEquals(Path.of("qr.svg"), parse("X", "--svg").output());
        assertEquals(Format.SVG, parse("X", "--svg").format());
        // the extension wins, in any case
        assertEquals(Format.SVG, parse("X", "-o", "a/b.SVG").format());
        assertEquals(Path.of("a/b.SVG"), parse("X", "-o", "a/b.SVG").output());
        assertEquals(Format.PNG, parse("X", "-o", "b.Png").format());
        // --svg with an .svg file is redundant, not a conflict
        assertEquals(Format.SVG, parse("X", "--svg", "-o", "b.svg").format());
        // no extension: the format's extension is appended
        assertEquals(Path.of("out/qr.png"), parse("X", "-o", "out/qr").output());
        assertEquals(Path.of("out/qr.svg"), parse("X", "-o", "out/qr", "--svg").output());
        // option values are never options, even when they look like one
        assertEquals(Path.of("-h.png"), parse("X", "-o", "-h").output());
        assertEquals(Path.of("-d.png"), parse("X", "-o", "-d.png").output());
        assertEquals(Path.of("--.png"), parse("X", "-o", "--").output());
    }

    @Test
    void outputNamingADirectory() throws Exception {
        // a trailing separator means a directory (created later by Main if missing)
        assertEquals(Path.of("out/qr.png"), parse("X", "-o", "out/").output());
        assertEquals(Path.of("out/qr.svg"), parse("X", "-o", "out/", "--svg").output());
        assertEquals(Path.of("/qr.png"), parse("X", "-o", "/").output());
        // an existing directory, even one whose name has a dot
        assertEquals(tempDir.resolve("qr.png"), parse("X", "-o", tempDir.toString()).output());
        Path dotted = Files.createDirectory(tempDir.resolve("images.png"));
        assertEquals(dotted.resolve("qr.svg"), parse("X", "--svg", "-o", dotted.toString()).output());
        assertEquals(Path.of(".", "qr.png"), parse("X", "-o", ".").output());
        // not existing and no separator: a file name
        assertEquals(tempDir.resolve("missing.png"), parse("X", "-o", tempDir.resolve("missing").toString()).output());
    }

    @Test
    void svgConflictsWithPngExtension() {
        assertEquals("--svg conflicts with the .png output file 'x.png'", usageError("X", "--svg", "-o", "x.png"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"x.jpg", "x.png.txt", ".png", "my.file"})
    void badOutputFiles(String file) {
        assertEquals("output file must end in .png or .svg, got '" + file + "'", usageError("X", "-o", file));
    }

    @Test
    void emptyOutputFile() {
        assertEquals("invalid output file ''", usageError("X", "-o", ""));
    }

    @Test
    void optionsMayComeBeforeOrAfterText() {
        assertEquals(parse("-l", "H", "X"), parse("X", "-l", "H"));
    }

    @Test
    void textStartingWithDashNeedsDoubleDash() {
        assertEquals("-5", parse("--", "-5").text());
        assertEquals("--help", parse("-l", "L", "--", "--help").text());
        assertEquals("-", parse("-").text());
        assertEquals("unknown option -5", usageError("-5"));
    }

    @Test
    void everythingAfterDoubleDashIsText() {
        assertEquals("unexpected argument '-o' after --: TEXT is already '-5' (put options before --)",
                usageError("--", "-5", "-o", "dash.png"));
        assertEquals("unexpected argument 'B' after --: TEXT is already 'A' (put options before --)",
                usageError("--", "A", "B"));
        assertEquals("unexpected argument 'B' after --: TEXT is already 'A' (put options before --)",
                usageError("A", "--", "B"));
        assertEquals(Path.of("dash.png"), parse("-o", "dash.png", "--", "-5").output());
    }

    @Test
    void missingOrEmptyText() {
        assertEquals("missing TEXT to encode", usageError());
        assertEquals("missing TEXT to encode", usageError("-l", "H"));
        assertEquals("TEXT is empty: nothing to encode", usageError(""));
    }

    @Test
    void secondTextIsRejected() {
        assertTrue(usageError("HELLO", "WORLD").startsWith("unexpected argument 'WORLD'"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-x", "--level", "--version", "-L", "--SVG", "-vv"})
    void unknownOptions(String option) {
        assertEquals("unknown option " + option, usageError("X", option));
    }

    @Test
    void repeatedOption() {
        assertEquals("option -l is given more than once", usageError("X", "-l", "L", "-l", "H"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-l", "-v", "-m", "-s", "-q", "-o"})
    void missingValue(String option) {
        assertEquals("option " + option + " needs a value", usageError("X", option));
    }

    @ParameterizedTest
    @CsvSource({
            "-l, X, '-l must be L, M, Q or H, got ''X'''",
            "-l, LOW, '-l must be L, M, Q or H, got ''LOW'''",
            "-v, 0, '-v must be 1..10, got 0'",
            "-v, 11, '-v must be 1..10, got 11'",
            "-v, V1, '-v must be a number 1..10, got ''V1'''",
            "-m, -1, '-m must be 0..7, got -1'",
            "-m, 8, '-m must be 0..7, got 8'",
            "-s, 0, '-s must be 1..50, got 0'",
            "-s, 51, '-s must be 1..50, got 51'",
            "-s, 2.5, '-s must be a number 1..50, got ''2.5'''",
            "-q, -1, '-q must be 0..40, got -1'",
            "-q, 41, '-q must be 0..40, got 41'",
            "-q, 99999999999, '-q must be a number 0..40, got ''99999999999'''",
    })
    void invalidValues(String option, String value, String message) {
        assertEquals(message, usageError("X", option, value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-h", "--help"})
    void helpWinsOverEverythingElse(String flag) {
        assertTrue(parse(flag).help());
        assertTrue(parse("X", "-l", "Z", flag).help());
        assertTrue(parse("-x", flag).help());
        assertFalse(parse("--", flag).help());
    }

    @Test
    void helpAsAnOptionValueIsNotHelp() {
        assertFalse(parse("X", "-o", "-h").help());
        assertEquals("-l must be L, M, Q or H, got '--help'", usageError("X", "-l", "--help"));
        // -o takes "--" as its value, so the -- does not end the options and --help is the real flag
        assertTrue(parse("X", "-o", "--", "--help").help());
        // and without --help, "--" is just the file name
        assertEquals("X", parse("-o", "--", "X").text());
    }

    @Test
    void usageFitsIn80Columns() {
        for (String line : CliOptions.USAGE.split("\n")) {
            assertTrue(line.length() <= 80, line);
        }
        assertTrue(CliOptions.USAGE.contains("-v 1..10 "));
        assertTrue(CliOptions.USAGE.contains("-m 0..7 "));
    }

    @Test
    void usageMentionsEveryOption() {
        for (String option : new String[]{"-l", "-v", "-m", "-s", "-q", "-o", "--svg", "--debug", "-h, --help", "--"}) {
            assertTrue(CliOptions.USAGE.contains("  " + option + " "), option);
        }
    }
}
