package educational.dmitriigurylev;

import java.util.EnumMap;

public class EncodingHeaderMap {
    private static final EnumMap<EncodingWay, String> encodingWayHeaderMap = new EnumMap<>(EncodingWay.class);
    static {
        encodingWayHeaderMap.put(EncodingWay.DIGITS, "0001");
        encodingWayHeaderMap.put(EncodingWay.LETTERS_DIGITS, "0010");
        encodingWayHeaderMap.put(EncodingWay.BYTES, "0100");
    }

    public static String getFieldSizeByVersion(EncodingWay e) {
        return encodingWayHeaderMap.get(e);
    }

    public enum EncodingWay {
        DIGITS, LETTERS_DIGITS, BYTES
    }
}
