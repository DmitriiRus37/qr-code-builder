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
        map.put(Version.V_1, addInformationByCorrectionLevel(152, 128, 104, 72));
        map.put(Version.V_2, addInformationByCorrectionLevel(272, 224, 176, 128));
        map.put(Version.V_3, addInformationByCorrectionLevel(440, 352, 272, 208));
        map.put(Version.V_4, addInformationByCorrectionLevel(640, 512, 384, 288));
        map.put(Version.V_5, addInformationByCorrectionLevel(864, 688, 496, 368));
        map.put(Version.V_6, addInformationByCorrectionLevel(1088, 864, 608, 480));
        map.put(Version.V_7, addInformationByCorrectionLevel(1248, 992, 704, 528));
        map.put(Version.V_8, addInformationByCorrectionLevel(1552, 1232, 880, 688));
        map.put(Version.V_9, addInformationByCorrectionLevel(1856, 1456, 1056, 800));
        map.put(Version.V_10, addInformationByCorrectionLevel(2192, 1728, 1232, 976));
    }


    private static Map<CorrectionLevel, Integer> addInformationByCorrectionLevel(
            int low,
            int middle,
            int quarter,
            int high) {
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
