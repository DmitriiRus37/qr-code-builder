package educational.dmitriigurylev;


import educational.dmitriigurylev.custom_exceptions.InvalidInputFormatException;
import educational.dmitriigurylev.reed_solomon_mapping.ABMap;
import educational.dmitriigurylev.reed_solomon_mapping.CDMap;
import educational.dmitriigurylev.utility_maps.GeneratingPolynomialMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UtilityMethods {
    public static int binaryToDecimal(String binaryString) {
        if (binaryString == null || binaryString.isEmpty() || !containsZerosAndOnesOnly(binaryString)) {
            throw new InvalidInputFormatException();
        }
        int decimal = 0;
        int length = binaryString.length();
        for (int i = 0; i < length; i++) {
            if (binaryString.charAt(i) == '1') {
                decimal += (int) Math.pow(2, length - i - 1);
            }
        }
        return decimal;
    }

    public static StringBuilder binaryArrayToBitString(String[] strAr) {
        if (strAr == null) {
            throw new InvalidInputFormatException();
        }
        return new StringBuilder(String.join("", strAr));
    }

    public static String addLagZeros(StringBuilder binaryString) {
        StringBuilder res = new StringBuilder(binaryString);
        byte remainToWholeByte = (byte) (8 - binaryString.length() % 8);
        return res.append("0".repeat(remainToWholeByte == 8 ? 0 : remainToWholeByte)).toString();
    }

    public static int[] binaryStringToDecimalArray(String bitString) {
        if (bitString == null || bitString.isEmpty() || bitString.length() % 8 != 0) {
            throw new InvalidInputFormatException();
        }
        int[] decimalIntArray = new int[9];
        int i = 0;
        while (!bitString.isEmpty()) {
            decimalIntArray[i++] = UtilityMethods.binaryToDecimal(bitString.substring(0, 8));
            bitString = bitString.substring(8);
        }
        while (true) {
            if (i>=9) break;
            decimalIntArray[i++] = 236;
            if (i>=9) break;
            decimalIntArray[i++] = 17;
        }
        return decimalIntArray;
    }

    public static boolean containsAllLettersUpperCase(String str) {
        if (str == null) {
            throw new InvalidInputFormatException();
        }
        return str.chars()
                .filter(Character::isLetter)
                .allMatch(Character::isUpperCase);
    }

    public static boolean containsZerosAndOnesOnly(String str) {
        if (str == null) {
            throw new InvalidInputFormatException();
        }
        Pattern pattern = Pattern.compile("^[01]+$");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    public static List<Integer> getCorrectionBytes(int[] decimalArr, int correctionBytesPerBlock) {
        int[] generatingPolynomial = GeneratingPolynomialMap.getGeneratingPolynomial(correctionBytesPerBlock);
        List<Integer> listCorrectBytes = Arrays.stream(decimalArr)
                .boxed()
                .collect(Collectors.toCollection(ArrayList::new));
        while (listCorrectBytes.size() < generatingPolynomial.length) {
            listCorrectBytes.add(0);
        }

        for (int count = 0; count < decimalArr.length; count++) {
            int aValue = listCorrectBytes.remove(0);
            listCorrectBytes.add(0);
            if (aValue == 0) {
                continue;
            }
            int bValue = ABMap.getBackGaluaFieldValue(aValue);
            if (listCorrectBytes.size() < correctionBytesPerBlock) {
                listCorrectBytes.add(0);
            }

            for (int i = 0; i < correctionBytesPerBlock; i++) {
                int cValue = (generatingPolynomial[i] + bValue) % 255;
                int dValue = CDMap.getGaluaFieldValue(cValue);
                listCorrectBytes.set(i, dValue ^ listCorrectBytes.get(i));
            }
        }
        return listCorrectBytes;
    }
}
