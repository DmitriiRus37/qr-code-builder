package educational.dmitriigurylev.encoders;

import educational.dmitriigurylev.enums.Version;

public interface Encoder {
    String[] transformToBinaryArray();
    Encoder setValueAndVersion(Object ob, Version version);
}
