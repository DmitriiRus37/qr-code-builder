package educational.dmitriigurylev;

import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.Version;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.image.BufferedImage;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Checks the files {@link QrImageDrawer} writes: PNG files are read back with ImageIO and decoded by ZXing and
 * BoofCV, their size and every pixel are compared with the matrix; SVG files are parsed as XML and rasterised.
 */
class QrImageDrawerTest {

    private static final String SVG_NS = "http://www.w3.org/2000/svg";
    private static final int WHITE = 0xFFFFFF;

    /** One payload per version × level; it fits V1-H, the smallest capacity. */
    private static final String SAMPLE = "IMG 11";

    @TempDir
    Path tempDir;

    /** Every version x level. */
    static Stream<Arguments> versionsAndLevels() {
        return Arrays.stream(Version.values())
                .flatMap(v -> Arrays.stream(CorrectionLevel.values()).map(l -> Arguments.of(v, l)));
    }

    /** A creator for {@link #SAMPLE} built at the given version and level. */
    private static QrCreator built(Version version, CorrectionLevel level) {
        return QrCreator.of(SAMPLE, level).version(version).createQr();
    }

    @ParameterizedTest(name = "{0}-{1}")
    @MethodSource("versionsAndLevels")
    void defaultPngIsReadableAndMatchesTheMatrix(Version version, CorrectionLevel level) throws Exception {
        QrCreator creator = built(version, level);
        Path file = tempDir.resolve("qr.png");
        creator.drawQr(file);

        byte[] png = Files.readAllBytes(file);
        // IHDR starts at byte 16 (8-byte signature, chunk length, "IHDR"); bit depth at 24, colour type at 25
        assertEquals("IHDR", new String(png, 12, 4, StandardCharsets.US_ASCII));
        assertEquals(1, png[24], "bit depth");
        assertEquals(0, png[25], "colour type: grayscale");
        BufferedImage image = ImageIO.read(file.toFile());
        assertNotNull(image, "ImageIO cannot read the file back");
        assertPixelsMatch(creator.getQrCodeField().getField(), image, 10, 4);
        assertEquals(SAMPLE, QrTestSupport.decodeBoofcv(image), "BoofCV");
        assertEquals(SAMPLE, QrTestSupport.decode(image), "ZXing");
    }

    @ParameterizedTest(name = "scale {0}, quiet zone {1}")
    @MethodSource("scalesAndQuietZones")
    void customScaleAndQuietZone(int scale, int quietZone) throws Exception {
        QrCreator creator = built(Version.V_3, CorrectionLevel.MEDIUM);
        Path file = tempDir.resolve("custom.png");
        creator.drawer().setScale(scale).setQuietZone(quietZone).write(file);

        BufferedImage image = ImageIO.read(file.toFile());
        assertPixelsMatch(creator.getQrCodeField().getField(), image, scale, quietZone);
        if (quietZone >= 4 && scale >= 3) {
            assertEquals(SAMPLE, QrTestSupport.decodeBoofcv(image), "BoofCV");
            assertEquals(SAMPLE, QrTestSupport.decode(image), "ZXing");
        }
    }

    /** Scale/quiet-zone pairs, including the minimum and maximum values. */
    static Stream<Arguments> scalesAndQuietZones() {
        return Stream.of(Arguments.of(1, 0), Arguments.of(1, 4), Arguments.of(3, 2), Arguments.of(4, 4),
                Arguments.of(7, 6), Arguments.of(QrImageDrawer.MAX_SCALE, 1), Arguments.of(2, QrImageDrawer.MAX_QUIET_ZONE));
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(Version.class)
    void svgIsWellFormedAndMatchesTheMatrix(Version version) throws Exception {
        QrCreator creator = built(version, CorrectionLevel.QUARTILE);
        Cell[][] field = creator.getQrCodeField().getField();
        Path file = tempDir.resolve("qr.SVG");
        creator.drawer().setScale(3).write(file);

        int side = version.size() + 8;
        Document doc = parse(file);
        Element svg = doc.getDocumentElement();
        assertEquals(SVG_NS, svg.getNamespaceURI());
        assertEquals("svg", svg.getLocalName());
        assertEquals("0 0 " + side + " " + side, svg.getAttribute("viewBox"));
        assertEquals(String.valueOf(side * 3), svg.getAttribute("width"));
        assertEquals(String.valueOf(side * 3), svg.getAttribute("height"));

        Element background = (Element) doc.getElementsByTagNameNS(SVG_NS, "rect").item(0);
        assertEquals(svg, background.getParentNode(), "the background rect is a direct child of <svg>");
        assertEquals(String.valueOf(side), background.getAttribute("width"));
        assertEquals(String.valueOf(side), background.getAttribute("height"));
        assertEquals("#ffffff", background.getAttribute("fill"));

        // rasterise at 1 px per viewBox unit: the background rect, then the dark runs inside <g fill="#000000">
        boolean[][] dark = new boolean[side][side];
        NodeList groups = doc.getElementsByTagNameNS(SVG_NS, "g");
        assertEquals(1, groups.getLength());
        assertEquals("#000000", ((Element) groups.item(0)).getAttribute("fill"));
        NodeList rects = ((Element) groups.item(0)).getElementsByTagNameNS(SVG_NS, "rect");
        int darkArea = 0;
        for (int i = 0; i < rects.getLength(); i++) {
            Element rect = (Element) rects.item(i);
            int x = Integer.parseInt(rect.getAttribute("x"));
            int y = Integer.parseInt(rect.getAttribute("y"));
            int w = Integer.parseInt(rect.getAttribute("width"));
            assertEquals("1", rect.getAttribute("height"));
            for (int dx = 0; dx < w; dx++) {
                assertFalse(dark[y][x + dx], "overlapping rects at " + (x + dx) + "," + y);
                dark[y][x + dx] = true;
            }
            darkArea += w;
        }
        assertEquals(countDark(field), darkArea, "dark area of the rects");
        assertEquals(rects.getLength() + 1, doc.getElementsByTagNameNS(SVG_NS, "rect").getLength(),
                "only the background rect lies outside the group");

        BufferedImage raster = new BufferedImage(side * 8, side * 8, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < side * 8; y++) {
            for (int x = 0; x < side * 8; x++) {
                raster.setRGB(x, y, dark[y / 8][x / 8] ? 0 : WHITE);
            }
        }
        assertPixelsMatch(field, raster, 8, 4);
        assertEquals(SAMPLE, QrTestSupport.decodeBoofcv(raster), "BoofCV");
        assertEquals(SAMPLE, QrTestSupport.decode(raster), "ZXing");
    }

    @Test
    void toSvgEqualsTheWrittenFile() throws Exception {
        QrImageDrawer drawer = built(Version.V_1, CorrectionLevel.LOW).drawer().setQuietZone(2);
        Path file = tempDir.resolve("qr.svg");
        drawer.write(file);
        assertEquals(drawer.toSvg(), Files.readString(file));
    }

    @Test
    void explicitFormatOverridesTheExtension() throws Exception {
        Path file = tempDir.resolve("qr.img");
        built(Version.V_1, CorrectionLevel.LOW).drawer().write(file, QrImageDrawer.Format.PNG);
        assertNotNull(ImageIO.read(file.toFile()));
    }

    @Test
    void debugImageGoesNextToTheImageInASubdirectory() throws Exception {
        QrCreator creator = built(Version.V_7, CorrectionLevel.HIGH);
        Path dir = Files.createDirectories(tempDir.resolve("out").resolve("nested"));
        Path file = dir.resolve("qr.png");
        creator.drawer().setDebug(true).write(file);

        Path debug = dir.resolve("debug_qr.png");
        assertEquals(debug, QrImageDrawer.debugPath(file));
        assertTrue(Files.exists(file));
        assertTrue(Files.exists(debug));
        try (Stream<Path> all = Files.walk(tempDir)) {
            assertEquals(2, all.filter(Files::isRegularFile).count(), "no stray files (e.g. debug_out/...)");
        }
        // the debug image shows the reserved modules in dark
        Cell[][] field = creator.getQrCodeField().getField();
        Cell[][] busy = new Cell[field.length][field.length];
        for (int row = 0; row < field.length; row++) {
            for (int col = 0; col < field.length; col++) {
                busy[row][col] = new Cell(false, field[row][col].isBusy() ? 1 : 0);
            }
        }
        assertPixelsMatch(busy, ImageIO.read(debug.toFile()), 10, 4);
    }

    @Test
    void debugPathOfAParentlessPath() {
        assertEquals(Path.of("debug_qr.png"), QrImageDrawer.debugPath(Path.of("qr.png")));
    }

    @Test
    void drawerWorksOnAFieldFromBuild() throws Exception {
        Path file = tempDir.resolve("qr.png");
        new QrImageDrawer(QrCreator.of(SAMPLE, CorrectionLevel.LOW).build()).write(file);
        BufferedImage image = ImageIO.read(file.toFile());
        assertEquals((Version.V_1.size() + 8) * 10, image.getWidth());
        assertEquals(SAMPLE, QrTestSupport.decodeBoofcv(image), "BoofCV");
        assertEquals(SAMPLE, QrTestSupport.decode(image), "ZXing");
    }

    @Test
    void noDebugImageByDefault() throws Exception {
        Path file = tempDir.resolve("qr.svg");
        built(Version.V_1, CorrectionLevel.LOW).drawQr(file);
        assertTrue(Files.exists(file));
        assertFalse(Files.exists(tempDir.resolve("debug_qr.svg")));
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -1, 0, QrImageDrawer.MAX_SCALE + 1})
    void invalidScaleIsRejected(int scale) {
        QrImageDrawer drawer = built(Version.V_1, CorrectionLevel.LOW).drawer();
        assertThrows(IllegalArgumentException.class, () -> drawer.setScale(scale));
    }

    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, -1, QrImageDrawer.MAX_QUIET_ZONE + 1})
    void invalidQuietZoneIsRejected(int quietZone) {
        QrImageDrawer drawer = built(Version.V_1, CorrectionLevel.LOW).drawer();
        assertThrows(IllegalArgumentException.class, () -> drawer.setQuietZone(quietZone));
    }

    @ParameterizedTest
    @ValueSource(strings = {"qr.jpg", "qr", "qr.png.txt", ".png", ".SVG"})
    void unsupportedExtensionIsRejected(String name) {
        QrCreator creator = built(Version.V_1, CorrectionLevel.LOW);
        assertThrows(IllegalArgumentException.class, () -> creator.drawQr(tempDir.resolve(name)));
        assertFalse(Files.exists(tempDir.resolve(name)));
    }

    @Test
    void ioErrorsAreThrownUnchecked() {
        QrCreator creator = built(Version.V_1, CorrectionLevel.LOW);
        Path missingDir = tempDir.resolve("missing").resolve("qr.png");
        var e = assertThrows(UncheckedIOException.class, () -> creator.drawQr(missingDir));
        assertTrue(e.getMessage().contains(missingDir.toString()));
    }

    /** Image side = (N + 2·quietZone)·scale, the quiet zone is white, and every module's pixels match the matrix. */
    private static void assertPixelsMatch(Cell[][] field, BufferedImage image, int scale, int quietZone) {
        int n = field.length;
        int side = (n + 2 * quietZone) * scale;
        assertEquals(side, image.getWidth(), "width");
        assertEquals(side, image.getHeight(), "height");
        for (int y = 0; y < side; y++) {
            for (int x = 0; x < side; x++) {
                int row = y / scale - quietZone;
                int col = x / scale - quietZone;
                boolean inSymbol = row >= 0 && col >= 0 && row < n && col < n;
                int expected = inSymbol && field[row][col].getValue() == 1 ? 0 : WHITE;
                int actual = image.getRGB(x, y) & 0xFFFFFF;
                if (actual != expected) {
                    assertEquals(expected, actual, (inSymbol ? "module " + row + "," + col : "quiet zone")
                            + " at pixel " + x + "," + y);
                }
            }
        }
    }

    /** Number of dark modules in the matrix. */
    private static int countDark(Cell[][] field) {
        return (int) Arrays.stream(field).flatMap(Arrays::stream).filter(c -> c.getValue() == 1).count();
    }

    /** Parses an SVG file as namespace-aware XML (doctype declarations are refused). */
    private static Document parse(Path file) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder().parse(file.toFile());
    }
}
