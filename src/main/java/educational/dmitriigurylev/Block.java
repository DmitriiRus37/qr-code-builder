package educational.dmitriigurylev;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Block {
    private int[] decimalArr;

    @Setter
    private List<Integer> listCorrectBytes;

    public Block(int[] decimalArr) {
        this.decimalArr = decimalArr;
    }
}
