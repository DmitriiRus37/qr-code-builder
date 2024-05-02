package educational.dmitriigurylev.reedSolomonMapping;

import java.util.*;
import java.util.stream.IntStream;

public class AB_Map {
    private final Map<Integer, Integer> map;

    private static AB_Map instance;

    public static AB_Map getMapInstance() {
        if (instance == null) {
            instance = new AB_Map();
        }
        return instance;
    }

    public int getVal(int a) {
        return instance.map.get(a);
    }

    private AB_Map() {
        Queue<Integer> q = new LinkedList<>(List.of(
                0,1,25,2,50,26,198,3,223,51,238,27,104,199,75,
                4,100,224,14,52,141,239,129,28,193,105,248,200,8,76,113,
                5,138,101,47,225,36,15,33,53,147,142,218,240,18,130,69,
                29,181,194,125,106,39,249,185,201,154,9,120,77,228,114,166,
                6,191,139,98,102,221,48,253,226,152,37,1179,16,145,34,136,
                54,208,148,206,143,150,219,189,241,210,19,92,131,56,70,64,
                30,66,182,163,195,72,126,110,107,58,40,84,250,133,186,61,
                202,94,155,159,10,21,121,43,78,212,229,172,115,243,167,87,
                7,112,192,247,140,128,99,13,103,74,222,237,49,197,254,24,
                227,165,153,119,38,184,180,124,17,68,146,217,35,32,137,46
        ));
        map = new HashMap<>();
        IntStream.range(1, 256).forEach(i -> map.put(i, q.isEmpty() ? Integer.valueOf(i) : q.poll()));
    }

}
