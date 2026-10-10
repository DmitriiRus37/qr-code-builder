package educational.dmitriigurylev;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Cell {
    private boolean busy;
    private int value;

    public Cell setBusy(boolean busy) {
        this.busy = busy;
        return this;
    }

    public Cell setValue(int value) {
        this.value = value;
        return this;
    }

}
