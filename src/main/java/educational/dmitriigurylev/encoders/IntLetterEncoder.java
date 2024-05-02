package educational.dmitriigurylev.encoders;

import educational.dmitriigurylev.UtilityClass;

import java.util.HashMap;
import java.util.Map;

public class IntLetterEncoder {

    private static final Map<Character, Integer> charIntMap = createCharMap();

    public static int[] encodeSymbols(String value) {
        String[] strAr = separateSymbols(value);
        int[][] intAr = mapCharToInt(strAr);
        String[] binaryArr = symbolsArrayToBinaryArray(intAr);
        String bitString = binaryArrayToBitString(binaryArr);
        return UtilityClass.binaryStringToDecimalString(bitString);
    }

    private static int[][] mapCharToInt(String[] strAr) {
        int[][] res = new int[strAr.length][];
        for (int j = 0; j < strAr.length; j++) {
            String str = strAr[j];
            int[] ar = new int[str.length()];
            for (int i = 0; i < str.length(); i++) {
                ar[i] = charIntMap.get(str.charAt(i));
            }
            res[j] = ar;
        }
        return res;
    }


    private static String[] separateSymbols(String value) {
        int symbolsInValue = value.length();
        String[] arr = symbolsInValue % 2 == 0 ? new String[symbolsInValue / 2] : new String[symbolsInValue / 2 + 1];

        int i = 0;
        while (!value.isEmpty()) {
            if (value.length() >= 2) {
                arr[i++] = value.substring(0, 2);
                value = value.substring(2);
            } else {
                arr[i++] = value;
                value = "";
            }
        }
        return arr;
    }

    private static String[] symbolsArrayToBinaryArray(int[][] arr) {
        int symbolsCounter = 0;
        String[] resArr = new String[arr.length+2];
        for (int i = 0; i < arr.length; i++) {
            int decimalValue = 0;
            for (int curVal : arr[i]) {
                decimalValue = decimalValue * 45 + curVal;
                symbolsCounter++;
            }
            String binaryString = Integer.toBinaryString(decimalValue);
            Map<Integer, String> binaryDigitsCount = Map.of(1, "%6s", 2, "%11s");
            resArr[i+2] = String.format(binaryDigitsCount.get(arr[i].length), binaryString).replace(' ', '0');
        }
        resArr[0] = "0010";

        String binaryString = Integer.toBinaryString(symbolsCounter);
        resArr[1] = String.format("%9s", binaryString).replace(' ', '0');
        return resArr;
    }

    private static String binaryArrayToBitString(String[] strAr) {
        StringBuilder str = new StringBuilder(String.join("", strAr));
        return str.append("0".repeat(Math.max(0, 8 - str.length() % 8))).toString();
    }

    private static Map<Character, Integer> createCharMap() {
        return new HashMap<>(){{
            put('0', 0);
            put('1', 1);
            put('2', 2);
            put('3', 3);
            put('4', 4);
            put('5', 5);
            put('6', 6);
            put('7', 7);
            put('8', 8);
            put('9', 9);
            put('A', 10);
            put('B', 11);
            put('C', 12);
            put('D', 13);
            put('E', 14);
            put('F', 15);
            put('G', 16);
            put('H', 17);
            put('I', 18);
            put('J', 19);
            put('K', 20);
            put('L', 21);
            put('M', 22);
            put('N', 23);
            put('O', 24);
            put('P', 25);
            put('Q', 26);
            put('R', 27);
            put('S', 28);
            put('T', 29);
            put('U', 30);
            put('V', 31);
            put('W', 32);
            put('X', 33);
            put('Y', 34);
            put('Z', 35);
            put(' ', 36);
            put('$', 37);
            put('%', 38);
            put('*', 39);
            put('+', 40);
            put('-', 41);
            put('.', 42);
            put('/', 43);
            put(':', 44);
        }};
    }

}


