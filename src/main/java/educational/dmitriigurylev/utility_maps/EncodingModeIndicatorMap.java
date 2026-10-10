package educational.dmitriigurylev.utility_maps;

import educational.dmitriigurylev.enums.EncodingWay;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.EnumMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EncodingModeIndicatorMap {
    private static final EnumMap<EncodingWay, String> map = new EnumMap<>(EncodingWay.class);
    static {
        map.put(EncodingWay.NUMERIC, "0001");
        map.put(EncodingWay.ALPHANUMERIC, "0010");
        map.put(EncodingWay.BYTE, "0100");
    }

    public static String getModeIndicator(EncodingWay e) {
        return map.get(e);
    }

}
