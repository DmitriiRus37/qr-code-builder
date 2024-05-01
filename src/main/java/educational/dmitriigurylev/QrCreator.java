package educational.dmitriigurylev;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class QrCreator {
    QrCodeField qrCodeField;

    public int[][] createQr() {
        qrCodeField = new QrCodeField(Version.ONE);
        qrCodeField.addFinderPatterns();

        drawImage(qrCodeField.getField());
        return new int[1][1];
    }

    private void drawImage(int[][] field) {
        BufferedImage bufferedImage = new BufferedImage(field.length, field[0].length, BufferedImage.TYPE_BYTE_GRAY);

        for (int x = 0; x < field[0].length; x++) {
            for (int y = 0; y < field.length; y++) {
                bufferedImage.setRGB(x, y, field[y][x] == 0 ? 0xFFFFFF : 0x000000);
            }
        }

        // Define the output file
        File outputFile = new File("output.jpg");

        try {
            // Write the BufferedImage to the JPEG file
            ImageIO.write(bufferedImage, "jpg", outputFile);
            System.out.println("JPEG file written successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
