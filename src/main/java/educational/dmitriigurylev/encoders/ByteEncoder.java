package educational.dmitriigurylev.encoders;

import educational.dmitriigurylev.UtilityMethods;
import educational.dmitriigurylev.enums.EncodingWay;
import educational.dmitriigurylev.utility_maps.EncodingHeaderMap;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ByteEncoder implements Encoder {

    private final byte[] value;
    public ByteEncoder(byte[] value) {
        this.value = value;
    }


    @Override
    public int[] encodeSymbols() {
        String[] binaryArr = byteArrayToBinaryArray(this.value);
        String bitString = UtilityMethods.binaryArrayToBitString(binaryArr);
        return UtilityMethods.binaryStringToDecimalArray(bitString);
    }

    private static String[] byteArrayToBinaryArray(byte[] arr) {
        String[] resArr = new String[3];
        int symbolsCounter = arr.length;
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


