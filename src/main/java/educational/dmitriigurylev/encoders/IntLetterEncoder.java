package educational.dmitriigurylev.encoders;

import educational.dmitriigurylev.UtilityMethods;
import educational.dmitriigurylev.custom_exceptions.InvalidInputFormatException;
import educational.dmitriigurylev.enums.EncodingWay;
import educational.dmitriigurylev.enums.Version;
import educational.dmitriigurylev.utility_maps.CharacterCountIndicatorMap;
import educational.dmitriigurylev.utility_maps.EncodingModeIndicatorMap;
import educational.dmitriigurylev.utility_maps.IntLetterEncoderCharMap;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
public class IntLetterEncoder implements Encoder {

    @Override
    public String[] encode(Object obj, Version version) {
        if (!(obj instanceof String val)) {
            throw new InvalidInputFormatException();
        }
        String[] strAr = separateSymbols(val);
        int[][] intAr = mapCharToInt(strAr);
        return symbolsArrayToBinaryArray(intAr, version);
    }

    private static String[] separateSymbols(String val) {
        int symbolsInValue = val.length();
        String[] arr = symbolsInValue % 2 == 0 ? new String[symbolsInValue / 2] : new String[symbolsInValue / 2 + 1];

        int i = 0;
        while (!val.isEmpty()) {
            if (val.length() >= 2) {
                arr[i++] = val.substring(0, 2);
                val = val.substring(2);
            } else {
                arr[i++] = val;
                val = "";
            }
        }
        return arr;
    }

    private static int[][] mapCharToInt(String[] strAr) {
        int[][] res = new int[strAr.length][];
        for (int j = 0; j < strAr.length; j++) {
            String str = strAr[j];
            int[] ar = new int[str.length()];
            for (int i = 0; i < str.length(); i++) {
                if (IntLetterEncoderCharMap.contains(str.charAt(i))) {
                    ar[i] = IntLetterEncoderCharMap.getIntByChar(str.charAt(i));
                } else {
                    throw new InvalidInputFormatException();
                }
            }
            res[j] = ar;
        }
        return res;
    }

    private static String[] symbolsArrayToBinaryArray(int[][] arr, Version version) {
        Map<Integer, Integer> binaryDigitsCount = Map.of(1, 6, 2, 11);
        int symbolsCounter = 0;
        String[] resArr = new String[arr.length+2];
        for (int i = 0; i < arr.length; i++) {
            int decimalValue = 0;
            for (int curVal : arr[i]) {
                decimalValue = decimalValue * 45 + curVal;
                symbolsCounter++;
            }
            resArr[i+2] = UtilityMethods.toBits(decimalValue, binaryDigitsCount.get(arr[i].length));
        }
        resArr[0] = EncodingModeIndicatorMap.getFieldSizeByVersion(EncodingWay.LETTERS_DIGITS);

        resArr[1] = UtilityMethods.characterCountToBits(
                symbolsCounter,
                CharacterCountIndicatorMap.getCharacterCountIndicatorByVersionAndEncodingWay(version, EncodingWay.LETTERS_DIGITS));

        return resArr;
    }

}


