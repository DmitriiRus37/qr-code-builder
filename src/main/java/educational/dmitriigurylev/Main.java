package educational.dmitriigurylev;

import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.Version;

import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
//        int val = 1; // ok
//        int val = 12; // not ok !!!!!!
//        int val = 123; // not ok !!!!!!
//        int val = 1234; // ok
//        int val = 12345; // not ok !!!!!!
        int val = 123456; // ok
//        String val = "DIMA"; // ok
//        String val = "HE L LO"; // ok
//        byte[] val = "п".getBytes(StandardCharsets.UTF_8); // ok
//        byte[] val = "абв".getBytes(StandardCharsets.UTF_8); // ok
//        byte[] val = "ХАБ".getBytes(StandardCharsets.UTF_8); // ok
//        byte[] val = "ХАБРПРи".getBytes(StandardCharsets.UTF_8); // ok
//        byte[] val = "HELLO WO".getBytes(StandardCharsets.UTF_8); // not ok
//        String val = "HELLO WORLD";

        new QrCreator(Version.V_1, CorrectionLevel.HIGH, val).createQr("qr_image.jpg");


//        String val = "123456789123456789"; // ok
//        new QrCreator(Version.V_1, CorrectionLevel.HIGH, val).createQr("qr_image.jpg");
    }
}
