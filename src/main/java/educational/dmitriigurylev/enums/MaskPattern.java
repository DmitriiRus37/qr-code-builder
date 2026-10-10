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

    /** The 3-bit mask reference written into the format information. */
    private final int number;

    MaskPattern(int number) {
        this.number = number;
    }

    /** @return true if the module at (row, col) is inverted by this mask */
    public abstract boolean inverts(int row, int col);

    /** @throws IllegalArgumentException if {@code number} is not 0-7 */
    public static MaskPattern of(int number) {
        if (number < 0 || number >= BY_NUMBER.length) {
            throw new IllegalArgumentException("Mask pattern must be 0-7, got " + number);
        }
        return BY_NUMBER[number];
    }
}
