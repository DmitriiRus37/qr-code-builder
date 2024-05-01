package educational.dmitriigurylev;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

public class QrCreator {
    QrCodeField qrCodeField;

    public int[][] createQr() {
        qrCodeField = new QrCodeField(Version.ONE);
        qrCodeField.addFinderPatterns();
        qrCodeField.addSynchronizationLines();

        String[] strAr = IntegerEncoder.separateValue(1234);
        IntegerEncoder.intArrayToBinaryArray(strAr);
        String bitString = IntegerEncoder.binaryArrayToBitString(strAr);
        int[] decimalAr = IntegerEncoder.binaryStringToDecimalString(bitString);
        int[] generatingPolynomial = GeneratingPolynomial.map.get(17);

        Deque<Integer> deque = new LinkedList<>(Arrays.stream(decimalAr).boxed().toList());
        while (deque.size() < generatingPolynomial.length) {
            deque.add(0);
        }

        int aValue = deque.pollFirst();
        deque.add(0);

        int bValue = AB_Map.getMapInstance().getVal(aValue);


        drawImage(qrCodeField.getField());
        return new int[1][1];
    }

    private void drawImage(Cell[][] field) {
        BufferedImage bufferedImage = new BufferedImage(field.length, field[0].length, BufferedImage.TYPE_BYTE_GRAY);

        for (int x = 0; x < field[0].length; x++) {
            for (int y = 0; y < field.length; y++) {
                bufferedImage.setRGB(x, y, field[y][x].getValue() == 0 ? 0xFFFFFF : 0x000000);
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
