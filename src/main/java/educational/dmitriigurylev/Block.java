package educational.dmitriigurylev;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** One Reed-Solomon block: its data codewords and the error correction codewords computed from them. */
@Getter
@AllArgsConstructor
public class Block {
    private final int[] dataCodewords;
    /** Error correction codewords: the remainder of data(x)·x^n divided by the generator g(x). */
    private final int[] ecCodewords;
}
