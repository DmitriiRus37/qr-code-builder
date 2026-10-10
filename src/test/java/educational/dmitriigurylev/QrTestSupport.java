package educational.dmitriigurylev;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

/**
 * Decodes a generated matrix with ZXing, an independent QR reader, so tests check
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
        var bitmap = new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(render(field))));
        return new MultiFormatReader().decode(bitmap, HINTS).getText();
    }
}
