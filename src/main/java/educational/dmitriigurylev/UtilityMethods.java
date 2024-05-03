package educational.dmitriigurylev;


import educational.dmitriigurylev.custom_exceptions.InvalidInputFormatException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static String binaryArrayToBitString(String[] strAr) {
        if (strAr == null) {
            throw new InvalidInputFormatException();
        }
        StringBuilder str = new StringBuilder(String.join("", strAr));
        byte remainToWholeByte = (byte) (8 - str.length() % 8);
        str.append("0".repeat(remainToWholeByte == 8 ? 0 : remainToWholeByte));
        return str.toString();
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
}
