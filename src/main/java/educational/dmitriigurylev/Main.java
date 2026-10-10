package educational.dmitriigurylev;

import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.Version;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        var qr = new QrCreator(Version.V_2, CorrectionLevel.HIGH, 1);
        qr.createQr();
        qr.drawQr(Path.of("qr_image.png"));
    }
}
