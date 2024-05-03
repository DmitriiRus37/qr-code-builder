package educational.dmitriigurylev.utility_maps;

import educational.dmitriigurylev.Cell;
import educational.dmitriigurylev.enums.Version;

import java.util.EnumMap;

public class VersionMap {

    private VersionMap() {}

    private static final EnumMap<Version, Cell[][]> versionFieldSizeMap = new EnumMap<>(Version.class);
    static {
        versionFieldSizeMap.put(Version.V_1, new Cell[21][21]);
        versionFieldSizeMap.put(Version.V_2, new Cell[25][25]);
        versionFieldSizeMap.put(Version.V_3, new Cell[29][29]);
        versionFieldSizeMap.put(Version.V_4, new Cell[33][33]);
        versionFieldSizeMap.put(Version.V_5, new Cell[37][37]);
        versionFieldSizeMap.put(Version.V_6, new Cell[41][41]);
    }

    public static Cell[][] getFieldSizeByVersion(Version v) {
        return versionFieldSizeMap.get(v);
    }
}
