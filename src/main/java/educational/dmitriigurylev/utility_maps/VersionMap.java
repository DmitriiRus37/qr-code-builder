package educational.dmitriigurylev.utility_maps;

import educational.dmitriigurylev.Cell;
import educational.dmitriigurylev.enums.Version;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.EnumMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VersionMap {
    private static final EnumMap<Version, Cell[][]> map = new EnumMap<>(Version.class);
    static {
        int size = 21;
        for (Version v : Version.values()) {
            map.put(v, new Cell[size][size]);
            size+=4;
        }
    }

    public static Cell[][] getFieldSizeByVersion(Version v) {
        return map.get(v);
    }
}
