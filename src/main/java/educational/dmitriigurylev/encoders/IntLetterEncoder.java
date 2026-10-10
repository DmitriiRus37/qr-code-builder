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
public class IntLetterEncoder extends AbstractEncoder implements Encoder {

    @Override
    public Encoder setValueAndVersion(Object obj, Version version) {
        this.value = obj;
        this.version = version;
        return this;
    }

    @Override
    public String[] transformToBinaryArray() {
        String[] strAr = separateSymbols();
        int[][] intAr = mapCharToInt(strAr);
        return symbolsArrayToBinaryArray(intAr);
    }

    private String[] separateSymbols() {
        String val = (String) this.value;
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

    private String[] symbolsArrayToBinaryArray(int[][] arr) {
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


