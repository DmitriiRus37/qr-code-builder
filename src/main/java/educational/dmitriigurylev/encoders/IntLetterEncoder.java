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
        String bitString = UtilityClass.binaryArrayToBitString(binaryArr);
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

    private static Map<Character, Integer> createCharMap() {
        Map<Character, Integer> m = new HashMap<>();
        m.put('0', 0);
        m.put('1', 1);
        m.put('2', 2);
        m.put('3', 3);
        m.put('4', 4);
        m.put('5', 5);
        m.put('6', 6);
        m.put('7', 7);
        m.put('8', 8);
        m.put('9', 9);
        m.put('A', 10);
        m.put('B', 11);
        m.put('C', 12);
        m.put('D', 13);
        m.put('E', 14);
        m.put('F', 15);
        m.put('G', 16);
        m.put('H', 17);
        m.put('I', 18);
        m.put('J', 19);
        m.put('K', 20);
        m.put('L', 21);
        m.put('M', 22);
        m.put('N', 23);
        m.put('O', 24);
        m.put('P', 25);
        m.put('Q', 26);
        m.put('R', 27);
        m.put('S', 28);
        m.put('T', 29);
        m.put('U', 30);
        m.put('V', 31);
        m.put('W', 32);
        m.put('X', 33);
        m.put('Y', 34);
        m.put('Z', 35);
        m.put(' ', 36);
        m.put('$', 37);
        m.put('%', 38);
        m.put('*', 39);
        m.put('+', 40);
        m.put('-', 41);
        m.put('.', 42);
        m.put('/', 43);
        m.put(':', 44);
        return m;
    }

}


