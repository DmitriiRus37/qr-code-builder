package educational.dmitriigurylev.encoders;

import educational.dmitriigurylev.UtilityMethods;
import educational.dmitriigurylev.custom_exceptions.InvalidInputFormatException;
import educational.dmitriigurylev.enums.EncodingWay;
import educational.dmitriigurylev.enums.Version;
import educational.dmitriigurylev.utility_maps.CharacterCountIndicatorMap;
import educational.dmitriigurylev.utility_maps.EncodingModeIndicatorMap;

import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** Byte mode: every byte of the payload is written as 8 bits. */
public class ByteEncoder implements Encoder {

    /**
     * {@inheritDoc}
     *
     * @throws InvalidInputFormatException if {@code obj} is not a {@code byte[]}
     */
    @Override
    public String[] encode(Object obj, Version version) {
        // QrCreator passes the bytes; text was already turned into UTF-8 there (see toUtf8)
        if (!(obj instanceof byte[] bytes)) {
            throw new InvalidInputFormatException("byte mode takes a byte[], got "
                    + (obj == null ? "null" : obj.getClass().getName()));
        }
        return byteArrayToBinaryArray(bytes, version);
    }

    /**
     * Encodes text as strict UTF-8 for byte mode (no ECI header: practically every reader detects it).
     * A lone surrogate is an error instead of a silent '?' (String.getBytes would replace it).
     *
     * @throws InvalidInputFormatException if {@code str} is not valid UTF-16
     */
    public static byte[] toUtf8(String str) {
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

    /** Builds {mode indicator, byte count, data bits} for the given bytes. */
    private static String[] byteArrayToBinaryArray(byte[] arr, Version version) {
        String[] resArr = new String[3];
        var binaryString = new StringBuilder();
        for (byte b : arr) {
            // always 8 bits per byte, leading zeros included
            binaryString.append(UtilityMethods.toBits(b & 0xFF, 8));
        }
        resArr[0] = EncodingModeIndicatorMap.getModeIndicator(EncodingWay.BYTE);

        // character count = number of bytes, not chars ("Хаб" is 6 bytes)
        resArr[1] = UtilityMethods.characterCountToBits(
                arr.length,
                CharacterCountIndicatorMap.getCharacterCountIndicatorByVersionAndEncodingWay(version, EncodingWay.BYTE));

        resArr[2] = binaryString.toString();
        return resArr;
    }

}
