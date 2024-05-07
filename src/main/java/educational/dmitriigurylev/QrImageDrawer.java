package educational.dmitriigurylev;

import lombok.Setter;
import lombok.experimental.Accessors;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class QrImageDrawer {

    @Setter
    @Accessors(chain = true)
    private boolean isDebug = false;

    public QrImageDrawer(QrCodeField qrCodeField) {
        this.qrCodeField = qrCodeField;
    }

    QrCodeField qrCodeField;

    public void drawImage(String fileName, String fileFormat) {
        Cell[][] field = qrCodeField.getField();

        BufferedImage qrImage = new BufferedImage(
                field[0].length+4,
                field.length+4,
                BufferedImage.TYPE_BYTE_GRAY);
        drawFrame(qrImage);

        BufferedImage qrImageBusy = new BufferedImage(
                field[0].length+4,
                field.length+4,
                BufferedImage.TYPE_BYTE_GRAY);
        drawFrame(qrImageBusy);

        for (int x = 2; x < field[0].length+2; x++) {
            for (int y = 2; y < field.length+2; y++) {
                qrImage.setRGB(x, y, field[y-2][x-2].getValue() == 0 ? 0xFFFFFF : 0x000000);
                if (isDebug) {
                    qrImageBusy.setRGB(x, y, !field[y-2][x-2].isBusy() ? 0xFFFFFF : 0x000000);
                }
            }
        }
        File outputQrImageFile = new File(fileName);
        try {
            ImageIO.write(qrImage, fileFormat, outputQrImageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }


        File outputQrImageBusyFile = new File("debug_" + fileName);
        if (isDebug) {
            try {
                ImageIO.write(qrImageBusy, fileFormat, outputQrImageBusyFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void drawFrame(BufferedImage qrImage) {
        int width = qrCodeField.getField().length;
        int height = qrCodeField.getField()[0].length;
        for (int x = 0; x < height+4; x++) {
            if (x == 0 || x == 1 || x == height+2 || x == height+3) {
                for (int y = 2; y < width+2; y++) {
                    qrImage.setRGB(x, y, 0xFFFFFF);
                }
            }
            qrImage.setRGB(x, 0, 0xFFFFFF);
            qrImage.setRGB(x, 1, 0xFFFFFF);
            qrImage.setRGB(x, width+2, 0xFFFFFF);
            qrImage.setRGB(x, width+3, 0xFFFFFF);
        }
    }

}
