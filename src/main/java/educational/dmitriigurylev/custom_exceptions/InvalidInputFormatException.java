package educational.dmitriigurylev.custom_exceptions;

/**
 * The input cannot be encoded (null, empty, unsupported type, illegal character, invalid UTF-16),
 * or a bit-level helper received malformed input.
 */
public class InvalidInputFormatException extends RuntimeException {

    /** Creates the exception with a message that describes the bad input. */
    public InvalidInputFormatException(String message) {
        super(message);
    }
}
