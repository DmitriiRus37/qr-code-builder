package educational.dmitriigurylev;

import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.Version;

public class Main {
    public static void main(String[] args) {
        var qr = new QrCreator(Version.V_2, CorrectionLevel.HIGH, 1);
        qr.createQr();
        qr.drawQr("qr_image.jpg");
    }
}
