package educational.dmitriigurylev;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Cell {
    private boolean busy = false;
    private int value = 0;

    public Cell setBusy(boolean busy) {
        this.busy = busy;
        return this;
    }

    public Cell setValue(int value) {
        this.value = value;
        return this;
    }

}
