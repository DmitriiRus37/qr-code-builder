package educational.dmitriigurylev.encoders;

import educational.dmitriigurylev.custom_exceptions.InvalidInputFormatException;
import educational.dmitriigurylev.enums.EncodingWay;
import educational.dmitriigurylev.enums.Version;
import educational.dmitriigurylev.utility_maps.DataLengthOfServiceInformation;
import educational.dmitriigurylev.utility_maps.EncodingHeaderMap;
import educational.dmitriigurylev.utility_maps.IntLetterEncoderCharMap;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@NoArgsConstructor
public class IntLetterEncoder implements Encoder {

    private String value;
    private Version version;

    @Override
    public Encoder setValueAndVersion(Object obj, Version version) {
        this.value = (String) obj;
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
        resArr[0] = EncodingHeaderMap.getFieldSizeByVersion(EncodingWay.LETTERS_DIGITS);

        resArr[1] = StringUtils.leftPad(
                Integer.toBinaryString(symbolsCounter),
                DataLengthOfServiceInformation.getDataLengthByVersionAndEncodingWay(version, EncodingWay.LETTERS_DIGITS),
                '0');

        return resArr;
    }

}


