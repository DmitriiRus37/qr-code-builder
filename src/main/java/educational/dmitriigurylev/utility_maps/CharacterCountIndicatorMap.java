package educational.dmitriigurylev.utility_maps;

import educational.dmitriigurylev.enums.EncodingWay;
import educational.dmitriigurylev.enums.Version;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.EnumMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CharacterCountIndicatorMap {

    private static final EnumMap<Version, Map<EncodingWay, Integer>> map = new EnumMap<>(Version.class);

    static {
        int digitSize_1_9 = 10;
        int letterDigitSize_1_9 = 9;
        int byteSize_1_9 = 8;

        int digitSize_10_26 = 12;
        int letterDigitSize_10_26 = 11;
        int byteSize_10_26 = 16;

//        int digitSize_27_40 = 14;
//        int letterDigitSize_27_40 = 13;
//        int byteSize_27_40 = 16;

        int currentDigitSize = digitSize_1_9;
        int currentLetterDigitSize = letterDigitSize_1_9;
        int currentByteSize = byteSize_1_9;

        for (Version v : Version.values()) {
            if (v == Version.V_10) {
                 currentDigitSize = digitSize_10_26;
                 currentLetterDigitSize = letterDigitSize_10_26;
                 currentByteSize = byteSize_10_26;
            }
//            else if (v == Version.V_27) {
//                currentDigitSize = digitSize_27_40;
//                currentLetterDigitSize = letterDigitSize_27_40;
//                currentByteSize = byteSize_27_40;
//            }
            map.put(v, Map.of(
                    EncodingWay.DIGITS, currentDigitSize,
                    EncodingWay.LETTERS_DIGITS, currentLetterDigitSize,
                    EncodingWay.BYTES, currentByteSize
                    ));
        }
    }

    public static int getCharacterCountIndicatorByVersionAndEncodingWay(Version v, EncodingWay ew) {
        return map.get(v).get(ew);
    }
}
