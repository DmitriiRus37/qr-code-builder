package educational.dmitriigurylev;

import educational.dmitriigurylev.QrImageDrawer.Format;
import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.MaskPattern;
import educational.dmitriigurylev.enums.Version;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * The command line of {@link Main}, parsed without side effects (the file system is only read, to see whether
 * {@code -o} names an existing directory). {@link #parse(String...)} throws {@link UsageException} for anything it
 * cannot accept; {@link Main} turns that into exit code 2.
 *
 * @param text      what to encode (never empty)
 * @param level     error correction level
 * @param version   requested version, or null for the smallest that fits
 * @param mask      forced mask pattern 0-7, or null for the one with the lowest penalty
 * @param scale     pixels per module
 * @param quietZone light border in modules
 * @param output    the image file; its extension always matches {@code format}
 * @param format    PNG or SVG
 * @param debug     also write {@code debug_<name>} with the reserved modules
 * @param help      {@code -h}/{@code --help} was given: print {@link #USAGE} and do nothing else
 */
public record CliOptions(String text, CorrectionLevel level, Version version, Integer mask, int scale, int quietZone,
                         Path output, Format format, boolean debug, boolean help) {

    public static final String USAGE = """
            Usage: java -jar qr-code-builder.jar TEXT [options]

            Encodes TEXT as a QR code (versions 1-%1$d) and writes it as a PNG or SVG image.

            Options:
              -l L|M|Q|H   error correction level (default M)
              -v 1..%1$d     version (default: the smallest that fits)
              -m 0..%2$d      mask pattern (default: the one with the lowest penalty)
              -s 1..%3$d     pixels per module (default %4$d)
              -q 0..%5$d     quiet zone in modules (default %6$d)
              -o FILE      output file (default qr.png, or qr.svg with --svg).
                           A .png or .svg extension selects the format; without
                           an extension, .png (or .svg with --svg) is appended.
                           Other extensions, and --svg with a .png file, are
                           errors. An existing directory, or a FILE ending in
                           '/', means qr.png (or qr.svg) inside that directory.
                           Missing directories are created.
              --svg        write SVG instead of PNG
              --debug      also write debug_FILE with the reserved (function)
                           modules in dark
              -h, --help   print this help and exit
              --           end of options: every argument after -- is TEXT
                           (e.g. a TEXT starting with '-'); put options before it

            Exit codes: 0 success; 1 the text cannot be encoded or the file
            cannot be written; 2 usage error.
            """.formatted(Version.values().length, MaskPattern.COUNT - 1,
            QrImageDrawer.MAX_SCALE, QrImageDrawer.DEFAULT_SCALE,
            QrImageDrawer.MAX_QUIET_ZONE, QrImageDrawer.DEFAULT_QUIET_ZONE);

    public static final CorrectionLevel DEFAULT_LEVEL = CorrectionLevel.MIDDLE;
    public static final String DEFAULT_OUTPUT_NAME = "qr";
    /** The options that take a value; the argument after them is never read as an option. */
    private static final Set<String> VALUE_OPTIONS = Set.of("-l", "-v", "-m", "-s", "-q", "-o");

    /** A command line that cannot be accepted; the message says why. */
    public static class UsageException extends RuntimeException {
        public UsageException(String message) {
            super(message);
        }
    }

    /**
     * Parses {@code args}. When {@code -h}/{@code --help} appears as an option (before {@code --} and not as the value
     * of an option such as {@code -o}), the result has {@code help} set and the rest of the line is ignored, even if it
     * has errors. Options may come before or after TEXT; each option may be given at most once. Every argument after
     * {@code --} is TEXT, so there must be at most one.
     *
     * @throws UsageException for unknown options, missing or invalid values, a missing, empty or second TEXT, and a
     *                        conflict between {@code --svg} and the {@code -o} extension
     */
    public static CliOptions parse(String... args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (VALUE_OPTIONS.contains(arg)) {
                i++;
            } else if (arg.equals("--")) {
                break;
            } else if (arg.equals("-h") || arg.equals("--help")) {
                return new CliOptions(null, DEFAULT_LEVEL, null, null, QrImageDrawer.DEFAULT_SCALE,
                        QrImageDrawer.DEFAULT_QUIET_ZONE, null, null, false, true);
            }
        }

        String text = null;
        CorrectionLevel level = DEFAULT_LEVEL;
        Version version = null;
        Integer mask = null;
        int scale = QrImageDrawer.DEFAULT_SCALE;
        int quietZone = QrImageDrawer.DEFAULT_QUIET_ZONE;
        String output = null;
        boolean svg = false;
        boolean debug = false;

        Set<String> seen = new HashSet<>();
        boolean optionsEnded = false;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (optionsEnded || !arg.startsWith("-") || arg.equals("-")) {
                if (text != null && optionsEnded) {
                    throw new UsageException("unexpected argument '" + arg + "' after --: TEXT is already '" + text
                            + "' (put options before --)");
                } else if (text != null) {
                    throw new UsageException("unexpected argument '" + arg + "': TEXT is already '" + text
                            + "' (quote a TEXT that contains spaces)");
                }
                text = arg;
                continue;
            }
            if (arg.equals("--")) {
                optionsEnded = true;
                continue;
            }
            if (!seen.add(arg)) {
                throw new UsageException("option " + arg + " is given more than once");
            }
            switch (arg) {
                case "--svg" -> svg = true;
                case "--debug" -> debug = true;
                case "-l" -> level = parseLevel(value(args, ++i, arg));
                case "-v" -> {
                    int number = parseInt(value(args, ++i, arg), arg, 1, Version.values().length);
                    version = Version.values()[number - 1];
                }
                case "-m" -> mask = parseInt(value(args, ++i, arg), arg, 0, MaskPattern.COUNT - 1);
                case "-s" -> scale = parseInt(value(args, ++i, arg), arg, 1, QrImageDrawer.MAX_SCALE);
                case "-q" -> quietZone = parseInt(value(args, ++i, arg), arg, 0, QrImageDrawer.MAX_QUIET_ZONE);
                case "-o" -> output = value(args, ++i, arg);
                default -> throw new UsageException("unknown option " + arg);
            }
        }
        if (text == null) {
            throw new UsageException("missing TEXT to encode");
        }
        if (text.isEmpty()) {
            throw new UsageException("TEXT is empty: nothing to encode");
        }

        Format format = svg ? Format.SVG : Format.PNG;
        Path path = outputPath(output, format);
        return new CliOptions(text, level, version, mask, scale, quietZone, path, Format.fromFileName(path), debug,
                false);
    }

    /**
     * The output path: the default name; the default name inside {@code -o} when {@code -o} ends in a separator or is
     * an existing directory; {@code -o} as given when it ends in {@code .png}/{@code .svg} (any case); or {@code -o}
     * plus the extension of {@code format} when its file name has no dot at all.
     */
    private static Path outputPath(String output, Format format) {
        String extension = "." + format.name().toLowerCase(Locale.ROOT);
        if (output == null) {
            return Path.of(DEFAULT_OUTPUT_NAME + extension);
        }
        Path path;
        try {
            path = Path.of(output);
        } catch (InvalidPathException e) {
            throw new UsageException("invalid output file '" + output + "': " + e.getReason());
        }
        if (output.isEmpty()) {
            throw new UsageException("invalid output file ''");
        }
        if (output.endsWith("/") || output.endsWith(File.separator) || Files.isDirectory(path)) {
            return path.resolve(DEFAULT_OUTPUT_NAME + extension);
        }
        Path fileName = path.getFileName();
        if (fileName == null) {
            throw new UsageException("invalid output file '" + output + "'");
        }
        String name = fileName.toString();
        if (!name.contains(".")) {
            return path.resolveSibling(name + extension);
        }
        Format byExtension;
        try {
            byExtension = Format.fromFileName(path);
        } catch (IllegalArgumentException e) {
            throw new UsageException("output file must end in .png or .svg, got '" + output + "'");
        }
        if (format == Format.SVG && byExtension == Format.PNG) {
            throw new UsageException("--svg conflicts with the .png output file '" + output + "'");
        }
        return path;
    }

    private static String value(String[] args, int i, String option) {
        if (i >= args.length) {
            throw new UsageException("option " + option + " needs a value");
        }
        return args[i];
    }

    private static CorrectionLevel parseLevel(String value) {
        return switch (value.toUpperCase(Locale.ROOT)) {
            case "L" -> CorrectionLevel.LOW;
            case "M" -> CorrectionLevel.MIDDLE;
            case "Q" -> CorrectionLevel.QUARTER;
            case "H" -> CorrectionLevel.HIGH;
            default -> throw new UsageException("-l must be L, M, Q or H, got '" + value + "'");
        };
    }

    private static int parseInt(String value, String option, int min, int max) {
        int n;
        try {
            n = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new UsageException(option + " must be a number " + min + ".." + max + ", got '" + value + "'");
        }
        if (n < min || n > max) {
            throw new UsageException(option + " must be " + min + ".." + max + ", got " + n);
        }
        return n;
    }

    /** The one-letter name of a level, as {@code -l} takes it. */
    public static String levelLetter(CorrectionLevel level) {
        return switch (level) {
            case LOW -> "L";
            case MIDDLE -> "M";
            case QUARTER -> "Q";
            case HIGH -> "H";
        };
    }
}
