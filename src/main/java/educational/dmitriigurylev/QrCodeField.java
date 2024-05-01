package educational.dmitriigurylev;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class QrCodeField {

    static Map<Version, Cell[][]> versionFieldMap = createVersionFieldMap();

    Version version;

    @Getter
    Cell[][] field;

    public QrCodeField(Version version) {
        this.version = version;
        this.field = versionFieldMap.get(version);
        fillFieldWithZeros();
    }

    private void fillFieldWithZeros() {
        for (Cell[] cells : field) {
            for (int i = 0; i < cells.length; i++) {
                cells[i] = new Cell(false, 0);
            }
        }
    }

    private static Map<Version, Cell[][]> createVersionFieldMap() {
        return new HashMap<>(){{
            put(Version.ONE, new Cell[21][21]);
        }};
    }

    public void addFinderPatterns() {
        if (version == Version.ONE) {
            addSquareStartingWithLeftUpperPoint(0,0, FinderPatterLocation.UpperLeft);
            addSquareStartingWithLeftUpperPoint(0,14, FinderPatterLocation.UpperRight);
            addSquareStartingWithLeftUpperPoint(14,0, FinderPatterLocation.LowerLeft);
        }
    }

    private void addSquareStartingWithLeftUpperPoint(int firstY, int firstX, FinderPatterLocation location) {
        for (int y=0; y<7; y++) {
            for (int x=0; x<7; x++) {
                if (y == 0 || y == 6) {
                    field[firstY+y][firstX+x].setValue(1);
                } else if (y == 1 || y == 5) {
                    if (x == 0 || x ==6) {
                        field[firstY+y][firstX+x].setValue(1);
                    }
                } else {
                    if (x == 0 || (x >= 2 && x <= 4) || x == 6) {
                        field[firstY+y][firstX+x].setValue(1);
                    }
                }
                field[firstY+y][firstX+x].setBusy(true);
            }
        }
        if (location == FinderPatterLocation.UpperLeft) {
            IntStream.range(0, 8).forEach(x -> field[firstY + 7][firstX + x].setBusy(true));
            IntStream.range(0, 7).forEach(y -> field[firstY + y][firstX + 7].setBusy(true));
        } else if (location == FinderPatterLocation.LowerLeft) {
            IntStream.range(0, 8).forEach(x -> field[firstY - 1][firstX + x].setBusy(true));
            IntStream.range(-1, 7).forEach(y -> field[firstY + y][firstX + 7].setBusy(true));
        } else if (location == FinderPatterLocation.UpperRight) {
            IntStream.range(-1, 7).forEach(x -> field[firstY + 7][firstX + x].setBusy(true));
            IntStream.range(0, 7).forEach(y -> field[firstY + y][firstX - 1].setBusy(true));
        }

    }
}
