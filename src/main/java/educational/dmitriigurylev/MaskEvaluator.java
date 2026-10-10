package educational.dmitriigurylev;

import educational.dmitriigurylev.enums.MaskPattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Mask selection by the penalty rules of ISO/IEC 18004 section 7.8.3 on a 0/1 matrix; lower is better.
 * N1: 3 + (length - 5) per row/column run of 5+ same-colour modules. N2: 3 per one-colour 2x2 block (overlaps count).
 * N3: 40 per {@code 1011101} with 4 light modules on at least one side. N4: 10 per full 5 % the dark ratio
 * deviates from 50 %. N3 follows ZXing: light modules on both sides count once, and modules outside the symbol
 * are not light.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MaskEvaluator {

    private static final int N1 = 3;
    private static final int N2 = 3;
    private static final int N3 = 40;
    private static final int N4 = 10;

    private static final int[] FINDER_LIKE = {1, 0, 1, 1, 1, 0, 1};

    /**
     * Tries all 8 masks on copies of {@code qrCodeField} (data placed, not masked yet) and returns the one
     * with the lowest penalty. Each candidate is scored as the final symbol: masked data, format information
     * for that mask and version information. On a tie the lower mask number wins. {@code qrCodeField} is not changed.
     */
    public static MaskPattern chooseBest(QrCodeField qrCodeField) {
        MaskPattern[] masks = MaskPattern.values();
        int[] penalties = new int[masks.length];
        for (MaskPattern mask : masks) {
            QrCodeField candidate = qrCodeField.copy();
            candidate.applyMaskPattern(mask);
            candidate.addInformationTypeBits(mask);
            candidate.addVersionInformation();
            penalties[mask.getNumber()] = penalty(toMatrix(candidate.getField()));
        }
        return lowestPenalty(penalties);
    }

    /** The mask with the lowest penalty ({@code penalties[i]} belongs to mask i); a tie goes to the lower number. */
    static MaskPattern lowestPenalty(int[] penalties) {
        int best = 0;
        for (int i = 1; i < penalties.length; i++) {
            if (penalties[i] < penalties[best]) {
                best = i;
            }
        }
        return MaskPattern.of(best);
    }

    /** Total penalty N1 + N2 + N3 + N4 of a 0/1 matrix. */
    public static int penalty(int[][] matrix) {
        return penaltyRule1(matrix) + penaltyRule2(matrix) + penaltyRule3(matrix) + penaltyRule4(matrix);
    }

    /** N1: runs of 5 or more same-colour modules in a row or column. */
    static int penaltyRule1(int[][] matrix) {
        int penalty = 0;
        int rows = matrix.length;
        int cols = matrix[0].length;
        for (int row = 0; row < rows; row++) {
            int run = 0;
            for (int col = 0; col < cols; col++) {
                run = col > 0 && matrix[row][col] == matrix[row][col - 1] ? run + 1 : 1;
                if (col == cols - 1 || matrix[row][col] != matrix[row][col + 1]) {
                    penalty += runPenalty(run);
                }
            }
        }
        for (int col = 0; col < cols; col++) {
            int run = 0;
            for (int row = 0; row < rows; row++) {
                run = row > 0 && matrix[row][col] == matrix[row - 1][col] ? run + 1 : 1;
                if (row == rows - 1 || matrix[row][col] != matrix[row + 1][col]) {
                    penalty += runPenalty(run);
                }
            }
        }
        return penalty;
    }

    /** N1 score of one run: 3 + (length - 5) for runs of 5 or more, otherwise 0. */
    private static int runPenalty(int run) {
        return run >= 5 ? N1 + (run - 5) : 0;
    }

    /** N2: 2x2 blocks of one colour; an MxN block holds (M-1)(N-1) of them. */
    static int penaltyRule2(int[][] matrix) {
        int blocks = 0;
        for (int row = 0; row < matrix.length - 1; row++) {
            for (int col = 0; col < matrix[0].length - 1; col++) {
                int value = matrix[row][col];
                if (value == matrix[row][col + 1] && value == matrix[row + 1][col] && value == matrix[row + 1][col + 1]) {
                    blocks++;
                }
            }
        }
        return N2 * blocks;
    }

    /** N3: dark-light-dark-dark-dark-light-dark (1:1:3:1:1) with 4 light modules before or after it. */
    static int penaltyRule3(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        int patterns = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (col + 6 < cols && isFinderLike(matrix, row, col, 0, 1)
                        && (isLight(matrix, row, col - 4, 0, 1) || isLight(matrix, row, col + 7, 0, 1))) {
                    patterns++;
                }
                if (row + 6 < rows && isFinderLike(matrix, row, col, 1, 0)
                        && (isLight(matrix, row - 4, col, 1, 0) || isLight(matrix, row + 7, col, 1, 0))) {
                    patterns++;
                }
            }
        }
        return N3 * patterns;
    }

    /** Whether the 7 modules from (row, col) in direction (dRow, dCol) read 1011101. */
    private static boolean isFinderLike(int[][] matrix, int row, int col, int dRow, int dCol) {
        for (int i = 0; i < FINDER_LIKE.length; i++) {
            if (matrix[row + i * dRow][col + i * dCol] != FINDER_LIKE[i]) {
                return false;
            }
        }
        return true;
    }

    /** Whether the 4 modules from (row, col) in direction (dRow, dCol) are light; outside the symbol is not light. */
    private static boolean isLight(int[][] matrix, int row, int col, int dRow, int dCol) {
        for (int i = 0; i < 4; i++) {
            int r = row + i * dRow;
            int c = col + i * dCol;
            if (r < 0 || c < 0 || r >= matrix.length || c >= matrix[0].length || matrix[r][c] == 1) {
                return false;
            }
        }
        return true;
    }

    /** N4: 10 per full 5 % deviation of the dark ratio from 50 %, i.e. floor(|2 * dark - total| * 10 / total). */
    static int penaltyRule4(int[][] matrix) {
        int dark = 0;
        int total = 0;
        for (int[] row : matrix) {
            for (int value : row) {
                dark += value;
                total++;
            }
        }
        return N4 * (Math.abs(2 * dark - total) * 10 / total);
    }

    /** Copies the module colours of the field into a plain 0/1 matrix. */
    static int[][] toMatrix(Cell[][] field) {
        int[][] matrix = new int[field.length][field[0].length];
        for (int row = 0; row < field.length; row++) {
            for (int col = 0; col < field[0].length; col++) {
                matrix[row][col] = field[row][col].getValue();
            }
        }
        return matrix;
    }
}
