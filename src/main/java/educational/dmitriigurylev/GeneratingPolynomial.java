package educational.dmitriigurylev;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GeneratingPolynomial {

    public static Map<Integer, int[]> map = fillMap();

    private static Map<Integer,int[]> fillMap() {
        return Collections.unmodifiableMap(new HashMap<>() {{
            put(17, new int[]{43, 139, 206, 78, 43, 239, 123, 206, 214, 147, 24, 99, 150, 39, 243, 163, 136});
        }});
    }

}


