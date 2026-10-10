package educational.dmitriigurylev;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Block {
    private final int[] dataCodewords;
    private final int[] ecCodewords;
}
