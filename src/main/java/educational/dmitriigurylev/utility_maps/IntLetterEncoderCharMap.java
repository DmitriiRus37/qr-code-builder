package educational.dmitriigurylev.utility_maps;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IntLetterEncoderCharMap {
    private static final Map<Character, Integer> map = Map.ofEntries(
            Map.entry('0', 0),
            Map.entry('1', 1),
            Map.entry('2', 2),
            Map.entry('3', 3),
            Map.entry('4', 4),
            Map.entry('5', 5),
            Map.entry('6', 6),
            Map.entry('7', 7),
            Map.entry('8', 8),
            Map.entry('9', 9),
            Map.entry('A', 10),
            Map.entry('B', 11),
            Map.entry('C', 12),
            Map.entry('D', 13),
            Map.entry('E', 14),
            Map.entry('F', 15),
            Map.entry('G', 16),
            Map.entry('H', 17),
            Map.entry('I', 18),
            Map.entry('J', 19),
            Map.entry('K', 20),
            Map.entry('L', 21),
            Map.entry('M', 22),
            Map.entry('N', 23),
            Map.entry('O', 24),
            Map.entry('P', 25),
            Map.entry('Q', 26),
            Map.entry('R', 27),
            Map.entry('S', 28),
            Map.entry('T', 29),
            Map.entry('U', 30),
            Map.entry('V', 31),
            Map.entry('W', 32),
            Map.entry('X', 33),
            Map.entry('Y', 34),
            Map.entry('Z', 35),
            Map.entry(' ', 36),
            Map.entry('$', 37),
            Map.entry('%', 38),
            Map.entry('*', 39),
            Map.entry('+', 40),
            Map.entry('-', 41),
            Map.entry('.', 42),
            Map.entry('/', 43),
            Map.entry(':', 44));

    public static int getIntByChar(char ch) {
        return map.get(ch);
    }

    public static boolean contains(char ch) {
        return map.containsKey(ch);
    }
}
