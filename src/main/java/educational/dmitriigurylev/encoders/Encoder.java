package educational.dmitriigurylev.encoders;

import educational.dmitriigurylev.enums.Version;

/**
 * Turns a payload into the bits of one QR segment. Implementations are stateless, so one instance can be shared.
 */
public interface Encoder {
    /**
     * @return {mode indicator, character count indicator, data bits...}; the count width depends on {@code version}
     */
    String[] encode(Object value, Version version);
}
