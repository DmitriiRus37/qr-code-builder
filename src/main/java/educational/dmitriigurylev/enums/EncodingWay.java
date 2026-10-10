package educational.dmitriigurylev.enums;

/** Encoding mode of the payload (the data segment's mode indicator is chosen from it). */
public enum EncodingWay {
    NUMERIC("numeric"), ALPHANUMERIC("alphanumeric"), BYTE("byte");

    private final String displayName;

    /** Binds the mode to its display name. */
    EncodingWay(String displayName) {
        this.displayName = displayName;
    }

    /** The spec's name of the mode: numeric, alphanumeric or byte. */
    public String displayName() {
        return displayName;
    }
}
