package educational.dmitriigurylev;

import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.MaskPattern;
import educational.dmitriigurylev.enums.Version;
import educational.dmitriigurylev.utility_maps.CorrectionLevelAndMaskCodeMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Penalty rules on small hand-made matrices; every expected score is computed by hand in the comment next to it.
 * The cross-check against ZXing's mask choice on real symbols is in {@link MaskSelectionTest}.
 */
class MaskEvaluatorTest {

    /** One string per row, '1' = dark. */
    private static int[][] matrix(String... rows) {
        int[][] m = new int[rows.length][];
        for (int r = 0; r < rows.length; r++) {
            m[r] = rows[r].chars().map(c -> c - '0').toArray();
        }
        return m;
    }

    private static int[][] filled(int rows, int cols, int value) {
        int[][] m = new int[rows][cols];
        for (int[] row : m) {
            Arrays.fill(row, value);
        }
        return m;
    }

    /** A 10x10 matrix whose first {@code dark} modules (row by row) are dark. */
    private static int[][] withDarkCount(int dark) {
        int[][] m = new int[10][10];
        for (int i = 0; i < dark; i++) {
            m[i / 10][i % 10] = 1;
        }
        return m;
    }

    @ParameterizedTest
    @CsvSource({
            "1111,         0",   // run of 4: no penalty
            "11111,        3",   // run of 5: 3
            "0000000,      5",   // run of 7: 3 + 2
            "11111011111,  6",   // two runs of 5: 3 + 3 (the single 0 is a run of 1)
            "1111100000,   6",   // dark run of 5 and light run of 5
            "0101010101,   0"})
    void rule1SingleRow(String row, int expected) {
        // one row: every column is a run of 1, so only the horizontal run counts
        assertEquals(expected, MaskEvaluator.penaltyRule1(matrix(row)));
    }

    @Test
    void rule1CountsColumnsToo() {
        // a column of 6 dark modules: 3 + 1 = 4; each row is a run of 1
        assertEquals(4, MaskEvaluator.penaltyRule1(matrix("1", "1", "1", "1", "1", "1")));
        // 5x5 all dark: 5 rows x 3 + 5 columns x 3
        assertEquals(30, MaskEvaluator.penaltyRule1(filled(5, 5, 1)));
    }

    @Test
    void rule2CountsOverlappingTwoByTwoBlocks() {
        assertEquals(3, MaskEvaluator.penaltyRule2(filled(2, 2, 0)));       // one block
        assertEquals(12, MaskEvaluator.penaltyRule2(filled(3, 3, 1)));      // 3x3 holds (3-1)(3-1) = 4 blocks
        assertEquals(18, MaskEvaluator.penaltyRule2(filled(3, 4, 0)));      // (3-1)(4-1) = 6 blocks
        assertEquals(3, MaskEvaluator.penaltyRule2(matrix("110", "110")));  // only the left block is one colour
        assertEquals(0, MaskEvaluator.penaltyRule2(matrix("10", "11")));    // mixed colours
        assertEquals(0, MaskEvaluator.penaltyRule2(matrix("0101", "1010", "0101", "1010")));
    }

    @ParameterizedTest
    @CsvSource({
            "10111010000,      40",  // light modules after
            "00001011101,      40",  // light modules before
            "000010111010000,  40",  // light on both sides: still one pattern (ZXing's reading)
            "1011101,          0",   // no room for the light modules; outside the symbol does not count as light
            "10111010001,      0",   // only 3 light modules after
            "10001011101,      0",   // only 3 light modules before
            "00001011100000,   0",   // 1011100: not the 1:1:3:1:1 pattern
            "0000101110100001011101, 80"})  // two patterns: positions 4 (light before) and 15 (light before)
    void rule3SingleRow(String row, int expected) {
        assertEquals(expected, MaskEvaluator.penaltyRule3(matrix(row)));
    }

    @Test
    void rule3CountsColumnsToo() {
        String[] column = "10111010000".split("");
        assertEquals(40, MaskEvaluator.penaltyRule3(matrix(column)));
    }

    @ParameterizedTest
    @CsvSource({
            "50, 0",    // exactly 50 %
            "54, 0",    // 4 % off: no full 5 % step
            "55, 10",   // 5 % off: one step
            "45, 10",
            "39, 20",   // 11 % off: two full steps
            "100, 100", // 50 % off: ten steps
            "0, 100"})
    void rule4DarkRatio(int dark, int expected) {
        assertEquals(expected, MaskEvaluator.penaltyRule4(withDarkCount(dark)));
    }

    @Test
    void penaltyIsTheSumOfAllRules() {
        // 2x2 all dark: N1 0 (runs of 2), N2 3 (one block), N3 0, N4 |8 - 4| * 10 / 4 = 10 steps -> 100
        int[][] m = filled(2, 2, 1);
        assertEquals(0, MaskEvaluator.penaltyRule1(m));
        assertEquals(3, MaskEvaluator.penaltyRule2(m));
        assertEquals(0, MaskEvaluator.penaltyRule3(m));
        assertEquals(100, MaskEvaluator.penaltyRule4(m));
        assertEquals(103, MaskEvaluator.penalty(m));
    }

    @Test
    void tieGoesToTheLowerMaskNumber() {
        assertEquals(MaskPattern.M_1, MaskEvaluator.lowestPenalty(new int[]{50, 30, 30, 40, 30, 90, 90, 90}));
        assertEquals(MaskPattern.M_0, MaskEvaluator.lowestPenalty(new int[]{7, 7, 7, 7, 7, 7, 7, 7}));
        assertEquals(MaskPattern.M_7, MaskEvaluator.lowestPenalty(new int[]{9, 9, 9, 9, 9, 9, 9, 8}));
    }

    @Test
    void chooseBestDoesNotChangeTheField() {
        QrCodeField qrCodeField = new QrCodeField(Version.V_2, CorrectionLevel.MEDIUM);
        qrCodeField.addFinderPatterns();
        qrCodeField.addSynchronizationLines();
        qrCodeField.addAlignmentPatterns();
        qrCodeField.reserveInformationAreas();
        qrCodeField.fillFieldWithBitsSequence(new StringBuilder("1011".repeat(80)));
        String before = FieldUtil.fieldToString(qrCodeField.getField());

        MaskEvaluator.chooseBest(qrCodeField);

        assertEquals(before, FieldUtil.fieldToString(qrCodeField.getField()));
    }

    /** Expected: one character per mask 0-7, '1' if the module is inverted; computed by hand from the table in the plan. */
    @ParameterizedTest
    @CsvSource({
            "0, 0, 11111111",   // every condition is 0 == 0
            "1, 1, 10001010",   // 0: 2%2=0  1: 1%2=1  2: 1%3=1  3: 2%3=2  4: (0+0)%2=0  5: 1+1=2  6: (1+1)%2=0  7: (0+1)%2=1
            "2, 3, 01101110",   // 0: 5%2=1  1: 2%2=0  2: 3%3=0  3: 5%3=2  4: (1+1)%2=0  5: 0+0=0  6: 0  7: (1+0)%2=1
            "3, 4, 00001110"})  // 0: 7%2=1  1: 3%2=1  2: 4%3=1  3: 7%3=1  4: (1+1)%2=0  5: 0+0=0  6: 0  7: (1+0)%2=1
    void maskConditionsFollowTheSpec(int row, int col, String expected) {
        StringBuilder actual = new StringBuilder();
        for (MaskPattern mask : MaskPattern.values()) {
            actual.append(mask.inverts(row, col) ? '1' : '0');
        }
        assertEquals(expected, actual.toString());
    }

    @Test
    void maskNumberOutOfRangeIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> MaskPattern.of(8));
        assertThrows(IllegalArgumentException.class, () -> MaskPattern.of(-1));
        assertThrows(IllegalArgumentException.class, () -> new QrCreator(Version.V_1, CorrectionLevel.HIGH, "A").forceMask(8));
    }

    /** The second call must write the new mask's bits into both copies, not consume a shared list. */
    @Test
    void informationTypeBitsCanBeRewritten() {
        QrCodeField qrCodeField = new QrCodeField(Version.V_1, CorrectionLevel.LOW);
        qrCodeField.addFinderPatterns();
        qrCodeField.addSynchronizationLines();
        qrCodeField.reserveInformationAreas();
        qrCodeField.addInformationTypeBits(MaskPattern.M_2);
        qrCodeField.addInformationTypeBits(MaskPattern.M_5);

        Cell[][] field = qrCodeField.getField();
        int size = field.length;
        String expected = CorrectionLevelAndMaskCodeMap.getMaskAndCorrectionLevelCode(CorrectionLevel.LOW, (byte) 5);
        StringBuilder first = new StringBuilder();
        for (int col = 0; col <= 8; col++) {
            if (col != 6) {
                first.append(field[8][col].getValue());
            }
        }
        for (int row = 7; row >= 0; row--) {
            if (row != 6) {
                first.append(field[row][8].getValue());
            }
        }
        StringBuilder second = new StringBuilder();
        for (int row = size - 1; row >= size - 7; row--) {
            second.append(field[row][8].getValue());
        }
        for (int col = size - 8; col < size; col++) {
            second.append(field[8][col].getValue());
        }
        assertEquals(expected, first.toString(), "copy around the top-left finder");
        assertEquals(expected, second.toString(), "copy split between bottom-left and top-right");
        assertEquals(1, field[size - 8][8].getValue(), "dark module");
    }
}
