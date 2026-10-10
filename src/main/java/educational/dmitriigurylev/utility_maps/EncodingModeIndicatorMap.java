package educational.dmitriigurylev.utility_maps;

import educational.dmitriigurylev.enums.EncodingWay;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.EnumMap;

/** The 4-bit mode indicator that starts each segment (0001 numeric, 0010 alphanumeric, 0100 byte). */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EncodingModeIndicatorMap {
    private static final EnumMap<EncodingWay, String> map = new EnumMap<>(EncodingWay.class);
    static {
        map.put(EncodingWay.NUMERIC, "0001");
        map.put(EncodingWay.ALPHANUMERIC, "0010");
        map.put(EncodingWay.BYTE, "0100");
    }

    /** The mode indicator bits for mode {@code e}. */
    public static String getModeIndicator(EncodingWay e) {
        return map.get(e);
    }

}
