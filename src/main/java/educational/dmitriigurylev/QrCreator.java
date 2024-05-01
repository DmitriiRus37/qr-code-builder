package educational.dmitriigurylev;

import java.util.HashMap;
import java.util.Map;

public class QrCreator {
    QrCodeField qrCodeField;

    public int[][] createQr() {
        qrCodeField = new QrCodeField(Version.ONE);
        qrCodeField.addFinderPatterns();
        return new int[1][1];
    }
}
