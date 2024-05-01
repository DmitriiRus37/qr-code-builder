package educational.dmitriigurylev;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Cell {
    private boolean busy = false;

    public Cell setBusy(boolean busy) {
        this.busy = busy;
        return this;
    }

    public Cell setValue(int value) {
        this.value = value;
        return this;
    }

    private int value = 0;
}
