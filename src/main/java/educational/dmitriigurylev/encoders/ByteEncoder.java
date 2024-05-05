package educational.dmitriigurylev.encoders;

import educational.dmitriigurylev.enums.EncodingWay;
import educational.dmitriigurylev.enums.Version;
import educational.dmitriigurylev.utility_maps.DataLengthOfServiceInformation;
import educational.dmitriigurylev.utility_maps.EncodingHeaderMap;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor
public class ByteEncoder implements Encoder {

    private byte[] value;
    private Version version;

    @Override
    public Encoder setValueAndVersion(Object obj, Version version) {
        this.value = (byte[]) obj;
        this.version = version;
        return this;
    }

    @Override
    public String[] transformToBinaryArray() {
        return byteArrayToBinaryArray(this.value);
    }

    private String[] byteArrayToBinaryArray(byte[] arr) {
        int bytesSize = arr.length;
        String[] resArr = new String[3];
        var binaryString = new StringBuilder();
        for (byte b : arr) {
            binaryString.append(Integer.toBinaryString(b < 0 ? b & 0xff : b));
        }
        resArr[0] = EncodingHeaderMap.getFieldSizeByVersion(EncodingWay.BYTES);

        resArr[1] = StringUtils.leftPad(
                Integer.toBinaryString(bytesSize),
                DataLengthOfServiceInformation.getDataLengthByVersionAndEncodingWay(version, EncodingWay.BYTES),
                '0');

        resArr[2] = binaryString.toString();
        return resArr;
    }

}


