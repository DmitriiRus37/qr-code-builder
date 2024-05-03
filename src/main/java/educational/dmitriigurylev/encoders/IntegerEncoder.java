package educational.dmitriigurylev.encoders;

import educational.dmitriigurylev.UtilityClass;

import java.util.Map;

public class IntegerEncoder implements Encoder {

    private final int value;
    public IntegerEncoder(int value) {
        this.value = value;
    }

    @Override
    public int[] encodeSymbols() {
        String[] strAr = separateSymbols();
        symbolsArrayToBinaryArray(strAr);
        String bitString = UtilityClass.binaryArrayToBitString(strAr);
        return UtilityClass.binaryStringToDecimalString(bitString);
    }

    private String[] separateSymbols() {
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

    private static void symbolsArrayToBinaryArray(String[] arr) {
        for (int i = 2; i < arr.length; i++) {
            int decimalValue = Integer.parseInt(arr[i]);
            String binaryString = Integer.toBinaryString(decimalValue);
            Map<Integer, String> binaryDigitsCount = Map.of(1, "%4s", 2, "%7s", 3, "%11s");
            arr[i] = String.format(binaryDigitsCount.get(arr[i].length()), binaryString).replace(' ', '0');
        }
        arr[0] = "0001";

        String binaryString = Integer.toBinaryString(Integer.parseInt(arr[1]));
        arr[1] = String.format("%10s", binaryString).replace(' ', '0');
    }


}


