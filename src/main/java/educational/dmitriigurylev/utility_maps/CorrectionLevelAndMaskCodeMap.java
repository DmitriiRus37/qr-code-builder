package educational.dmitriigurylev.utility_maps;

import educational.dmitriigurylev.enums.CorrectionLevel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.EnumMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CorrectionLevelAndMaskCodeMap {
    private static final EnumMap<CorrectionLevel, Map<Byte, String>> map = new EnumMap<>(CorrectionLevel.class);
    static {
        map.put(CorrectionLevel.LOW, Map.of(
                (byte) 0, "111011111000100",
                (byte) 1, "111001011110011",
                (byte) 2, "111110110101010",
                (byte) 3, "111100010011101",
                (byte) 4, "110011000101111",
                (byte) 5, "110001100011000",
                (byte) 6, "110110001000001",
                (byte) 7, "110100101110110"));

        map.put(CorrectionLevel.MIDDLE, Map.of(
                (byte) 0, "101010000010010",
                (byte) 1, "101000100100101",
                (byte) 2, "101111001111100",
                (byte) 3, "101101101001011",
                (byte) 4, "100010111111001",
                (byte) 5, "100000011001110",
                (byte) 6, "100111110010111",
                (byte) 7, "100101010100000"));

        map.put(CorrectionLevel.QUARTER, Map.of(
                (byte) 0, "011010101011111",
                (byte) 1, "011000001101000",
                (byte) 2, "011111100110001",
                (byte) 3, "011101000000110",
                (byte) 4, "010010010110100",
                (byte) 5, "010000110000011",
                (byte) 6, "010111011011010",
                (byte) 7, "010101111101101"));

        map.put(CorrectionLevel.HIGH, Map.of(
                (byte) 0, "001011010001001",
                (byte) 1, "001001110111110",
                (byte) 2, "001110011100111",
                (byte) 3, "001100111010000",
                (byte) 4, "000011101100010",
                (byte) 5, "000001001010101",
                (byte) 6, "000110100001100",
                (byte) 7, "000100000111011"));
    }

    public static String getMaskAndCorrectionLevelCode(CorrectionLevel cl, byte m) {
        return map.get(cl).get(m);
    }

}
