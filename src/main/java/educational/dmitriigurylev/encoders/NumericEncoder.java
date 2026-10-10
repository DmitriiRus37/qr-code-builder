package educational.dmitriigurylev.encoders;

import educational.dmitriigurylev.UtilityMethods;
import educational.dmitriigurylev.custom_exceptions.InvalidInputFormatException;
import educational.dmitriigurylev.enums.EncodingWay;
import educational.dmitriigurylev.enums.Version;
import educational.dmitriigurylev.utility_maps.CharacterCountIndicatorMap;
import educational.dmitriigurylev.utility_maps.EncodingModeIndicatorMap;

import java.util.Map;

public class NumericEncoder implements Encoder {

    @Override
    public String[] encode(Object obj, Version version) {
        String[] strAr = separateSymbols(toDigits(obj));
        symbolsArrayToBinaryArray(strAr, version);
        return strAr;
    }

    /** QrCreator passes the text; numbers were already turned into their decimal text there. */
    private static String toDigits(Object obj) {
        if (!(obj instanceof String digits)) {
            throw new InvalidInputFormatException("numeric mode takes a String, got "
                    + (obj == null ? "null" : obj.getClass().getName()));
        }
        // numeric mode holds the digits 0-9 only: no sign, no blanks, not empty
        if (!digits.matches("\\d+")) {
            throw new InvalidInputFormatException("numeric mode accepts digits 0-9 only: " + digits);
        }
        return digits;
    }

    private static String[] separateSymbols(String strInteger) {
        int digitsInValue = strInteger.length();
        String[] arr = digitsInValue % 3 == 0 ? new String[digitsInValue / 3 + 2] : new String[digitsInValue / 3 + 3];
        arr[1] = String.valueOf(digitsInValue);

        int i = 2;
        while (!strInteger.isEmpty()) {
            if (strInteger.length() >= 3) {
                // leading zeros are kept: "012" is still a 3-digit group
                arr[i++] = strInteger.substring(0, 3);
                strInteger = strInteger.substring(3);
            } else {
                arr[i++] = strInteger;
                strInteger = "";
            }
        }
        return arr;
    }

    private static void symbolsArrayToBinaryArray(String[] arr, Version version) {
        // the group width depends on the number of digits, never on the value ("012" takes 10 bits)
        Map<Integer, Integer> binaryDigitsCount = Map.of(1, 4, 2, 7, 3, 10);
        for (int i = 2; i < arr.length; i++) {
            arr[i] = UtilityMethods.toBits(Integer.parseInt(arr[i]), binaryDigitsCount.get(arr[i].length()));
        }
        arr[0] = EncodingModeIndicatorMap.getModeIndicator(EncodingWay.NUMERIC);

        arr[1] = UtilityMethods.characterCountToBits(
                Integer.parseInt(arr[1]),
                CharacterCountIndicatorMap.getCharacterCountIndicatorByVersionAndEncodingWay(version, EncodingWay.NUMERIC));
    }

}
