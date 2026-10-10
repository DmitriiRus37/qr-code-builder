package educational.dmitriigurylev.enums;

import lombok.Getter;

/**
 * The 8 data mask patterns of ISO/IEC 18004 (section 7.8.2). A data or EC module at
 * (row, col) is inverted when the pattern's condition is true for it. Function patterns are never masked.
 */
@Getter
public enum MaskPattern {
    M_0(0) {
        @Override
        public boolean inverts(int row, int col) {
            return (row + col) % 2 == 0;
        }
    },
    M_1(1) {
        @Override
        public boolean inverts(int row, int col) {
            return row % 2 == 0;
        }
    },
    M_2(2) {
        @Override
        public boolean inverts(int row, int col) {
            return col % 3 == 0;
        }
    },
    M_3(3) {
        @Override
        public boolean inverts(int row, int col) {
            return (row + col) % 3 == 0;
        }
    },
    M_4(4) {
        @Override
        public boolean inverts(int row, int col) {
            return (row / 2 + col / 3) % 2 == 0;
        }
    },
    M_5(5) {
        @Override
        public boolean inverts(int row, int col) {
            return (row * col) % 2 + (row * col) % 3 == 0;
        }
    },
    M_6(6) {
        @Override
        public boolean inverts(int row, int col) {
            return ((row * col) % 2 + (row * col) % 3) % 2 == 0;
        }
    },
    M_7(7) {
        @Override
        public boolean inverts(int row, int col) {
            return ((row + col) % 2 + (row * col) % 3) % 2 == 0;
        }
    };

    private static final MaskPattern[] BY_NUMBER = values();
    /** Number of mask patterns; valid numbers are 0 to {@code COUNT - 1}. */
    public static final int COUNT = BY_NUMBER.length;

    /** The 3-bit mask reference written into the format information. */
    private final int number;

    /** Binds the pattern to its 3-bit mask reference. */
    MaskPattern(int number) {
        this.number = number;
    }

    /** Whether this mask inverts the module at (row, col). */
    public abstract boolean inverts(int row, int col);

    /**
     * The mask pattern with the given reference number.
     *
     * @throws IllegalArgumentException if {@code number} is not 0-7
     */
    public static MaskPattern of(int number) {
        if (number < 0 || number >= COUNT) {
            throw new IllegalArgumentException("Mask pattern must be 0-" + (COUNT - 1) + ", got " + number);
        }
        return BY_NUMBER[number];
    }
}
