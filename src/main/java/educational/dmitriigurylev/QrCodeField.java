package educational.dmitriigurylev;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class QrCodeField {

    static Map<Version, Cell[][]> versionFieldMap = createVersionFieldMap();

    private Version version;

    @Getter
    private Cell[][] field;

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

    public void addSynchronizationLines() {
        for (int y=7; y<14; y++) {
            if (y == 8 || y == 10 || y == 12) {
                field[6][y].setValue(1);
            }
            field[6][y].setBusy(true);
        }
        for (int x=7; x<14; x++) {
            if (x == 8 || x == 10 || x == 12) {
                field[x][6].setValue(1);
            }
            field[x][6].setBusy(true);
        }
    }

    public void addTypeInformationBits() {
        String code = "001110011100111";
        StringBuilder typeInformationBits = new StringBuilder(code);
        int i=-1;
        int j=-2;
//        TODO разбить цикл на 2 части, чтобы писать меньше условий
        while (!typeInformationBits.isEmpty()) {
            i++;
            Cell currentCell;
            if (i < 8) {
                currentCell = field[8][i];
            } else {
                j+=2;
                currentCell = field[i-j][8];
            }

            if (currentCell.isBusy()) {
                continue;
            }
            char curChar = typeInformationBits.charAt(0);
            typeInformationBits.deleteCharAt(0);
            if (curChar == '1') {
                currentCell.setValue(1);
            }
            currentCell.setBusy(true);
        }

        i=-1;
        j=-2;
        typeInformationBits = new StringBuilder(code);
//        TODO разбить цикл на 2 части, чтобы писать меньше условий
        while (!typeInformationBits.isEmpty()) {
            i++;
            Cell currentCell;
            if (i < 7) {
                currentCell = field[field.length-i-1][8];
            } else {
                j+=2;
                currentCell = field[8][field[0].length-1-i+j];
            }
            char curChar = typeInformationBits.charAt(0);
            typeInformationBits.deleteCharAt(0);
            if (curChar == '1') {
                currentCell.setValue(1);
            }
            currentCell.setBusy(true);
            if (i == 7) {
                field[field.length-i-1][8].setValue(1).setBusy(true);
            }
        }
    }

}
