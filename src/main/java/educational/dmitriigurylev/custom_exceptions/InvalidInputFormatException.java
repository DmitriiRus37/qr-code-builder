package educational.dmitriigurylev.custom_exceptions;

public class InvalidInputFormatException extends RuntimeException {

    public InvalidInputFormatException(String message) {
        super(message);
    }

    public InvalidInputFormatException() {
    }
}
