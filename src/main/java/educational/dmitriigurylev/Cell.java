package educational.dmitriigurylev;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** One module of the QR matrix: its colour and whether a pattern already reserved it. */
@Getter
@AllArgsConstructor
public class Cell {
    /**
     * True if the module belongs to a function pattern or the reserved format/version areas; data modules stay
     * false, so placement skips busy cells and the mask applies only to non-busy ones.
     */
    private boolean busy;
    /** Module colour: 1 = dark, 0 = light. */
    private int value;

    /** Marks the module as occupied (or free) and returns this cell for chaining. */
    public Cell setBusy(boolean busy) {
        this.busy = busy;
        return this;
    }

    /** Sets the module colour (1 = dark, 0 = light) and returns this cell for chaining. */
    public Cell setValue(int value) {
        this.value = value;
        return this;
    }

}
