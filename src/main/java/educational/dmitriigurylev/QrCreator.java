package educational.dmitriigurylev;

import educational.dmitriigurylev.custom_exceptions.UnknownEncodingTypeException;
import educational.dmitriigurylev.encoders.ByteEncoder;
import educational.dmitriigurylev.encoders.IntLetterEncoder;
import educational.dmitriigurylev.encoders.IntegerEncoder;
import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.Version;
import educational.dmitriigurylev.reed_solomon_mapping.ABMap;
import educational.dmitriigurylev.reed_solomon_mapping.CDMap;
import educational.dmitriigurylev.utility_maps.GeneratingPolynomialMap;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class QrCreator {
    QrCodeField qrCodeField;

    public int[][] createQr(Object objectToEncode) {
        qrCodeField = new QrCodeField(Version.V_1, CorrectionLevel.HIGH);
        qrCodeField.addFinderPatterns();
        qrCodeField.addSynchronizationLines();
        qrCodeField.addInformationTypeBits();

        StringBuilder sb = encodeBits(objectToEncode);
        fillFieldWithBitsSequence(sb);
        applyMaskPattern();
        new QrImageDrawer(qrCodeField).drawImage("qr_image.jpg", "jpg");
        return new int[0][0];
    }

    private StringBuilder encodeBits(Object objectToEncode) {
        String[] binaryArr = getBinaryArray(objectToEncode);
        String bitString = UtilityMethods.binaryArrayToBitString(binaryArr);
        int[] decimalArr = UtilityMethods.binaryStringToDecimalArray(bitString);
        int[] generatingPolynomial = GeneratingPolynomialMap.getGeneratingPolynomial(17);

        List<Integer> listCorrectBytes = Arrays.stream(decimalArr)
                .boxed()
                .collect(Collectors.toCollection(LinkedList::new));
        while (listCorrectBytes.size() < generatingPolynomial.length) {
            listCorrectBytes.add(0);
        }
        int[] cValueArr = new int[generatingPolynomial.length];
        int[] dValueArr = new int[generatingPolynomial.length];

        for (int count = 0; count < decimalArr.length; count++) {
            int aValue = listCorrectBytes.remove(0);
            listCorrectBytes.add(0);
            if (aValue == 0) {
                continue;
            }
            int bValue = ABMap.getVal(aValue);
            if (listCorrectBytes.size() < generatingPolynomial.length) {
                listCorrectBytes.add(0);
            }

            for (int i = 0; i < cValueArr.length; i++) {
                cValueArr[i] = (generatingPolynomial[i] + bValue) % 255;
                dValueArr[i] = CDMap.getVal(cValueArr[i]);
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
        return sb;
    }

    private String[] getBinaryArray(Object objectToEncode) {
        if (objectToEncode.getClass() == Integer.class) {
            return new IntegerEncoder((int) objectToEncode).transformToBinaryArray();
        } else if (objectToEncode.getClass() == String.class && UtilityMethods.containsAllLettersUpperCase((String) objectToEncode)) {
            return new IntLetterEncoder((String) objectToEncode).transformToBinaryArray();
        } else if (objectToEncode.getClass() == byte[].class) {
            return new ByteEncoder((byte[]) objectToEncode).transformToBinaryArray();
        } else {
            throw new UnknownEncodingTypeException("You can encode digits/letters/bytes only");
        }
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

    private void fillFieldWithBitsSequence(StringBuilder sb) {
        Cell[][] f = qrCodeField.getField();
        FillDirection dir = FillDirection.UP;
        int x = f[0].length - 1;
        int y = f.length - 1;

        while (!sb.isEmpty()) {
            while (dir == FillDirection.UP) {
                fillCell(sb, f, x, y);
                x--;

                fillCell(sb, f, x, y);
                x++;
                y--;
                if (y < 0) {
                    dir = FillDirection.DOWN;
                    y++;
                    x-=2;
                    if (x == 6) {
                        x--;
                    }
                }
            }

            while (dir == FillDirection.DOWN) {
                fillCell(sb, f, x, y);
                x--;

                fillCell(sb, f, x, y);
                x++;
                y++;
                if (y >= f.length) {
                    dir = FillDirection.UP;
                    y--;
                    x-=2;
                    if (x == 6) {
                        x--;
                    }
                }
            }
        }
    }

    private void fillCell(StringBuilder sb, Cell[][] f, int x, int y) {
        if (!f[y][x].isBusy() && !sb.isEmpty()) {
            f[y][x].setValue(sb.charAt(0) == '0' ? 0 : 1);
            sb.deleteCharAt(0);
        }
    }

    private enum FillDirection {UP, DOWN}

}
