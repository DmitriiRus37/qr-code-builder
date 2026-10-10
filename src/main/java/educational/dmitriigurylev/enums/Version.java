package educational.dmitriigurylev.enums;

import lombok.Getter;

/** Supported QR versions 1-10; the version fixes the symbol size and the data capacity. */
@Getter
public enum Version {
    V_1(1), V_2(2), V_3(3), V_4(4), V_5(5), V_6(6), V_7(7), V_8(8), V_9(9), V_10(10);

    private final int number;

    /** Binds the constant to its version number. */
    Version(int number) {
        this.number = number;
    }

    /** Side length in modules: 17 + 4 * version (21 for V1 ... 57 for V10). */
    public int size() {
        return 17 + 4 * number;
    }
}
