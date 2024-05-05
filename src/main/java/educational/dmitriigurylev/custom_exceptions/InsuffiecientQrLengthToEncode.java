package educational.dmitriigurylev.custom_exceptions;

public class InsuffiecientQrLengthToEncode extends RuntimeException {

    public InsuffiecientQrLengthToEncode(String message) {
        super(message);
    }

    public InsuffiecientQrLengthToEncode() {
    }
}
