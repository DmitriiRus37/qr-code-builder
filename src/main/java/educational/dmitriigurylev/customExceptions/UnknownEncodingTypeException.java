package educational.dmitriigurylev.customExceptions;

public class UnknownEncodingTypeException extends RuntimeException {

    public UnknownEncodingTypeException(String message) {
        super(message);
    }
}
