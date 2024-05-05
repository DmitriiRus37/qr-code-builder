package educational.dmitriigurylev.utility_maps;

import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.Version;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.EnumMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InformationBitSizeMap {
    private static final EnumMap<Version, Map<CorrectionLevel, Integer>> map = new EnumMap<>(Version.class);
    static {
        map.put(Version.V_1, addInfoByCorrectionLevel(152, 128, 104, 72));
        map.put(Version.V_2, addInfoByCorrectionLevel(272, 224, 176, 128));
        map.put(Version.V_3, addInfoByCorrectionLevel(440, 352, 272, 208));
        map.put(Version.V_4, addInfoByCorrectionLevel(640, 512, 384, 288));
        map.put(Version.V_5, addInfoByCorrectionLevel(864, 688, 496, 368));
        map.put(Version.V_6, addInfoByCorrectionLevel(1088, 864, 608, 480));
        map.put(Version.V_7, addInfoByCorrectionLevel(1248, 992, 704, 528));
        map.put(Version.V_8, addInfoByCorrectionLevel(1552, 1232, 880, 688));
        map.put(Version.V_9, addInfoByCorrectionLevel(1856, 1456, 1056, 800));
        map.put(Version.V_10, addInfoByCorrectionLevel(2192, 1728, 1232, 976));
    }


    private static Map<CorrectionLevel, Integer> addInfoByCorrectionLevel(int low, int middle, int quarter, int high) {
        return Map.of(
                CorrectionLevel.LOW, low,
                CorrectionLevel.MIDDLE, middle,
                CorrectionLevel.QUARTER, quarter,
                CorrectionLevel.HIGH, high);
    }

    public static int getInformationBitsSizeByVersionAndCorrectionLevel(Version v, CorrectionLevel cl) {
        return map.get(v).get(cl);
    }
}
