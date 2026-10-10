package educational.dmitriigurylev;


/** Formats a module matrix as text for comparisons with the stored snapshots. */
public class FieldUtil {

    /** One line per row, each module value followed by a space. */
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
