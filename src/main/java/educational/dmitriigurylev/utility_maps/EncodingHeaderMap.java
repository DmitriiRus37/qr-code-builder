package educational.dmitriigurylev.utility_maps;

import educational.dmitriigurylev.enums.EncodingWay;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.EnumMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EncodingHeaderMap {
    private static final EnumMap<EncodingWay, String> map = new EnumMap<>(EncodingWay.class);
    static {
        map.put(EncodingWay.DIGITS, "0001");
        map.put(EncodingWay.LETTERS_DIGITS, "0010");
        map.put(EncodingWay.BYTES, "0100");
    }

    public static String getFieldSizeByVersion(EncodingWay e) {
        return map.get(e);
    }

}
