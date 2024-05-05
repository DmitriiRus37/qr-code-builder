package educational.dmitriigurylev.utility_maps;

import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.Version;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.EnumMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BlocksCountMap {

    private static final EnumMap<Version, Map<CorrectionLevel, Integer>> map = new EnumMap<>(Version.class);
    static {
        map.put(Version.V_1, addInfoByCorrectionLevel(1, 1, 1, 1));
        map.put(Version.V_2, addInfoByCorrectionLevel(1, 1, 1, 1));
        map.put(Version.V_3, addInfoByCorrectionLevel(1, 1, 2, 2));
        map.put(Version.V_4, addInfoByCorrectionLevel(1, 2, 2, 4));
        map.put(Version.V_5, addInfoByCorrectionLevel(1, 2, 4, 4));
        map.put(Version.V_6, addInfoByCorrectionLevel(2, 4, 4, 4));
        map.put(Version.V_7, addInfoByCorrectionLevel(2,4,6,5));
        map.put(Version.V_8, addInfoByCorrectionLevel(2,4,6,6));
        map.put(Version.V_9, addInfoByCorrectionLevel(2,5,8,8));
        map.put(Version.V_10, addInfoByCorrectionLevel(4,5,8,8));

    }


    private static Map<CorrectionLevel, Integer> addInfoByCorrectionLevel(int low, int middle, int quarter, int high) {
        return Map.of(
                CorrectionLevel.LOW, low,
                CorrectionLevel.MIDDLE, middle,
                CorrectionLevel.QUARTER, quarter,
                CorrectionLevel.HIGH, high);
    }

    public static int getBlocksCountByVersionAndCorrectionLevel(Version v, CorrectionLevel cl) {
        return map.get(v).get(cl);
    }
}
