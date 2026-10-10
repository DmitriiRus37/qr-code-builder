package educational.dmitriigurylev;

import educational.dmitriigurylev.custom_exceptions.InsufficientCapacityException;
import educational.dmitriigurylev.custom_exceptions.InvalidInputFormatException;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Command line entry point: {@code java -jar target/qr-code-builder.jar "HELLO WORLD" -l Q -o out/qr.svg}.
 * See {@link CliOptions#USAGE} for the options.
 */
public class Main {
    public static final int EXIT_OK = 0;
    public static final int EXIT_FAILURE = 1;
    public static final int EXIT_USAGE = 2;

    public static void main(String[] args) {
        int code = run(args, System.out, System.err);
        if (code != EXIT_OK) {
            System.exit(code);
        }
    }

    /**
     * Does everything {@link #main} does except exiting: parses {@code args}, encodes the text and writes the image.
     * Errors the user can fix are reported as one line on {@code err} (usage errors followed by the usage text),
     * never as a stack trace.
     *
     * @return {@link #EXIT_OK}, {@link #EXIT_FAILURE} (the text does not fit, or the file cannot be written)
     * or {@link #EXIT_USAGE}
     */
    static int run(String[] args, PrintStream out, PrintStream err) {
        CliOptions options;
        try {
            options = CliOptions.parse(args);
        } catch (CliOptions.UsageException e) {
            err.println("error: " + e.getMessage());
            err.println();
            err.print(CliOptions.USAGE);
            return EXIT_USAGE;
        }
        if (options.help()) {
            out.print(CliOptions.USAGE);
            return EXIT_OK;
        }

        try {
            QrCreator creator = QrCreator.of(options.text(), options.level());
            if (options.version() != null) {
                creator.version(options.version());
            }
            if (options.mask() != null) {
                creator.forceMask(options.mask());
            }
            creator.createQr();

            Path output = options.output();
            createParentDirectories(output);
            creator.drawer()
                    .setScale(options.scale())
                    .setQuietZone(options.quietZone())
                    .setDebug(options.debug())
                    .write(output, options.format());

            out.println("wrote " + output + " (V" + creator.getVersion().getNumber() + "-"
                    + creator.getCorrectionLevel().letter() + ", "
                    + creator.getEncodingWay().displayName() + " mode, mask " + creator.getMaskPattern().getNumber() + ")");
            if (options.debug()) {
                out.println("wrote " + QrImageDrawer.debugPath(output));
            }
            return EXIT_OK;
        } catch (InsufficientCapacityException | InvalidInputFormatException e) {
            err.println("error: cannot encode the text: " + e.getMessage());
            return EXIT_FAILURE;
        } catch (UncheckedIOException e) {
            err.println("error: " + e.getMessage() + ": " + describe(e.getCause()));
            return EXIT_FAILURE;
        }
    }

    private static void createParentDirectories(Path output) {
        Path parent = output.toAbsolutePath().getParent();
        try {
            Files.createDirectories(parent);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot create directory " + parent, e);
        }
    }

    /** NIO exceptions often carry only the path as their message, so the class name says what went wrong. */
    private static String describe(IOException e) {
        return e.getClass().getSimpleName() + (e.getMessage() == null ? "" : " " + e.getMessage());
    }
}
