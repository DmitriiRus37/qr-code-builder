package educational.dmitriigurylev;

import boofcv.abst.fiducial.QrCodeDetector;
import boofcv.alg.fiducial.qrcode.QrCode;
import boofcv.factory.fiducial.ConfigQrCode;
import boofcv.factory.fiducial.FactoryFiducial;
import boofcv.struct.image.GrayU8;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;
import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.Version;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Decodes a generated matrix with two independent QR readers, ZXing and BoofCV, so tests check
 * "is this a valid QR code with the right content" instead of comparing against our own output.
 * Rendering is done here on purpose (not via QrImageDrawer) so drawer bugs cannot hide encoder bugs.
 */
public final class QrTestSupport {

    private static final int SCALE = 8;
    private static final int QUIET_ZONE = 4;

    private static final Map<DecodeHintType, Object> HINTS = Map.of(
            DecodeHintType.TRY_HARDER, Boolean.TRUE,
            DecodeHintType.POSSIBLE_FORMATS, List.of(BarcodeFormat.QR_CODE),
            DecodeHintType.CHARACTER_SET, "UTF-8");

    /** Skips detection: the symbol is assumed to fill the image exactly (minus the quiet zone). Checks the bits only. */
    private static final Map<DecodeHintType, Object> PURE_HINTS = Map.of(
            DecodeHintType.PURE_BARCODE, Boolean.TRUE,
            DecodeHintType.POSSIBLE_FORMATS, List.of(BarcodeFormat.QR_CODE),
            DecodeHintType.CHARACTER_SET, "UTF-8");

    /** A correct symbol that ZXing's detector cannot locate; {@code text} is the payload and the expected decode. */
    public record UndetectableByZxing(Version version, CorrectionLevel level, String text) {
        @Override
        public String toString() {
            return "V" + version.getNumber() + "-" + level.name().charAt(0) + " \"" + text + "\"";
        }
    }

    /**
     * Symbols that are correct but that ZXing's finder-pattern detector cannot locate in our rendering.
     * For each of them our matrix is bit-identical to the one ZXing's own encoder builds (same lowest-penalty mask),
     * ZXing cannot read its own output back either, the bits decode with PURE_BARCODE, and BoofCV decodes the symbol
     * normally; {@link #assertOnlyZxingDetectorFails} checks all of this. Whether ZXing finds them depends on the
     * rendering scale (each decodes at 2 px per module), the signature of the detector taking a finder-like pattern
     * in the data area for a real one. A seeded sweep of 960 random payloads hits this for about 1 % of them.
     */
    public static final List<UndetectableByZxing> UNDETECTABLE_BY_ZXING = List.of(
            new UndetectableByZxing(Version.V_1, CorrectionLevel.QUARTER, "007"),
            new UndetectableByZxing(Version.V_5, CorrectionLevel.LOW, "Хаб"),
            new UndetectableByZxing(Version.V_6, CorrectionLevel.QUARTER, "HELLO"),
            new UndetectableByZxing(Version.V_6, CorrectionLevel.MIDDLE, "A1 $%*+-./"),
            new UndetectableByZxing(Version.V_10, CorrectionLevel.QUARTER, "A1 $%*+-./"),
            new UndetectableByZxing(Version.V_6, CorrectionLevel.QUARTER, "5074145457"));

    private QrTestSupport() {
    }

    public static BufferedImage render(Cell[][] field) {
        int n = field.length;
        int size = (n + 2 * QUIET_ZONE) * SCALE;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int row = y / SCALE - QUIET_ZONE;
                int col = x / SCALE - QUIET_ZONE;
                boolean dark = row >= 0 && col >= 0 && row < n && col < n && field[row][col].getValue() == 1;
                img.setRGB(x, y, dark ? 0x000000 : 0xFFFFFF);
            }
        }
        return img;
    }

    /** @return decoded text, or throws {@link ReaderException} when the matrix is not a readable QR code */
    public static String decode(Cell[][] field) throws ReaderException {
        return decode(render(field));
    }

    /** ZXing decoding of an image produced elsewhere (e.g. a PNG written by QrImageDrawer and read back). */
    public static String decode(BufferedImage image) throws ReaderException {
        var bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image)));
        return new MultiFormatReader().decode(bitmap, HINTS).getText();
    }

    /**
     * Decodes with BoofCV, a second reader whose detector shares no code with ZXing's. The image is rendered here as
     * well (same scale and quiet zone). Byte mode is read as UTF-8, and transposed symbols are not accepted.
     *
     * @return the decoded text, or null if BoofCV finds no single readable QR code
     */
    public static String decodeBoofcv(Cell[][] field) {
        return decodeBoofcv(render(field));
    }

    /** BoofCV decoding of any image; each pixel's gray level is the mean of its RGB channels. */
    public static String decodeBoofcv(BufferedImage img) {
        GrayU8 image = new GrayU8(img.getWidth(), img.getHeight());
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);
                image.set(x, y, (((rgb >> 16) & 0xFF) + ((rgb >> 8) & 0xFF) + (rgb & 0xFF)) / 3);
            }
        }
        ConfigQrCode config = new ConfigQrCode();
        config.forceEncoding = "UTF-8";
        config.considerTransposed = false;
        QrCodeDetector<GrayU8> detector = FactoryFiducial.qrcode(config, GrayU8.class);
        detector.process(image);
        List<QrCode> found = detector.getDetections();
        return found.size() == 1 ? found.get(0).message : null;
    }

    /**
     * Rasterises an SVG written by {@link QrImageDrawer} at {@code pixelsPerModule} pixels per viewBox unit: the
     * square viewBox in white, then every {@code <rect>} inside a {@code <g>} in black. Only the subset of SVG the
     * drawer writes is understood; {@code QrImageDrawerTest} checks that subset in detail.
     */
    public static BufferedImage rasterizeSvg(Path file, int pixelsPerModule) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Document doc = factory.newDocumentBuilder().parse(file.toFile());
        String[] viewBox = doc.getDocumentElement().getAttribute("viewBox").split(" ");
        int side = Integer.parseInt(viewBox[2]) * pixelsPerModule;
        BufferedImage img = new BufferedImage(side, side, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < side; y++) {
            for (int x = 0; x < side; x++) {
                img.setRGB(x, y, 0xFFFFFF);
            }
        }
        NodeList groups = doc.getElementsByTagNameNS("*", "g");
        for (int g = 0; g < groups.getLength(); g++) {
            NodeList rects = ((Element) groups.item(g)).getElementsByTagNameNS("*", "rect");
            for (int i = 0; i < rects.getLength(); i++) {
                Element rect = (Element) rects.item(i);
                int x0 = Integer.parseInt(rect.getAttribute("x")) * pixelsPerModule;
                int y0 = Integer.parseInt(rect.getAttribute("y")) * pixelsPerModule;
                int w = Integer.parseInt(rect.getAttribute("width")) * pixelsPerModule;
                int h = Integer.parseInt(rect.getAttribute("height")) * pixelsPerModule;
                for (int y = y0; y < y0 + h; y++) {
                    for (int x = x0; x < x0 + w; x++) {
                        img.setRGB(x, y, 0x000000);
                    }
                }
            }
        }
        return img;
    }

    /**
     * The round-trip oracle: the symbol for {@code input} must decode to {@code expected} with BoofCV and with ZXing.
     * The only exception are the {@link #UNDETECTABLE_BY_ZXING} entries, which are checked by
     * {@link #assertOnlyZxingDetectorFails} instead.
     */
    public static void assertRoundTrip(Version version, CorrectionLevel level, Object input, String expected) throws Exception {
        if (UNDETECTABLE_BY_ZXING.contains(new UndetectableByZxing(version, level, expected))) {
            assertOnlyZxingDetectorFails(version, level, input, expected);
            return;
        }
        Cell[][] field = new QrCreator(version, level, input).createQr().getQrCodeField().getField();
        assertEquals(expected, decodeBoofcv(field), "BoofCV");
        assertEquals(expected, decode(field), "ZXing");
    }

    /**
     * Same oracle for a creator that was already built (e.g. through {@code QrCreator.of(...)}): its own field is
     * decoded, so the automatic choices are what gets checked, not a rebuild through the fixed-version constructor.
     */
    public static void assertRoundTrip(QrCreator built, Object input, String expected) throws Exception {
        Version version = built.getVersion();
        CorrectionLevel level = built.getCorrectionLevel();
        if (UNDETECTABLE_BY_ZXING.contains(new UndetectableByZxing(version, level, expected))) {
            assertOnlyZxingDetectorFails(version, level, input, expected);
            return;
        }
        Cell[][] field = built.getQrCodeField().getField();
        assertEquals(expected, decodeBoofcv(field), "BoofCV");
        assertEquals(expected, decode(field), "ZXing");
    }

    /**
     * Like {@link #decode}, but with {@code PURE_BARCODE}: no finder-pattern detection, so it shows whether the
     * bits are right even when the detector cannot locate the symbol.
     */
    public static String decodePure(Cell[][] field) throws ReaderException {
        var bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(render(field))));
        return new MultiFormatReader().decode(bitmap, PURE_HINTS).getText();
    }

    /**
     * Encodes the same payload with ZXing's own encoder (an independent reference implementation).
     * A {@code String} is handed over as its UTF-8 bytes read as ISO-8859-1, so ZXing writes exactly the bytes we
     * write and no ECI header. ZXing picks the mode from the characters like we do; a {@code byte[]} must therefore
     * contain a byte outside the alphanumeric set, or ZXing would not use byte mode for it.
     *
     * @param mask the mask to force, or null to let ZXing choose it by its penalty rules
     */
    public static QRCode zxingEncode(Object input, Version version, CorrectionLevel level, Integer mask) throws WriterException {
        byte[] bytes = input instanceof byte[] b ? b : String.valueOf(input).getBytes(StandardCharsets.UTF_8);
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.QR_VERSION, version.getNumber());
        if (mask != null) {
            hints.put(EncodeHintType.QR_MASK_PATTERN, mask);
        }
        ErrorCorrectionLevel zxingLevel = ErrorCorrectionLevel.valueOf(level.name().substring(0, 1));
        return Encoder.encode(new String(bytes, StandardCharsets.ISO_8859_1), zxingLevel, hints);
    }

    /** @return true if every module of {@code field} equals the module of ZXing's matrix */
    public static boolean sameMatrix(ByteMatrix expected, Cell[][] field) {
        if (expected.getHeight() != field.length || expected.getWidth() != field[0].length) {
            return false;
        }
        for (int row = 0; row < field.length; row++) {
            for (int col = 0; col < field[row].length; col++) {
                // ByteMatrix.get(x, y): x is the column
                if (expected.get(col, row) != field[row][col].getValue()) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Method source for the test that checks every {@link #UNDETECTABLE_BY_ZXING} entry. */
    static List<UndetectableByZxing> undetectableByZxing() {
        return UNDETECTABLE_BY_ZXING;
    }

    /**
     * For a symbol listed as undetectable: ZXing's normal decoding still fails (strict expected failure, so a symbol
     * that starts to decode must be removed from the list), ZXing's encoder builds the very same matrix, the bits
     * decode to {@code expected} with PURE_BARCODE, and BoofCV decodes the symbol normally.
     * So an entry can only hide a ZXing reader limitation, never an encoder bug.
     */
    public static void assertOnlyZxingDetectorFails(Version version, CorrectionLevel level, Object input, String expected)
            throws Exception {
        Cell[][] field = new QrCreator(version, level, input).createQr().getQrCodeField().getField();
        String decoded;
        try {
            decoded = decode(field);
        } catch (ReaderException e) {
            decoded = null;
        }
        assertNotEquals(expected, decoded, "ZXing now decodes it - remove it from UNDETECTABLE_BY_ZXING");
        assertTrue(sameMatrix(zxingEncode(input, version, level, null).getMatrix(), field), "matrix differs from ZXing's encoder");
        assertEquals(expected, decodePure(field), "ZXing PURE_BARCODE");
        assertEquals(expected, decodeBoofcv(field), "BoofCV");
    }
}
