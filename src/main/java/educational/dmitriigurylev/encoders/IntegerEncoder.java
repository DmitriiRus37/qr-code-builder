package educational.dmitriigurylev.encoders;

import educational.dmitriigurylev.enums.EncodingWay;
import educational.dmitriigurylev.enums.Version;
import educational.dmitriigurylev.utility_maps.DataLengthOfServiceInformation;
import educational.dmitriigurylev.utility_maps.EncodingHeaderMap;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@NoArgsConstructor
public class IntegerEncoder extends AbstractEncoder implements Encoder {

    @Override
    public Encoder setValueAndVersion(Object obj, Version version) {
        this.value = obj.getClass() == String.class ? (String) obj : String.valueOf((int)obj);
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
                arr[i++] = strInteger.substring(0, 3);
                strInteger = strInteger.substring(3);
            } else {
                arr[i++] = strInteger;
                strInteger = "";
            }
        }
        return arr;
    }

    private void symbolsArrayToBinaryArray(String[] arr) {
        for (int i = 2; i < arr.length; i++) {
            int decimalValue = Integer.parseInt(arr[i]);
            String binaryString = Integer.toBinaryString(decimalValue);
            Map<Integer, String> binaryDigitsCount = Map.of(1, "%4s", 2, "%7s", 3, "%10s");
            arr[i] = String.format(binaryDigitsCount.get(arr[i].length()), binaryString).replace(' ', '0');
        }
        arr[0] = EncodingHeaderMap.getFieldSizeByVersion(EncodingWay.DIGITS);

        arr[1] = StringUtils.leftPad(
                Integer.toBinaryString(Integer.parseInt(arr[1])),
                DataLengthOfServiceInformation.getDataLengthByVersionAndEncodingWay(version, EncodingWay.DIGITS),
                '0');
    }

}


