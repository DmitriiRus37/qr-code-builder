package educational.dmitriigurylev;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cell {
    private boolean busy = false;
    private int value = 0;
}
