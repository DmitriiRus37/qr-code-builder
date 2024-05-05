package educational.dmitriigurylev;

import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.Version;

import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
//        int val = 12;
//        String val = "DIMA"; // ok
//        String val = "HE L LO"; // ok
//        byte[] val = "п".getBytes(StandardCharsets.UTF_8); // ok
        byte[] val = "абв".getBytes(StandardCharsets.UTF_8); // ok
//        byte[] val = "ХАБ".getBytes(StandardCharsets.UTF_8); // ok
//        byte[] val = "HELLO WO".getBytes(StandardCharsets.UTF_8); // not ok

        new QrCreator(Version.V_1, CorrectionLevel.HIGH, val).createQr();
    }
}
