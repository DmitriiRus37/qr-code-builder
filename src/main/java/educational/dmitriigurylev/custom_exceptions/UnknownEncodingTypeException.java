package educational.dmitriigurylev.custom_exceptions;

public class UnknownEncodingTypeException extends RuntimeException {

    public UnknownEncodingTypeException(String message) {
        super(message);
    }

    public UnknownEncodingTypeException() {
    }
}
