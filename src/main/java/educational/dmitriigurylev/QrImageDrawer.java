package educational.dmitriigurylev;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Renders a {@link QrCodeField} as a PNG image or as SVG.
 * <pre>{@code new QrImageDrawer(field).setScale(8).write(Path.of("out/qr.png"));}</pre>
 * Every module becomes a {@code scale} × {@code scale} square, and the symbol is surrounded by a light quiet zone
 * of {@code quietZone} modules on each side; the spec requires at least 4, which is the default.
 * The image side is therefore {@code (N + 2 * quietZone) * scale} pixels for a symbol of N × N modules.
 * With {@link #setDebug(boolean)} a second image next to the main one ({@code debug_<name>}) shows the reserved
 * (function pattern) modules in dark.
 */
public class QrImageDrawer {

    public static final int DEFAULT_SCALE = 10;
    public static final int DEFAULT_QUIET_ZONE = 4;
    /** Upper bounds that keep a V10 image below 7 000 × 7 000 pixels. */
    public static final int MAX_SCALE = 50;
    public static final int MAX_QUIET_ZONE = 40;

    public enum Format {
        /** Lossless raster image, written by {@link ImageIO}. */
        PNG,
        /** Vector image: one {@code <rect>} per horizontal run of dark modules. */
        SVG;

        /** {@code .png} or {@code .svg} (any case); anything else is rejected. */
        public static Format fromFileName(Path path) {
            String name = fileName(path).toLowerCase(Locale.ROOT);
            if (name.equals(".png") || name.equals(".svg")) {
                throw new IllegalArgumentException("file name is only an extension: " + path);
            } else if (name.endsWith(".png")) {
                return PNG;
            } else if (name.endsWith(".svg")) {
                return SVG;
            }
            throw new IllegalArgumentException("unsupported image format: " + path + " (use .png or .svg)");
        }
    }

    private final QrCodeField qrCodeField;
    private int scale = DEFAULT_SCALE;
    private int quietZone = DEFAULT_QUIET_ZONE;
    private boolean isDebug = false;

    public QrImageDrawer(QrCodeField qrCodeField) {
        this.qrCodeField = Objects.requireNonNull(qrCodeField, "qrCodeField");
    }

    /** Pixels per module side, 1 to {@value #MAX_SCALE}; for SVG it sets the width and height attributes. */
    public QrImageDrawer setScale(int scale) {
        if (scale < 1 || scale > MAX_SCALE) {
            throw new IllegalArgumentException("scale must be 1.." + MAX_SCALE + " pixels per module, got " + scale);
        }
        this.scale = scale;
        return this;
    }

    /** Light border in modules on each side, 0 to {@value #MAX_QUIET_ZONE}; the spec requires at least 4. */
    public QrImageDrawer setQuietZone(int quietZone) {
        if (quietZone < 0 || quietZone > MAX_QUIET_ZONE) {
            throw new IllegalArgumentException("quiet zone must be 0.." + MAX_QUIET_ZONE + " modules, got " + quietZone);
        }
        this.quietZone = quietZone;
        return this;
    }

    /** Also writes {@code debug_<name>} next to the image, with the reserved modules in dark. */
    public QrImageDrawer setDebug(boolean isDebug) {
        this.isDebug = isDebug;
        return this;
    }

    /** Writes the image in the format given by the file extension ({@code .png} or {@code .svg}). */
    public void write(Path path) {
        write(path, Format.fromFileName(path));
    }

    /**
     * Writes the image to {@code path} (the directory must exist), plus the debug image if enabled.
     *
     * @throws UncheckedIOException if a file cannot be written
     */
    public void write(Path path, Format format) {
        Objects.requireNonNull(format, "format");
        writeOne(path, format, QrImageDrawer::isDark);
        if (isDebug) {
            writeOne(debugPath(path), format, Cell::isBusy);
        }
    }

    /** {@code dir/qr.png} → {@code dir/debug_qr.png}. */
    public static Path debugPath(Path path) {
        return path.resolveSibling("debug_" + fileName(path));
    }

    /** The symbol as a 1-bit image (dark = black, light = white). */
    public BufferedImage toImage() {
        return toImage(QrImageDrawer::isDark);
    }

    /** The symbol as a standalone SVG document; the {@code viewBox} is in modules, quiet zone included. */
    public String toSvg() {
        return toSvg(QrImageDrawer::isDark);
    }

    private void writeOne(Path path, Format format, Predicate<Cell> dark) {
        try {
            switch (format) {
                case PNG -> {
                    try (OutputStream out = Files.newOutputStream(path)) {
                        if (!ImageIO.write(toImage(dark), "png", out)) {
                            throw new IOException("no PNG writer available");
                        }
                    }
                }
                case SVG -> Files.writeString(path, toSvg(dark), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("cannot write " + path, e);
        }
    }

    private BufferedImage toImage(Predicate<Cell> dark) {
        Cell[][] field = qrCodeField.getField();
        int side = (field.length + 2 * quietZone) * scale;
        BufferedImage image = new BufferedImage(side, side, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g = image.createGraphics();
        try {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, side, side);
            g.setColor(Color.BLACK);
            for (int row = 0; row < field.length; row++) {
                for (int col = 0; col < field[row].length; col++) {
                    if (dark.test(field[row][col])) {
                        g.fillRect((col + quietZone) * scale, (row + quietZone) * scale, scale, scale);
                    }
                }
            }
        } finally {
            g.dispose();
        }
        return image;
    }

    private String toSvg(Predicate<Cell> dark) {
        Cell[][] field = qrCodeField.getField();
        int side = field.length + 2 * quietZone;
        StringBuilder svg = new StringBuilder()
                .append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\"")
                .append(" width=\"").append(side * scale).append("\" height=\"").append(side * scale).append('"')
                .append(" viewBox=\"0 0 ").append(side).append(' ').append(side).append('"')
                .append(" shape-rendering=\"crispEdges\">\n")
                .append("<rect width=\"").append(side).append("\" height=\"").append(side).append("\" fill=\"#ffffff\"/>\n")
                .append("<g fill=\"#000000\">\n");
        for (int row = 0; row < field.length; row++) {
            int col = 0;
            while (col < field[row].length) {
                if (!dark.test(field[row][col])) {
                    col++;
                    continue;
                }
                int start = col;
                while (col < field[row].length && dark.test(field[row][col])) {
                    col++;
                }
                svg.append("<rect x=\"").append(start + quietZone).append("\" y=\"").append(row + quietZone)
                        .append("\" width=\"").append(col - start).append("\" height=\"1\"/>\n");
            }
        }
        return svg.append("</g>\n</svg>\n").toString();
    }

    private static boolean isDark(Cell cell) {
        return cell.getValue() == 1;
    }

    private static String fileName(Path path) {
        Path name = Objects.requireNonNull(path, "path").getFileName();
        if (name == null) {
            throw new IllegalArgumentException("not a file path: " + path);
        }
        return name.toString();
    }
}
