package educational.dmitriigurylev;

import java.util.HashMap;
import java.util.Map;

public class QrCodeField {

    static Map<Version, int[][]> versionFieldMap = createVersionFieldMap();

    Version version;

    int[][] field;

    public QrCodeField(Version version) {
        this.version = version;
        this.field = versionFieldMap.get(version);
    }

    private static Map<Version,int[][]> createVersionFieldMap() {
        return new HashMap<>(){{
            put(Version.ONE, new int[21][21]);
        }};
    }
}
