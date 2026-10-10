package educational.dmitriigurylev.reed_solomon_mapping;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Arithmetic tables of GF(256) as used by QR codes: elements are bytes, α = 2,
 * reduction by the primitive polynomial x^8 + x^4 + x^3 + x^2 + 1 (0x11D).
 * The tables are computed instead of typed in, so they cannot contain typos.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GaloisField {

    private static final int PRIMITIVE_POLYNOMIAL = 0x11D;
    private static final int[] EXP = new int[256];
    private static final int[] LOG = new int[256];

    static {
        int x = 1;
        for (int i = 0; i < 255; i++) {
            EXP[i] = x;
            LOG[x] = i;
            x <<= 1;
            if (x > 255) {
                x ^= PRIMITIVE_POLYNOMIAL;
            }
        }
        EXP[255] = EXP[0]; // α^255 = α^0 = 1
    }

    /** α^power, for power in 0..255. */
    public static int exp(int power) {
        return EXP[power];
    }

    /** The power p with α^p = value, for value in 1..255 (log(0) is undefined). */
    public static int log(int value) {
        if (value == 0) {
            throw new IllegalArgumentException("log(0) is undefined in GF(256)");
        }
        return LOG[value];
    }
}
