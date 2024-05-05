package educational.dmitriigurylev.utility_maps;

import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.Version;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.EnumMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CorrectionBytesPerBlockMap {
    private static final EnumMap<Version, Map<CorrectionLevel, Integer>> map = new EnumMap<>(Version.class);
    static {
        map.put(Version.V_1, addInformationByCorrectionLevel(7, 10, 13, 17));
        map.put(Version.V_2, addInformationByCorrectionLevel(10,16,22,28));
        map.put(Version.V_3, addInformationByCorrectionLevel(15,26,18,22));
        map.put(Version.V_4, addInformationByCorrectionLevel(20,18,26,16));
        map.put(Version.V_5, addInformationByCorrectionLevel(26,24,18,22));
        map.put(Version.V_6, addInformationByCorrectionLevel(18,16,24,28));
        map.put(Version.V_7, addInformationByCorrectionLevel(20,18,18,26));
        map.put(Version.V_8, addInformationByCorrectionLevel(24,22,22,26));
        map.put(Version.V_9, addInformationByCorrectionLevel(30,22,20,24));
        map.put(Version.V_10, addInformationByCorrectionLevel(18,26,24,28));
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

    public static int getCorrectionBytesSizeByVersionAndCorrectionLevel(Version v, CorrectionLevel cl) {
        return map.get(v).get(cl);
    }
}
