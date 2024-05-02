package educational.dmitriigurylev;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class QrCreator {
    QrCodeField qrCodeField;

    public int[][] createQr() {
        qrCodeField = new QrCodeField(Version.ONE);
        qrCodeField.addFinderPatterns();
        qrCodeField.addSynchronizationLines();
        qrCodeField.addTypeInformationBits();


        String[] strAr = IntegerEncoder.separateValue(1234);
        IntegerEncoder.intArrayToBinaryArray(strAr);
        String bitString = IntegerEncoder.binaryArrayToBitString(strAr);
        int[] decimalArr = IntegerEncoder.binaryStringToDecimalString(bitString);
        int[] generatingPolynomial = GeneratingPolynomial.map.get(17);

//        listCorrectBytes = new LinkedList<>() {{add(32);add(91);add(11);add(120);add(209);add(114);add(220);add(77);add(67);add(64);add(236);add(17);add(236);add(17);add(236);add(17);}};
//        generatingPolynomial = new int[]{251,67,46,61,118,70,64,94,32,45};

        List<Integer> listCorrectBytes = new LinkedList<>(Arrays.stream(decimalArr).boxed().collect(Collectors.toList()));
        while (listCorrectBytes.size() < generatingPolynomial.length) {
            listCorrectBytes.add(0);
        }
        int[] cValueArr = new int[generatingPolynomial.length];
        int[] dValueArr = new int[generatingPolynomial.length];

        for (int count = 0; count < generatingPolynomial.length; count++) {
            int aValue = listCorrectBytes.remove(0);
            int bValue = AB_Map.getMapInstance().getVal(aValue);
            if (listCorrectBytes.size() < generatingPolynomial.length) {
                listCorrectBytes.add(0);
            }

            for (int i = 0; i < cValueArr.length; i++) {
                cValueArr[i] = (generatingPolynomial[i] + bValue) % 255;
                dValueArr[i] = CD_Map.getMapInstance().getVal(cValueArr[i]);
                listCorrectBytes.set(i, dValueArr[i] ^ listCorrectBytes.get(i));
            }
        }

        String[] unitedArr = new String[decimalArr.length + listCorrectBytes.size()];
        for (int i = 0; i < decimalArr.length; i++) {
            unitedArr[i] = String.valueOf(decimalArr[i]);
        }
        for (int i = decimalArr.length; i < unitedArr.length; i++) {
            unitedArr[i] = String.valueOf(listCorrectBytes.remove(0));
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < unitedArr.length; i++) {
            int decimalValue = Integer.parseInt(unitedArr[i]);
            String binStr = Integer.toBinaryString(decimalValue);
            unitedArr[i] = String.format("%8s", binStr).replace(' ', '0');
            sb.append(unitedArr[i]);
        }
        encodeBitsSequence(sb);
        applyMaskPattern();
        drawImage(qrCodeField.getField());
        return new int[1][1];
    }

    private void applyMaskPattern() {
        for (int x=0; x<qrCodeField.getField()[0].length; x++) {
            if (x % 3 == 0) {
                for (int y = 0; y < qrCodeField.getField().length; y++) {
                    if (qrCodeField.getField()[y][x].isBusy()) {
                        continue;
                    }
                    qrCodeField.getField()[y][x].setValue(qrCodeField.getField()[y][x].getValue() == 1 ? 0 : 1);
                }
            }
        }
    }

    private void encodeBitsSequence(StringBuilder sb) {
        Direction dir = Direction.up;
        int x = qrCodeField.getField()[0].length - 1;
        int y = qrCodeField.getField().length - 1;

        while (!sb.isEmpty()) {
            while (dir == Direction.up) {
                if (sb.charAt(0) == '0') {
                    qrCodeField.getField()[y][x].setValue(0).setBusy(true);
                } else {
                    qrCodeField.getField()[y][x].setValue(1).setBusy(true);
                }
                x--;
            }
        }
    }

    private void drawImage(Cell[][] field) {
        BufferedImage qrImage = new BufferedImage(
                field[0].length+4,
                field.length+4,
                BufferedImage.TYPE_BYTE_GRAY);

        for (int x = 0; x < field[0].length+4; x++) {
            if (x == 0 || x == 1 || x == field[0].length+2 || x == field[0].length+3) {
                for (int y = 2; y < field.length+2; y++) {
                    qrImage.setRGB(x, y, 0xFFFFFF);
                }
            }
            qrImage.setRGB(x, 0, 0xFFFFFF);
            qrImage.setRGB(x, 1, 0xFFFFFF);
            qrImage.setRGB(x, field.length+2, 0xFFFFFF);
            qrImage.setRGB(x, field.length+3, 0xFFFFFF);
        }

        for (int x = 2; x < field[0].length+2; x++) {
            for (int y = 2; y < field.length+2; y++) {
                qrImage.setRGB(x, y, field[y-2][x-2].getValue() == 0 ? 0xFFFFFF : 0x000000);
            }
        }
        File outputQrImageFile = new File("qr_image.jpg");
        try {
            ImageIO.write(qrImage, "jpg", outputQrImageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    enum Direction {up, down}

}
