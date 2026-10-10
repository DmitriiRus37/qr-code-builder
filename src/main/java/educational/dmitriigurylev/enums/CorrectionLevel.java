package educational.dmitriigurylev.enums;

import java.util.Locale;
import java.util.Objects;

/** Error correction level: roughly 7 % (L), 15 % (M), 25 % (Q) or 30 % (H) of the codewords can be restored. */
public enum CorrectionLevel {
    HIGH("H"), QUARTILE("Q"), MEDIUM("M"), LOW("L");

    private final String letter;

    CorrectionLevel(String letter) {
        this.letter = letter;
    }

    /** The one-letter name of the level: L, M, Q or H. */
    public String letter() {
        return letter;
    }

    /**
     * The level named by {@code letter} (L, M, Q or H, any case).
     *
     * @throws NullPointerException     if {@code letter} is null
     * @throws IllegalArgumentException for any other string
     */
    public static CorrectionLevel fromLetter(String letter) {
        String upper = Objects.requireNonNull(letter, "letter").toUpperCase(Locale.ROOT);
        for (CorrectionLevel level : values()) {
            if (level.letter.equals(upper)) {
                return level;
            }
        }
        throw new IllegalArgumentException("not a correction level letter: '" + letter + "'");
    }
}
