package educational.dmitriigurylev.utility_maps;

import java.util.HashMap;
import java.util.Map;

public class GeneratingPolynomialMap {

    private GeneratingPolynomialMap() {}

    private static final Map<Integer, int[]> map = new HashMap<>();
    static {
        map.put(17, new int[]{43, 139, 206, 78, 43, 239, 123, 206, 214, 147, 24, 99, 150, 39, 243, 163, 136});
    }

    public static int[] getGeneratingPolynomial(int i) {
        return map.get(i);
    }
}


