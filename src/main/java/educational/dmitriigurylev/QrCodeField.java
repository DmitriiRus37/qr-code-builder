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

@Getter
public class QrCodeField {

    private final Version version;
    private final CorrectionLevel level;
    private final Cell[][] field;

    public QrCodeField(Version version, CorrectionLevel level) {
        this.version = version;
        this.level = level;
        this.field = new Cell[version.size()][version.size()];
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
        int until = field.length - 9;
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
            int row;
            int col;
            if (i < 8) {
                row = 8;
                col = i;
            } else {
                j += 2;
                row = i - j;
                col = 8;
            }
            if (row == 6 || col == 6) {
                // timing pattern
                continue;
            }
            Cell currentCell = field[row][col];
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
        int[] centers = AlignmentPatternCentersMap.getAlignmentPatternCentersByVersion(version);
        if (centers.length == 0) {
            return;
        }
        int first = centers[0];
        int last = centers[centers.length - 1];

        for (int centerY : centers) {
            for (int centerX : centers) {
                // these three positions overlap the finder patterns
                boolean overlapsFinder = (centerY == first && centerX == first)
                        || (centerY == first && centerX == last)
                        || (centerY == last && centerX == first);
                if (!overlapsFinder) {
                    addAlignmentPattern(centerY, centerX);
                }
            }
        }
    }

    private void addAlignmentPattern(int centerY, int centerX) {
        for (int y = centerY - 2; y <= centerY + 2; y++) {
            for (int x = centerX - 2; x <= centerX + 2; x++) {
                boolean isDark = Math.max(Math.abs(y - centerY), Math.abs(x - centerX)) != 1;
                field[y][x].setValue(isDark ? 1 : 0).setBusy(true);
            }
        }
    }

    /**
     * Marks the format information cells (and, from V7 on, the version information blocks) as busy,
     * so data bits and the mask skip them. The format bits are written later by {@link #addInformationTypeBits()}.
     */
    public void reserveInformationAreas() {
        int size = field.length;
        for (int i = 0; i <= 8; i++) {
            field[8][i].setBusy(true);
            field[i][8].setBusy(true);
        }
        for (int i = size - 8; i < size; i++) {
            field[8][i].setBusy(true);
            field[i][8].setBusy(true);
        }
        if (version.getNumber() >= 7) {
            // 6x3 blocks: rows 0-5 x cols size-11..size-9 (top-right) and the transpose (bottom-left)
            for (int far = size - 11; far < size - 8; far++) {
                for (int near = 0; near < 6; near++) {
                    field[near][far].setBusy(true);
                    field[far][near].setBusy(true);
                }
            }
        }
    }

    /**
     * Writes the 18 version information bits into the two 6x3 blocks reserved by {@link #reserveInformationAreas()}.
     * Only V7 and above carry version information; for V1-V6 this does nothing.
     * Bit i (counted from the least significant bit) goes to row i / 3, column size - 11 + i % 3 of the top-right block,
     * and to the transposed position of the bottom-left block.
     */
    public void addVersionInformation() {
        if (version.getNumber() < 7) {
            return;
        }
        int size = field.length;
        int bits = versionInformationBits(version.getNumber());
        for (int i = 0; i < 18; i++) {
            int bit = (bits >>> i) & 1;
            int far = size - 11 + i % 3;
            int near = i / 3;
            field[near][far].setValue(bit).setBusy(true);   // top-right block
            field[far][near].setValue(bit).setBusy(true);   // bottom-left block
        }
    }

    /**
     * @return 18 bits: the 6-bit version number followed by the 12-bit BCH(18,6) remainder (generator 0x1F25)
     */
    static int versionInformationBits(int versionNumber) {
        int rem = versionNumber;
        for (int i = 0; i < 12; i++) {
            rem = (rem << 1) ^ ((rem >>> 11) * 0x1F25);
        }
        return (versionNumber << 12) | rem;
    }

    private enum FillDirection {UP, DOWN}

}
