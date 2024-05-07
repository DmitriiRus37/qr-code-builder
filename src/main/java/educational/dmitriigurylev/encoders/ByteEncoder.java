package educational.dmitriigurylev.encoders;

import educational.dmitriigurylev.enums.EncodingWay;
import educational.dmitriigurylev.enums.Version;
import educational.dmitriigurylev.utility_maps.CharacterCountIndicatorMap;
import educational.dmitriigurylev.utility_maps.EncodingModeIndicatorMap;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor
public class ByteEncoder extends AbstractEncoder implements Encoder {

    @Override
    public Encoder setValueAndVersion(Object obj, Version version) {
        this.value = obj;
        this.version = version;
        return this;
    }

    @Override
    public String[] transformToBinaryArray() {
        return byteArrayToBinaryArray((byte[]) this.value);
    }

    private String[] byteArrayToBinaryArray(byte[] arr) {
        int bytesSize = arr.length;
        String[] resArr = new String[3];
        var binaryString = new StringBuilder();
        for (byte b : arr) {
            binaryString.append(Integer.toBinaryString(b < 0 ? b & 0xff : b));
        }
        resArr[0] = EncodingModeIndicatorMap.getFieldSizeByVersion(EncodingWay.BYTES);

        resArr[1] = StringUtils.leftPad(
                Integer.toBinaryString(bytesSize),
                CharacterCountIndicatorMap.getCharacterCountIndicatorByVersionAndEncodingWay(version, EncodingWay.BYTES),
                '0');

        resArr[2] = binaryString.toString();
        return resArr;
    }

}


