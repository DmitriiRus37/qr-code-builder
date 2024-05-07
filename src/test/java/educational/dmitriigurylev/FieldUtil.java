package educational.dmitriigurylev;


public class FieldUtil {

    public static String fieldToString(Cell[][] field) {
        StringBuilder sb = new StringBuilder();
        for (int y=0; y<field.length; y++) {
            for (int x=0; x<field[0].length; x++) {
                sb.append(field[y][x].getValue()).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
