package educational.dmitriigurylev.enums;

/** Encoding mode of the payload. */
public enum EncodingWay {
    NUMERIC("numeric"), ALPHANUMERIC("alphanumeric"), BYTE("byte");

    private final String displayName;

    EncodingWay(String displayName) {
        this.displayName = displayName;
    }

    /** The spec's name of the mode: numeric, alphanumeric or byte. */
    public String displayName() {
        return displayName;
    }
}
