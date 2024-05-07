package educational.dmitriigurylev;

import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.FinderPatterLocation;
import educational.dmitriigurylev.enums.Version;
import educational.dmitriigurylev.utility_maps.AlignmentPatternCentersMap;
import educational.dmitriigurylev.utility_maps.CorrectionLevelAndMaskCodeMap;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static educational.dmitriigurylev.utility_maps.VersionMap.*;

@Getter
public class QrCodeField {

    private final Version version;
    private final CorrectionLevel level;
    private final Cell[][] field;

    public QrCodeField(Version version, CorrectionLevel level) {
        this.version = version;
        this.level = level;
        this.field = getFieldSizeByVersion(version);
        fillFieldWithZeros();
    }

    private void fillFieldWithZeros() {
        for (Cell[] cells : field) {
            for (int i = 0; i < cells.length; i++) {
                cells[i] = new Cell(false, 0);
            }
        }
    }

    public void addFinderPatterns() {
        addSquareStartingWithLeftUpperPoint(0,0, FinderPatterLocation.UPPER_LEFT);
        addSquareStartingWithLeftUpperPoint(0, field[0].length - 7, FinderPatterLocation.UPPER_RIGHT);
        addSquareStartingWithLeftUpperPoint(field.length-7,0, FinderPatterLocation.LOWER_LEFT);
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
        if (location == FinderPatterLocation.UPPER_LEFT) {
            IntStream.range(0, 8).forEach(x -> field[firstY + 7][firstX + x].setBusy(true));
            IntStream.range(0, 7).forEach(y -> field[firstY + y][firstX + 7].setBusy(true));
        } else if (location == FinderPatterLocation.LOWER_LEFT) {
            IntStream.range(0, 8).forEach(x -> field[firstY - 1][firstX + x].setBusy(true));
            IntStream.range(-1, 7).forEach(y -> field[firstY + y][firstX + 7].setBusy(true));
        } else if (location == FinderPatterLocation.UPPER_RIGHT) {
            IntStream.range(-1, 7).forEach(x -> field[firstY + 7][firstX + x].setBusy(true));
            IntStream.range(0, 7).forEach(y -> field[firstY + y][firstX - 1].setBusy(true));
        }
    }

    public void addSynchronizationLines() {
        int until = switch (version) {
            case V_1 -> 12;
            case V_2 -> 16;
            case V_3 -> 20;
            case V_4 -> 24;
            case V_5 -> 28;
            case V_6 -> 32;
            case V_7 -> 36;
            case V_8 -> 40;
            case V_9 -> 44;
            case V_10 -> 48;
        };
        boolean isOneHere = true;
        for (int y=8; y<=until; y++) {
            field[6][y].setValue(isOneHere ? 1 : 0).setBusy(true);
            isOneHere = !isOneHere;
        }
        isOneHere = true;
        for (int x=8; x<=until; x++) {
            field[x][6].setValue(isOneHere ? 1 : 0).setBusy(true);
            isOneHere = !isOneHere;
        }
    }

    public void addInformationTypeBits() {
        List<Character> collected = CorrectionLevelAndMaskCodeMap.getMaskAndCorrectionLevelCode(level, (byte) 2)
                .chars()
                .mapToObj(c -> (char) c).collect(Collectors.toList());
        List<Character> typeInformationBits = new LinkedList<>(collected);

        int i = -1;
        int j = -2;
        while (!typeInformationBits.isEmpty()) {
            i++;
            Cell currentCell;
            if (i < 8) {
                currentCell = field[8][i];
            } else {
                j += 2;
                currentCell = field[i - j][8];
            }
            if (currentCell.isBusy()) {
                continue;
            }
            currentCell.setValue(typeInformationBits.remove(0) == '1' ? 1 : 0).setBusy(true);
        }

        i = -1;
        j = -2;
        typeInformationBits = collected;
        while (!typeInformationBits.isEmpty()) {
            i++;
            Cell currentCell;
            if (i < 7) {
                currentCell = field[field.length - i - 1][8];
            } else {
                j += 2;
                currentCell = field[8][field[0].length - 1 - i + j];
            }
            currentCell.setValue(typeInformationBits.remove(0) == '1' ? 1 : 0).setBusy(true);
            if (i == 7) {
                field[field.length - i - 1][8].setValue(1).setBusy(true);
            }
        }
    }

    public void fillFieldWithBitsSequence(StringBuilder sb) {
        Cell[][] f = field;
        FillDirection dir = FillDirection.UP;
        int x = f[0].length - 1;
        int y = f.length - 1;

        while (!sb.isEmpty()) {
            while (dir == FillDirection.UP) {
                fillCell(sb, f, x, y);
                x--;

                fillCell(sb, f, x, y);
                x++;
                y--;
                if (y < 0) {
                    dir = FillDirection.DOWN;
                    y++;
                    x-=2;
                    if (x == 6) {
                        x--;
                    }
                }
            }

            while (dir == FillDirection.DOWN) {
                fillCell(sb, f, x, y);
                x--;

                fillCell(sb, f, x, y);
                x++;
                y++;
                if (y >= f.length) {
                    dir = FillDirection.UP;
                    y--;
                    x-=2;
                    if (x == 6) {
                        x--;
                    }
                }
            }
        }
    }

    private void fillCell(StringBuilder sb, Cell[][] f, int x, int y) {
        if (!f[y][x].isBusy() && !sb.isEmpty()) {
            f[y][x].setValue(sb.charAt(0) == '0' ? 0 : 1);
            sb.deleteCharAt(0);
        }
    }

    public void applyMaskPattern() {
        for (int x=0; x<field[0].length; x++) {
            if (x % 3 == 0) {
                for (Cell[] cells : field) {
                    if (cells[x].isBusy()) {
                        continue;
                    }
                    cells[x].setValue(cells[x].getValue() == 1 ? 0 : 1);
                }
            }
        }
    }

    public void addAlignmentPatterns() {
//        TODO тут нужны будут доработки
        int[] centers = AlignmentPatternCentersMap.getAlignmentPatternCentersByVersion(version);

        for (int cntr : centers) {
            for (int y = cntr-2; y < cntr+3; y++) {
                for (int x = cntr-2; x < cntr+3; x++) {
                    field[y][x].setBusy(true);
                    if ((x == cntr - 2 || x == cntr + 2) || (y == cntr - 2 || y == cntr + 2) || (x == cntr && y == cntr)) {
                        field[y][x].setValue(1);
                    }
                }
            }
        }
    }

    private enum FillDirection {UP, DOWN}

}
