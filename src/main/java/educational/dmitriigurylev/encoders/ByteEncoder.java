package educational.dmitriigurylev.encoders;

import educational.dmitriigurylev.enums.EncodingWay;
import educational.dmitriigurylev.utility_maps.EncodingHeaderMap;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ByteEncoder implements Encoder {

    private byte[] value;

    @Override
    public Encoder setValueToTransform(Object obj) {
        this.value = (byte[]) obj;
        return this;
    }

    @Override
    public String[] transformToBinaryArray() {
        return byteArrayToBinaryArray(this.value);
    }

    private static String[] byteArrayToBinaryArray(byte[] arr) {
        int symbolsCounter = arr.length;
        String[] resArr = new String[3];
        var binaryString = new StringBuilder();
        for (byte b : arr) {
            binaryString.append(Integer.toBinaryString(b < 0 ? b & 0xff : b));
        }
        resArr[0] = EncodingHeaderMap.getFieldSizeByVersion(EncodingWay.BYTES);
        resArr[1] = String.format("%8s", Integer.toBinaryString(symbolsCounter)).replace(' ', '0');
        resArr[2] = binaryString.toString();
        return resArr;
    }

}


