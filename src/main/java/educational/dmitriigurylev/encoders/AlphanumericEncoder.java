package educational.dmitriigurylev.encoders;

import educational.dmitriigurylev.UtilityMethods;
import educational.dmitriigurylev.custom_exceptions.InvalidInputFormatException;
import educational.dmitriigurylev.enums.EncodingWay;
import educational.dmitriigurylev.enums.Version;
import educational.dmitriigurylev.utility_maps.AlphanumericCharMap;
import educational.dmitriigurylev.utility_maps.CharacterCountIndicatorMap;
import educational.dmitriigurylev.utility_maps.EncodingModeIndicatorMap;

import java.util.Map;

/** Alphanumeric mode: character pairs are packed into 11 bits (45 * a + b), a trailing single character into 6. */
public class AlphanumericEncoder implements Encoder {

    /**
     * {@inheritDoc}
     *
     * @throws InvalidInputFormatException if {@code obj} is not a String of alphanumeric characters
     */
    @Override
    public String[] encode(Object obj, Version version) {
        if (!(obj instanceof String val)) {
            throw new InvalidInputFormatException("alphanumeric mode takes a String, got "
                    + (obj == null ? "null" : obj.getClass().getName()));
        }
        String[] strAr = separateSymbols(val);
        int[][] intAr = mapCharToInt(strAr);
        return symbolsArrayToBinaryArray(intAr, version);
    }

    /** Splits the text into pairs of characters; an odd length leaves one character in the last element. */
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

    /** Replaces each character by its alphanumeric value 0-44; throws for characters outside the set. */
    private static int[][] mapCharToInt(String[] strAr) {
        int[][] res = new int[strAr.length][];
        for (int j = 0; j < strAr.length; j++) {
            String str = strAr[j];
            int[] ar = new int[str.length()];
            for (int i = 0; i < str.length(); i++) {
                if (AlphanumericCharMap.contains(str.charAt(i))) {
                    ar[i] = AlphanumericCharMap.getIntByChar(str.charAt(i));
                } else {
                    throw new InvalidInputFormatException("not an alphanumeric character: '" + str.charAt(i) + "'");
                }
            }
            res[j] = ar;
        }
        return res;
    }

    /** Builds {mode indicator, character count, 11- or 6-bit groups...} from the character values. */
    private static String[] symbolsArrayToBinaryArray(int[][] arr, Version version) {
        Map<Integer, Integer> binaryDigitsCount = Map.of(1, 6, 2, 11);
        int symbolsCounter = 0;
        String[] resArr = new String[arr.length+2];
        for (int i = 0; i < arr.length; i++) {
            int pairValue = 0;
            for (int curVal : arr[i]) {
                pairValue = pairValue * 45 + curVal;
                symbolsCounter++;
            }
            resArr[i+2] = UtilityMethods.toBits(pairValue, binaryDigitsCount.get(arr[i].length));
        }
        resArr[0] = EncodingModeIndicatorMap.getModeIndicator(EncodingWay.ALPHANUMERIC);

        resArr[1] = UtilityMethods.characterCountToBits(
                symbolsCounter,
                CharacterCountIndicatorMap.getCharacterCountIndicatorByVersionAndEncodingWay(version, EncodingWay.ALPHANUMERIC));

        return resArr;
    }

}


