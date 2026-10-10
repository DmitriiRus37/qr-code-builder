package educational.dmitriigurylev;

import educational.dmitriigurylev.enums.CorrectionLevel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End to end, in process: {@link Main#run} with captured stdout/stderr writes into a {@code @TempDir}, and the
 * written files are decoded by BoofCV and ZXing.
 */
class MainTest {

    private static final String URL = "https://example.com";

    @TempDir
    Path tempDir;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private int run(String... args) {
        return Main.run(args, new PrintStream(out, true, StandardCharsets.UTF_8),
                new PrintStream(err, true, StandardCharsets.UTF_8));
    }

    private String out() {
        return out.toString(StandardCharsets.UTF_8);
    }

    private String err() {
        return err.toString(StandardCharsets.UTF_8);
    }

    private static void assertDecodes(String expected, BufferedImage image) throws Exception {
        assertEquals(expected, QrTestSupport.decodeBoofcv(image), "BoofCV");
        assertEquals(expected, QrTestSupport.decode(image), "ZXing");
    }

    @Test
    void writesPngIntoNewDirectories() throws Exception {
        Path file = tempDir.resolve("a/b/x.png");
        assertEquals(Main.EXIT_OK, run(URL, "-o", file.toString()));

        int mask = QrCreator.of(URL, CorrectionLevel.MIDDLE).createQr().getMaskPattern().getNumber();
        assertEquals("wrote " + file + " (V2-M, byte mode, mask " + mask + ")" + System.lineSeparator(), out());
        assertEquals("", err());
        BufferedImage image = ImageIO.read(file.toFile());
        assertEquals((25 + 8) * 10, image.getWidth(), "V2 at the default scale and quiet zone");
        assertDecodes(URL, image);
    }

    @Test
    void writesSvgWithEveryOption() throws Exception {
        Path base = tempDir.resolve("svg/qr");
        assertEquals(Main.EXIT_OK, run("-l", "H", "-v", "5", "-m", "6", "-s", "3", "-q", "5", "--svg", "--debug",
                "-o", base.toString(), "HELLO WORLD"));

        Path file = tempDir.resolve("svg/qr.svg");
        assertEquals("wrote " + file + " (V5-H, alphanumeric mode, mask 6)" + System.lineSeparator()
                + "wrote " + tempDir.resolve("svg/debug_qr.svg") + System.lineSeparator(), out());
        assertEquals("", err());
        String svg = Files.readString(file);
        int side = 37 + 2 * 5;
        assertTrue(svg.contains("viewBox=\"0 0 " + side + " " + side + "\""), svg);
        assertTrue(svg.contains("width=\"" + side * 3 + "\""), svg);
        assertDecodes("HELLO WORLD", QrTestSupport.rasterizeSvg(file, 8));
        assertTrue(Files.exists(tempDir.resolve("svg/debug_qr.svg")));
    }

    @Test
    void outputEndingInSeparatorIsANewDirectory() throws Exception {
        Path dir = tempDir.resolve("new");
        assertEquals(Main.EXIT_OK, run("DIR", "-o", dir + "/"));
        assertDecodes("DIR", ImageIO.read(dir.resolve("qr.png").toFile()));
        // the directory exists now, so without the separator it is still the directory
        assertEquals(Main.EXIT_OK, run("DIR2", "--svg", "-o", dir.toString()));
        assertDecodes("DIR2", QrTestSupport.rasterizeSvg(dir.resolve("qr.svg"), 8));
    }

    @Test
    void helpGoesToStdout() {
        assertEquals(Main.EXIT_OK, run("--help"));
        assertEquals(CliOptions.USAGE, out());
        assertEquals("", err());
    }

    @Test
    void usageErrorExitsWith2() throws Exception {
        assertEquals(Main.EXIT_USAGE, run("X", "-v", "11", "-o", tempDir.resolve("x.png").toString()));
        assertEquals("", out());
        assertTrue(err().startsWith("error: -v must be 1..10, got 11" + System.lineSeparator()), err());
        assertTrue(err().endsWith(CliOptions.USAGE), err());
        assertNoFiles();
    }

    @Test
    void missingTextExitsWith2() {
        assertEquals(Main.EXIT_USAGE, run());
        assertTrue(err().startsWith("error: missing TEXT to encode"), err());
    }

    @Test
    void capacityOverflowExitsWith1WithoutStackTrace() throws Exception {
        Path file = tempDir.resolve("x.png");
        assertEquals(Main.EXIT_FAILURE, run("A".repeat(400), "-l", "L", "-o", file.toString()));
        assertEquals("", out());
        assertEquals("error: cannot encode the text: alphanumeric payload of 400 characters needs 2215 bits, "
                + "V10-L holds 2192 (the largest supported version)" + System.lineSeparator(), err());
        assertNoFiles();
    }

    @Test
    void explicitVersionTooSmallExitsWith1() throws Exception {
        assertEquals(Main.EXIT_FAILURE, run("HELLO WORLD", "-v", "1", "-l", "H", "-o",
                tempDir.resolve("x.png").toString()));
        assertEquals("error: cannot encode the text: alphanumeric payload of 11 characters needs 74 bits, V1-H holds 72"
                + System.lineSeparator(), err());
        assertNoFiles();
    }

    @Test
    void unwritableOutputExitsWith1() throws Exception {
        Path blocker = Files.writeString(tempDir.resolve("file"), "not a directory");
        assertEquals(Main.EXIT_FAILURE, run("X", "-o", blocker.resolve("x.png").toString()));
        assertEquals("", out());
        assertTrue(err().startsWith("error: cannot create directory " + blocker + ": "), err());
        assertFalse(err().contains("\tat "), "no stack trace");
    }

    private void assertNoFiles() throws Exception {
        try (Stream<Path> files = Files.list(tempDir)) {
            assertEquals(0, files.count(), "nothing is written");
        }
    }
}
