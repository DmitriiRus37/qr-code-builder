package educational.dmitriigurylev;

import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
//        String val = "12345678AA";
//        int val = 12345678;
        byte[] val = "Хаб".getBytes(StandardCharsets.UTF_8);

        QrCreator qrCreator = new QrCreator();
        qrCreator.createQr(val);
    }
}