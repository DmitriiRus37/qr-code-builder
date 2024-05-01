package educational.dmitriigurylev;

import java.util.HashMap;
import java.util.Map;

public class QrCodeField {

    static Map<Version, int[][]> versionFieldMap = createVersionFieldMap();

    Version version;

    public int[][] getField() {
        return field;
    }

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

    public void addFinderPatterns() {
        if (version == Version.ONE) {
            addSquareStartingWithLeftUpperPoint(0,0);
            addSquareStartingWithLeftUpperPoint(0,14);
            addSquareStartingWithLeftUpperPoint(14,0);
        }
    }

    private void addSquareStartingWithLeftUpperPoint(int row, int column) {
        field[row][column] = 1;
        field[row][++column] = 1;
        field[row][++column] = 1;
        field[row][++column] = 1;
        field[row][++column] = 1;
        field[row][++column] = 1;
        field[row][++column] = 1;

        field[++row][column] = 1;
        field[++row][column] = 1;
        field[++row][column] = 1;
        field[++row][column] = 1;
        field[++row][column] = 1;
        field[++row][column] = 1;

        field[row][--column] = 1;
        field[row][--column] = 1;
        field[row][--column] = 1;
        field[row][--column] = 1;
        field[row][--column] = 1;
        field[row][--column] = 1;

        field[--row][column] = 1;
        field[--row][column] = 1;
        field[--row][column] = 1;
        field[--row][column] = 1;
        field[--row][column] = 1;
        field[--row][column] = 1;

        row += 2;
        column += 2;
        field[row][column] = 1;
        field[row][++column] = 1;
        field[row][++column] = 1;

        field[++row][column] = 1;
        field[++row][column] = 1;

        field[row][--column] = 1;
        field[row][--column] = 1;

        field[--row][column] = 1;

        field[row][++column] = 1;
    }
}
