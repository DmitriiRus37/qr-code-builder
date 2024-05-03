package educational.dmitriigurylev.utility_maps;

import educational.dmitriigurylev.Cell;
import educational.dmitriigurylev.enums.Version;

import java.util.EnumMap;

public class VersionMap {

    private VersionMap() {}

    private static final EnumMap<Version, Cell[][]> versionFieldSizeMap = new EnumMap<>(Version.class);
    static {
        int size = 21;
        for (Version v : Version.values()) {
            versionFieldSizeMap.put(v, new Cell[size][size]);
            size+=4;
        }
    }

    public static Cell[][] getFieldSizeByVersion(Version v) {
        return versionFieldSizeMap.get(v);
    }
}
