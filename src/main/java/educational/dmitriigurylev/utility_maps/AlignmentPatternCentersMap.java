package educational.dmitriigurylev.utility_maps;

import educational.dmitriigurylev.enums.Version;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.EnumMap;

/** Row/column coordinates of the alignment pattern centres per version (V1 has none). */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AlignmentPatternCentersMap {
    private static final EnumMap<Version, int[]> map = new EnumMap<>(Version.class);
    static {
        map.put(Version.V_1, new int[]{});
        map.put(Version.V_2, new int[]{6,18});
        map.put(Version.V_3, new int[]{6,22});
        map.put(Version.V_4, new int[]{6,26});
        map.put(Version.V_5, new int[]{6,30});
        map.put(Version.V_6, new int[]{6,34});
        map.put(Version.V_7, new int[]{6,22,38});
        map.put(Version.V_8, new int[]{6,24,42});
        map.put(Version.V_9, new int[]{6,26,46});
        map.put(Version.V_10, new int[]{6,28,50});
    }

    /** The centre coordinates; patterns sit at every row/column combination except over the finders. */
    public static int[] getAlignmentPatternCentersByVersion(Version e) {
        return map.get(e);
    }

}
