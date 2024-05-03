package educational.dmitriigurylev.utility_maps;

import educational.dmitriigurylev.encoders.ByteEncoder;
import educational.dmitriigurylev.encoders.Encoder;
import educational.dmitriigurylev.encoders.IntLetterEncoder;
import educational.dmitriigurylev.encoders.IntegerEncoder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EncoderMap {
    private static final Map<Class, Encoder> map = Map.of(
            Integer.class, new IntegerEncoder(),
            String.class, new IntLetterEncoder(),
            byte[].class, new ByteEncoder());

    public static Encoder getEncoder(Class cl) {
        return map.get(cl);
    }
}
