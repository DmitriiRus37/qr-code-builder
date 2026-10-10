package educational.dmitriigurylev.encoders;

import educational.dmitriigurylev.UtilityMethods;
import educational.dmitriigurylev.custom_exceptions.InvalidInputFormatException;
import educational.dmitriigurylev.enums.EncodingWay;
import educational.dmitriigurylev.enums.Version;
import educational.dmitriigurylev.utility_maps.CharacterCountIndicatorMap;
import educational.dmitriigurylev.utility_maps.EncodingModeIndicatorMap;
import lombok.NoArgsConstructor;

import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@NoArgsConstructor
public class ByteEncoder extends AbstractEncoder implements Encoder {

    @Override
    public Encoder setValueAndVersion(Object obj, Version version) {
        // Strings are encoded as UTF-8 (no ECI header): practically every reader detects it
        if (obj instanceof String str) {
            this.value = toUtf8(str);
        } else if (obj instanceof byte[] bytes) {
            this.value = bytes.clone();
        } else {
            throw new InvalidInputFormatException();
        }
        this.version = version;
        return this;
    }

    @Override
    public String[] transformToBinaryArray() {
        return byteArrayToBinaryArray((byte[]) this.value);
    }

    /** Strict UTF-8: a lone surrogate is an error instead of a silent '?' (String.getBytes would replace it). */
    private static byte[] toUtf8(String str) {
        try {
            var buffer = StandardCharsets.UTF_8.newEncoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .encode(CharBuffer.wrap(str));
            return Arrays.copyOfRange(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.arrayOffset() + buffer.limit());
        } catch (CharacterCodingException e) {
            throw new InvalidInputFormatException("not valid UTF-16 text (lone surrogate?): " + e.getMessage());
        }
    }

    private String[] byteArrayToBinaryArray(byte[] arr) {
        String[] resArr = new String[3];
        var binaryString = new StringBuilder();
        for (byte b : arr) {
            // always 8 bits per byte, leading zeros included
            binaryString.append(UtilityMethods.toBits(b & 0xFF, 8));
        }
        resArr[0] = EncodingModeIndicatorMap.getFieldSizeByVersion(EncodingWay.BYTES);

        // character count = number of bytes, not chars ("Хаб" is 6 bytes)
        resArr[1] = UtilityMethods.characterCountToBits(
                arr.length,
                CharacterCountIndicatorMap.getCharacterCountIndicatorByVersionAndEncodingWay(version, EncodingWay.BYTES));

        resArr[2] = binaryString.toString();
        return resArr;
    }

}
