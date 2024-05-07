package educational.dmitriigurylev.encoders;

import educational.dmitriigurylev.custom_exceptions.InvalidInputFormatException;
import educational.dmitriigurylev.enums.EncodingWay;
import educational.dmitriigurylev.enums.Version;
import educational.dmitriigurylev.utility_maps.CharacterCountIndicatorMap;
import educational.dmitriigurylev.utility_maps.EncodingModeIndicatorMap;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@NoArgsConstructor
public class IntegerEncoder extends AbstractEncoder implements Encoder {

    @Override
    public Encoder setValueAndVersion(Object obj, Version version) {
        if (obj.getClass() == String.class) {
            this.value = obj;
        } else if (obj.getClass() == Integer.class) {
            this.value = String.valueOf((int)obj);
        } else if (obj.getClass() == Long.class) {
            this.value = String.valueOf((long)obj);
        }
        else {
            throw new InvalidInputFormatException();
        }
        this.version = version;
        return this;
    }

    @Override
    public String[] transformToBinaryArray() {
        String[] strAr = separateSymbols();
        symbolsArrayToBinaryArray(strAr);
        return strAr;
    }

    private String[] separateSymbols() {
        String strInteger = String.valueOf(value);
        int digitsInValue = strInteger.length();
        String[] arr = digitsInValue % 3 == 0 ? new String[digitsInValue / 3 + 2] : new String[digitsInValue / 3 + 3];
        arr[1] = String.valueOf(digitsInValue);

        int i = 2;
        while (!strInteger.isEmpty()) {
            if (strInteger.length() >= 3) {
                String substr = StringUtils.stripStart(strInteger.substring(0, 3),"0");
                strInteger = strInteger.substring(3);
                arr[i++] = substr;
            } else {
                arr[i++] = strInteger;
                strInteger = "";
            }
        }
        return arr;
    }

    private void symbolsArrayToBinaryArray(String[] arr) {
        Map<Integer, Integer> binaryDigitsCount = Map.of(1, 4, 2, 7, 3, 10);
        for (int i = 2; i < arr.length; i++) {
            int decimalValue = Integer.parseInt(arr[i]);
            String binaryString = Integer.toBinaryString(decimalValue);
            arr[i] = StringUtils.leftPad(
                    binaryString,
                    binaryDigitsCount.get(arr[i].length()),
                    '0');
        }
        arr[0] = EncodingModeIndicatorMap.getFieldSizeByVersion(EncodingWay.DIGITS);

        arr[1] = StringUtils.leftPad(
                Integer.toBinaryString(Integer.parseInt(arr[1])),
                CharacterCountIndicatorMap.getCharacterCountIndicatorByVersionAndEncodingWay(version, EncodingWay.DIGITS),
                '0');
    }

}


