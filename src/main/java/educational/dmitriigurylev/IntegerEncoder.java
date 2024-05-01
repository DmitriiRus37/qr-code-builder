package educational.dmitriigurylev;

import java.util.Map;

public class IntegerEncoder {

    public static String[] separateValue(int value) {
        String strInteger = String.valueOf(value);
        int digitsInValue = strInteger.length();
        String[] arr = digitsInValue % 3 == 0 ? new String[digitsInValue / 3 + 2] : new String[digitsInValue / 3 + 3];
        arr[1] = String.valueOf(digitsInValue);

        int i = 2;
        while (!strInteger.isEmpty()) {
            if (strInteger.length() >= 3) {
                arr[i++] = strInteger.substring(0, 3);
                strInteger = strInteger.substring(3);
            } else {
                arr[i++] = strInteger;
                strInteger = "";
            }
        }
        return arr;
    }

    public static void intArrayToBinaryArray(String[] arr) {
        for (int i = 1; i < arr.length; i++) {
            int decimalValue = Integer.parseInt(arr[i]);
            String binaryString = Integer.toBinaryString(decimalValue);
            if (i == 1) {
                arr[i] = String.format("%10s", binaryString).replace(' ', '0');
            } else {
                Map<Integer, String> binaryDigitsCount = Map.of(1, "%4s", 2, "%7s", 3, "%11s");
                arr[i] = String.format(binaryDigitsCount.get(arr[i].length()), binaryString).replace(' ', '0');
            }
        }
        arr[0] = "0001";
    }

    public static String binaryArrayToBitString(String[] strAr) {
        StringBuilder str = new StringBuilder(String.join("", strAr));
        return str.append("0".repeat(Math.max(0, 8 - str.length() % 8))).toString();
    }

    public static int[] binaryStringToDecimalString(String bitString) {
        int[] decimalIntArray = new int[9];
        int i = 0;
        while (!bitString.isEmpty()) {
            decimalIntArray[i++] = binaryToDecimal(bitString.substring(0, 8));
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

    private static int binaryToDecimal(String binaryString) {
        int decimal = 0;
        int length = binaryString.length();
        for (int i = 0; i < length; i++) {
            if (binaryString.charAt(i) == '1') {
                decimal += (int) Math.pow(2, length - i - 1);
            }
        }
        return decimal;
    }

}


