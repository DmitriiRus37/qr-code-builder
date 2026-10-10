package educational.dmitriigurylev.custom_exceptions;

/** The payload does not fit the requested version (or, without one, the largest supported version). */
public class InsufficientCapacityException extends RuntimeException {

    /** Creates the exception with the given message. */
    public InsufficientCapacityException(String message) {
        super(message);
    }
}
