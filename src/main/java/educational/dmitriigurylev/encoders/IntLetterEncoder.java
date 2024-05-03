package educational.dmitriigurylev.encoders;

import educational.dmitriigurylev.UtilityMethods;

import java.util.Map;

public class IntLetterEncoder implements Encoder {

    private String value;
    public IntLetterEncoder(String value) {
        this.value = value;
    }

    private static final Map<Character, Integer> charIntMap = createCharMap();

    @Override
    public int[] encodeSymbols() {
        String[] strAr = separateSymbols();
        int[][] intAr = mapCharToInt(strAr);
        String[] binaryArr = symbolsArrayToBinaryArray(intAr);
        String bitString = UtilityMethods.binaryArrayToBitString(binaryArr);
        return UtilityMethods.binaryStringToDecimalArray(bitString);
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

    private String[] separateSymbols() {
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
        return Map.ofEntries(
                Map.entry('0', 0),
                Map.entry('1', 1),
                Map.entry('2', 2),
                Map.entry('3', 3),
                Map.entry('4', 4),
                Map.entry('5', 5),
                Map.entry('6', 6),
                Map.entry('7', 7),
                Map.entry('8', 8),
                Map.entry('9', 9),
                Map.entry('A', 10),
                Map.entry('B', 11),
                Map.entry('C', 12),
                Map.entry('D', 13),
                Map.entry('E', 14),
                Map.entry('F', 15),
                Map.entry('G', 16),
                Map.entry('H', 17),
                Map.entry('I', 18),
                Map.entry('J', 19),
                Map.entry('K', 20),
                Map.entry('L', 21),
                Map.entry('M', 22),
                Map.entry('N', 23),
                Map.entry('O', 24),
                Map.entry('P', 25),
                Map.entry('Q', 26),
                Map.entry('R', 27),
                Map.entry('S', 28),
                Map.entry('T', 29),
                Map.entry('U', 30),
                Map.entry('V', 31),
                Map.entry('W', 32),
                Map.entry('X', 33),
                Map.entry('Y', 34),
                Map.entry('Z', 35),
                Map.entry(' ', 36),
                Map.entry('$', 37),
                Map.entry('%', 38),
                Map.entry('*', 39),
                Map.entry('+', 40),
                Map.entry('-', 41),
                Map.entry('.', 42),
                Map.entry('/', 43),
                Map.entry(':', 44));
    }

}


