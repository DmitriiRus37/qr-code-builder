package educational.dmitriigurylev;

import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.Version;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionInformationTest {

    @ParameterizedTest
    @CsvSource({
            "7, 000111110010010100",
            "8, 001000010110111100",
            "9, 001001101010011001",
            "10, 001010010011010011"})
    void versionInformationBitsMatchSpec(int versionNumber, String expected) {
        String bits = Integer.toBinaryString(QrCodeField.versionInformationBits(versionNumber));
        assertEquals(expected, "0".repeat(18 - bits.length()) + bits);
    }

    /**
     * Both 6x3 blocks must match the ones ZXing's own encoder writes for the same version.
     * Version information does not depend on data, level or mask, and is never masked.
     */
    @ParameterizedTest
    @EnumSource(value = Version.class, names = {"V_7", "V_8", "V_9", "V_10"})
    void bothBlocksMatchZxingEncoder(Version version) throws WriterException {
        ByteMatrix expected = Encoder.encode("1", ErrorCorrectionLevel.L,
                Map.of(EncodeHintType.QR_VERSION, version.getNumber())).getMatrix();
        for (CorrectionLevel level : CorrectionLevel.values()) {
            Cell[][] field = new QrCreator(version, level, "HELLO").createQr().getQrCodeField().getField();
            int size = field.length;
            assertEquals(expected.getWidth(), size);
            for (int far = size - 11; far < size - 8; far++) {
                for (int near = 0; near < 6; near++) {
                    // ByteMatrix.get(x, y): x is the column
                    assertEquals(expected.get(far, near), field[near][far].getValue(),
                            "top-right block at row " + near + ", col " + far);
                    assertEquals(expected.get(near, far), field[far][near].getValue(),
                            "bottom-left block at row " + far + ", col " + near);
                }
            }
        }
    }

    /** On a fresh field (nothing reserved yet) exactly the 36 version cells become busy. */
    @ParameterizedTest
    @EnumSource(value = Version.class, names = {"V_7", "V_8", "V_9", "V_10"})
    void writesAndMarksBusyExactlyTheTwoBlocks(Version version) {
        QrCodeField qrCodeField = new QrCodeField(version, CorrectionLevel.HIGH);
        qrCodeField.addVersionInformation();
        Cell[][] field = qrCodeField.getField();
        int size = field.length;
        int busy = 0;
        for (Cell[] row : field) {
            for (Cell cell : row) {
                busy += cell.isBusy() ? 1 : 0;
            }
        }
        assertEquals(36, busy);
        for (int far = size - 11; far < size - 8; far++) {
            for (int near = 0; near < 6; near++) {
                assertTrue(field[near][far].isBusy(), "top-right block at row " + near + ", col " + far);
                assertTrue(field[far][near].isBusy(), "bottom-left block at row " + far + ", col " + near);
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = Version.class, names = {"V_1", "V_2", "V_3", "V_4", "V_5", "V_6"})
    void noVersionInformationBelowV7(Version version) {
        QrCodeField qrCodeField = new QrCodeField(version, CorrectionLevel.HIGH);
        qrCodeField.addVersionInformation();
        for (Cell[] row : qrCodeField.getField()) {
            for (Cell cell : row) {
                assertFalse(cell.isBusy());
                assertEquals(0, cell.getValue());
            }
        }
    }
}
