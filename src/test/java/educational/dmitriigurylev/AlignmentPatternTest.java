package educational.dmitriigurylev;

import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.Version;
import educational.dmitriigurylev.utility_maps.AlignmentPatternCentersMap;
import educational.dmitriigurylev.utility_maps.CorrectionLevelAndMaskCodeMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Alignment patterns, finder patterns with separators, format and version areas, and the reserved-module count. */
class AlignmentPatternTest {

    /** Number of alignment patterns per version V1..V10 (ISO/IEC 18004 table E.1). */
    private static final int[] PATTERN_COUNT = {0, 1, 1, 1, 1, 1, 6, 6, 6, 6};

    @Test
    void centresFollowSpec() {
        assertArrayEquals(new int[]{}, AlignmentPatternCentersMap.getAlignmentPatternCentersByVersion(Version.V_1));
        assertArrayEquals(new int[]{6, 18}, AlignmentPatternCentersMap.getAlignmentPatternCentersByVersion(Version.V_2));
        assertArrayEquals(new int[]{6, 22}, AlignmentPatternCentersMap.getAlignmentPatternCentersByVersion(Version.V_3));
        assertArrayEquals(new int[]{6, 26}, AlignmentPatternCentersMap.getAlignmentPatternCentersByVersion(Version.V_4));
        assertArrayEquals(new int[]{6, 30}, AlignmentPatternCentersMap.getAlignmentPatternCentersByVersion(Version.V_5));
        assertArrayEquals(new int[]{6, 34}, AlignmentPatternCentersMap.getAlignmentPatternCentersByVersion(Version.V_6));
        assertArrayEquals(new int[]{6, 22, 38}, AlignmentPatternCentersMap.getAlignmentPatternCentersByVersion(Version.V_7));
        assertArrayEquals(new int[]{6, 24, 42}, AlignmentPatternCentersMap.getAlignmentPatternCentersByVersion(Version.V_8));
        assertArrayEquals(new int[]{6, 26, 46}, AlignmentPatternCentersMap.getAlignmentPatternCentersByVersion(Version.V_9));
        assertArrayEquals(new int[]{6, 28, 50}, AlignmentPatternCentersMap.getAlignmentPatternCentersByVersion(Version.V_10));
    }

    @Test
    void version1HasNoAlignmentPattern() {
        assertOnlyAlignmentPatternsAt(Version.V_1, List.of());
    }

    @Test
    void version2HasOnePatternAt18x18() {
        assertOnlyAlignmentPatternsAt(Version.V_2, List.of(new int[]{18, 18}));
    }

    @Test
    void version7HasSixPatterns() {
        assertOnlyAlignmentPatternsAt(Version.V_7, List.of(
                new int[]{6, 22},
                new int[]{22, 6}, new int[]{22, 22}, new int[]{22, 38},
                new int[]{38, 22}, new int[]{38, 38}));
    }

    @ParameterizedTest
    @EnumSource(Version.class)
    void functionPatternsAreIntactAfterCreateQr(Version version) {
        for (CorrectionLevel level : CorrectionLevel.values()) {
            QrCreator creator = new QrCreator(version, level, "HELLO").createQr();
            Cell[][] field = creator.getQrCodeField().getField();
            int size = field.length;

            assertFinderWithSeparator(field, 0, 0, 7, 7);
            assertFinderWithSeparator(field, 0, size - 7, 7, size - 8);
            assertFinderWithSeparator(field, size - 7, 0, size - 8, 7);

            for (int i = 8; i <= size - 9; i++) {
                int expected = i % 2 == 0 ? 1 : 0;
                assertEquals(expected, field[6][i].getValue(), "timing row 6, col " + i);
                assertEquals(expected, field[i][6].getValue(), "timing col 6, row " + i);
            }

            List<int[]> centres = expectedCentres(version);
            assertEquals(PATTERN_COUNT[version.getNumber() - 1], centres.size(), "pattern count");
            for (int[] centre : centres) {
                assertAlignmentPattern(field, centre[0], centre[1]);
            }

            // the format information names the mask that was chosen
            assertFormatInformation(field, CorrectionLevelAndMaskCodeMap.getMaskAndCorrectionLevelCode(
                    level, (byte) creator.getMaskPattern().getNumber()));
        }
    }

    @ParameterizedTest
    @EnumSource(Version.class)
    void reservedAreasLeaveExactlyTheRawDataModules(Version version) {
        var qrCodeField = new QrCodeField(version, CorrectionLevel.HIGH);
        qrCodeField.addFinderPatterns();
        qrCodeField.addSynchronizationLines();
        qrCodeField.addAlignmentPatterns();
        qrCodeField.reserveInformationAreas();

        int free = 0;
        for (Cell[] row : qrCodeField.getField()) {
            for (Cell cell : row) {
                if (!cell.isBusy()) {
                    free++;
                }
            }
        }
        assertEquals(SpecVectorsTest.rawDataModules(version.getNumber()), free);
    }

    @ParameterizedTest
    @EnumSource(value = Version.class, names = {"V_7", "V_8", "V_9", "V_10"})
    void versionInformationAreasAreReservedFromV7(Version version) {
        Cell[][] field = new QrCreator(version, CorrectionLevel.HIGH, "HELLO").createQr().getQrCodeField().getField();
        int size = field.length;
        int reserved = 0;
        for (int row = 0; row < 6; row++) {
            for (int col = size - 11; col <= size - 9; col++) {
                assertTrue(field[row][col].isBusy(), "top-right version cell (" + row + "," + col + ")");
                assertTrue(field[col][row].isBusy(), "bottom-left version cell (" + col + "," + row + ")");
                reserved += 2;
            }
        }
        assertEquals(36, reserved);
    }

    /** On a fresh field, only the alignment patterns at {@code centres} are drawn and reserved. */
    private static void assertOnlyAlignmentPatternsAt(Version version, List<int[]> centres) {
        var qrCodeField = new QrCodeField(version, CorrectionLevel.HIGH);
        qrCodeField.addAlignmentPatterns();
        Cell[][] field = qrCodeField.getField();

        int busy = 0;
        for (Cell[] row : field) {
            for (Cell cell : row) {
                if (cell.isBusy()) {
                    busy++;
                }
            }
        }
        assertEquals(25 * centres.size(), busy, "busy modules");
        for (int[] centre : centres) {
            assertAlignmentPattern(field, centre[0], centre[1]);
        }
    }

    /** Every combination of the spec centres except the three that overlap the finder patterns. */
    private static List<int[]> expectedCentres(Version version) {
        int[] c = AlignmentPatternCentersMap.getAlignmentPatternCentersByVersion(version);
        List<int[]> result = new ArrayList<>();
        for (int y : c) {
            for (int x : c) {
                boolean finder = (y == c[0] && x == c[0]) || (y == c[0] && x == c[c.length - 1]) || (y == c[c.length - 1] && x == c[0]);
                if (!finder) {
                    result.add(new int[]{y, x});
                }
            }
        }
        return result;
    }

    /** Checks one 5x5 alignment pattern (dark ring, light ring, dark centre) and that it is reserved. */
    private static void assertAlignmentPattern(Cell[][] field, int centreY, int centreX) {
        for (int dy = -2; dy <= 2; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                int ring = Math.max(Math.abs(dy), Math.abs(dx));
                Cell cell = field[centreY + dy][centreX + dx];
                String at = "alignment (" + centreY + "," + centreX + ") module (" + (centreY + dy) + "," + (centreX + dx) + ")";
                assertTrue(cell.isBusy(), at);
                assertEquals(ring == 1 ? 0 : 1, cell.getValue(), at);
            }
        }
    }

    /** Checks the 7x7 finder at (top, left) and its light separator row/column. */
    private static void assertFinderWithSeparator(Cell[][] field, int top, int left, int separatorRow, int separatorCol) {
        for (int dy = 0; dy < 7; dy++) {
            for (int dx = 0; dx < 7; dx++) {
                int ring = Math.max(Math.abs(dy - 3), Math.abs(dx - 3));
                assertEquals(ring == 2 ? 0 : 1, field[top + dy][left + dx].getValue(),
                        "finder module (" + (top + dy) + "," + (left + dx) + ")");
            }
        }
        int size = field.length;
        int colFrom = left == 0 ? 0 : size - 8;
        for (int x = colFrom; x < colFrom + 8; x++) {
            assertEquals(0, field[separatorRow][x].getValue(), "separator (" + separatorRow + "," + x + ")");
        }
        int rowFrom = top == 0 ? 0 : size - 8;
        for (int y = rowFrom; y < rowFrom + 8; y++) {
            assertEquals(0, field[y][separatorCol].getValue(), "separator (" + y + "," + separatorCol + ")");
        }
    }

    /** Checks both copies of the 15 format bits and the dark module. */
    private static void assertFormatInformation(Cell[][] field, String bits) {
        int size = field.length;
        int[][] firstCopy = {
                {8, 0}, {8, 1}, {8, 2}, {8, 3}, {8, 4}, {8, 5}, {8, 7}, {8, 8},
                {7, 8}, {5, 8}, {4, 8}, {3, 8}, {2, 8}, {1, 8}, {0, 8}};
        int[][] secondCopy = new int[15][];
        for (int i = 0; i < 7; i++) {
            secondCopy[i] = new int[]{size - 1 - i, 8};
        }
        for (int i = 7; i < 15; i++) {
            secondCopy[i] = new int[]{8, size - 15 + i};
        }
        for (int i = 0; i < 15; i++) {
            int expected = bits.charAt(i) - '0';
            assertEquals(expected, field[firstCopy[i][0]][firstCopy[i][1]].getValue(), "format bit " + i + " (top-left)");
            assertEquals(expected, field[secondCopy[i][0]][secondCopy[i][1]].getValue(), "format bit " + i + " (split)");
        }
        assertEquals(1, field[size - 8][8].getValue(), "dark module");
    }
}
