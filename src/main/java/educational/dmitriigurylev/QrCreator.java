package educational.dmitriigurylev;

import educational.dmitriigurylev.custom_exceptions.InsufficientCapacityException;
import educational.dmitriigurylev.custom_exceptions.InvalidInputFormatException;
import educational.dmitriigurylev.encoders.AlphanumericEncoder;
import educational.dmitriigurylev.encoders.ByteEncoder;
import educational.dmitriigurylev.encoders.Encoder;
import educational.dmitriigurylev.encoders.NumericEncoder;
import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.EncodingWay;
import educational.dmitriigurylev.enums.MaskPattern;
import educational.dmitriigurylev.enums.Version;
import educational.dmitriigurylev.utility_maps.AlphanumericCharMap;
import educational.dmitriigurylev.utility_maps.BlocksCountMap;
import educational.dmitriigurylev.utility_maps.CharacterCountIndicatorMap;
import educational.dmitriigurylev.utility_maps.CorrectionBytesPerBlockMap;
import educational.dmitriigurylev.utility_maps.InformationBitSizeMap;
import lombok.AccessLevel;
import lombok.Getter;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Builds one QR code. Usual entry point:
 * <pre>{@code QrCodeField field = QrCreator.of("HELLO WORLD", CorrectionLevel.MEDIUM).build();}</pre>
 * The mode is the most compact one that covers every character (numeric, then alphanumeric, then byte as UTF-8),
 * the version is the smallest one the payload fits, and the mask is the one with the lowest penalty.
 * {@link #version(Version)} and {@link #forceMask(int)} override the automatic choices.
 * Empty input is rejected: a symbol that carries nothing is almost certainly a caller error.
 * <p>{@code of(...).build()} is the recommended API; {@code new QrCreator(version, level, input).createQr()} is the
 * legacy form, kept for existing callers. The class is mutable (a builder that keeps its last result) and not
 * thread-safe: use one instance per thread.
 */
@Getter
public class QrCreator {
    private static final int MODE_INDICATOR_BITS = 4;

    private static final Encoder NUMERIC_ENCODER = new NumericEncoder();
    private static final Encoder ALPHANUMERIC_ENCODER = new AlphanumericEncoder();
    private static final Encoder BYTE_ENCODER = new ByteEncoder();

    /** The field built by the last {@link #createQr()}; null before that. */
    private QrCodeField qrCodeField;
    /** Set by {@link #version(Version)} or the fixed-version constructor; null means "the smallest that fits". */
    @Getter(AccessLevel.NONE)
    private Version requestedVersion;
    /** The version used by the last {@link #createQr()}; null before that. */
    private Version version;
    private final CorrectionLevel correctionLevel;
    /** The mode chosen from the characters of the input; {@code byte[]} input is always byte mode. */
    private final EncodingWay encodingWay;
    /** What the encoder gets: the text for numeric and alphanumeric mode, the bytes for byte mode. */
    @Getter(AccessLevel.NONE)
    private final Object payload;
    /** Set by {@link #forceMask(int)}; null means "choose the mask with the lowest penalty". */
    @Getter(AccessLevel.NONE)
    private MaskPattern forcedMask;
    /** The mask applied by the last {@link #createQr()}. */
    private MaskPattern maskPattern;

    /**
     * Fixed-version form, kept for existing callers. {@code objectToEncode} is a {@code String}, a {@code byte[]},
     * or an {@code Integer}/{@code Long} (encoded as its decimal text).
     */
    public QrCreator(Version version, CorrectionLevel correctionLevel, Object objectToEncode) {
        this(correctionLevel, objectToEncode);
        this.requestedVersion = Objects.requireNonNull(version, "version");
    }

    private QrCreator(CorrectionLevel correctionLevel, Object objectToEncode) {
        this.correctionLevel = Objects.requireNonNull(correctionLevel, "correctionLevel");
        Object input = normalize(objectToEncode);
        if (input instanceof byte[] bytes) {
            this.encodingWay = EncodingWay.BYTE;
            // defensive copy: the caller may change its array after of(...)
            this.payload = bytes.clone();
        } else {
            String text = (String) input;
            this.encodingWay = chooseMode(text);
            this.payload = encodingWay == EncodingWay.BYTE ? ByteEncoder.toUtf8(text) : text;
        }
    }

    /** Text: the mode follows the characters; anything outside the alphanumeric set goes to byte mode as UTF-8. */
    public static QrCreator of(String text, CorrectionLevel correctionLevel) {
        return new QrCreator(correctionLevel, Objects.requireNonNull(text, "text"));
    }

    /** Raw bytes: always byte mode, written as they are. The array is copied. */
    public static QrCreator of(byte[] data, CorrectionLevel correctionLevel) {
        return new QrCreator(correctionLevel, Objects.requireNonNull(data, "data"));
    }

    /** Uses {@code version} instead of the smallest one that fits; {@link #createQr()} throws if the payload does not fit. */
    public QrCreator version(Version version) {
        this.requestedVersion = Objects.requireNonNull(version, "version");
        return this;
    }

    /**
     * Uses mask pattern {@code mask} (0-7) instead of the one with the lowest penalty.
     * Meant for tests and for reproducing a given symbol; the spec requires the lowest-penalty mask.
     */
    public QrCreator forceMask(int mask) {
        this.forcedMask = MaskPattern.of(mask);
        return this;
    }

    /** Builds the symbol and returns its field; same as {@code createQr().getQrCodeField()}. */
    public QrCodeField build() {
        return createQr().getQrCodeField();
    }

    /**
     * Picks (or checks) the version, then builds a fresh field. Each call starts over: without a requested version
     * the smallest fitting one is chosen again, and a {@link #version(Version)} set after a build is honoured.
     *
     * @throws InsufficientCapacityException if the payload does not fit the requested version or, without one, V10
     */
    public QrCreator createQr() {
        version = requestedVersion == null ? chooseVersion() : checkFits(requestedVersion);
        qrCodeField = new QrCodeField(version, correctionLevel);
        qrCodeField.addFinderPatterns();
        qrCodeField.addSynchronizationLines();
        qrCodeField.addAlignmentPatterns();
        qrCodeField.reserveInformationAreas();
        qrCodeField.fillFieldWithBitsSequence(encodeBits());
        maskPattern = forcedMask != null ? forcedMask : MaskEvaluator.chooseBest(qrCodeField);
        qrCodeField.applyMaskPattern(maskPattern);
        qrCodeField.addInformationTypeBits(maskPattern);
        qrCodeField.addVersionInformation();
        return this;
    }

    /**
     * Writes the symbol built by the last {@link #createQr()} or {@link #build()} with the default scale (10 px per
     * module) and quiet zone (4 modules), as PNG or SVG by the file extension. Use {@link #drawer()} for other settings.
     */
    public void drawQr(Path path) {
        drawer().write(path);
    }

    /**
     * A drawer for the symbol built by the last {@link #createQr()} or {@link #build()}, e.g.
     * {@code creator.drawer().setScale(4).setQuietZone(6).write(Path.of("qr.svg"))}; throws if there is none yet.
     */
    public QrImageDrawer drawer() {
        Objects.requireNonNull(qrCodeField, "call createQr()/build() first");
        return new QrImageDrawer(qrCodeField);
    }

    /** Number of characters (numeric, alphanumeric) or bytes (byte mode) in the payload. */
    private int payloadLength() {
        return payload instanceof byte[] bytes ? bytes.length : ((String) payload).length();
    }

    private Version chooseVersion() {
        for (Version v : Version.values()) {
            if (requiredBits(encodingWay, payloadLength(), v) <= capacityBits(v)) {
                return v;
            }
        }
        Version largest = Version.values()[Version.values().length - 1];
        throw new InsufficientCapacityException(overflowMessage(largest) + " (the largest supported version)");
    }

    private Version checkFits(Version v) {
        if (requiredBits(encodingWay, payloadLength(), v) > capacityBits(v)) {
            throw new InsufficientCapacityException(overflowMessage(v));
        }
        return v;
    }

    private int capacityBits(Version v) {
        return InformationBitSizeMap.getInformationBitsSizeByVersionAndCorrectionLevel(v, correctionLevel);
    }

    /** E.g. "alphanumeric payload of 155 characters needs 873 bits, V5-L holds 864". */
    private String overflowMessage(Version v) {
        String mode = switch (encodingWay) {
            case NUMERIC -> "numeric payload of %d digits";
            case ALPHANUMERIC -> "alphanumeric payload of %d characters";
            case BYTE -> "byte payload of %d bytes";
        };
        return mode.formatted(payloadLength())
                + " needs " + requiredBits(encodingWay, payloadLength(), v) + " bits, V" + v.getNumber()
                + "-" + correctionLevel.letter() + " holds " + capacityBits(v);
    }

    /** Mode indicator + character count indicator (its width depends on the version) + data bits. */
    static int requiredBits(EncodingWay mode, int length, Version v) {
        return MODE_INDICATOR_BITS
                + CharacterCountIndicatorMap.getCharacterCountIndicatorByVersionAndEncodingWay(v, mode)
                + dataBits(mode, length);
    }

    /** Data bits of a segment of {@code length} digits, characters or bytes (independent of the version). */
    static int dataBits(EncodingWay mode, int length) {
        return switch (mode) {
            case NUMERIC -> 10 * (length / 3) + new int[]{0, 4, 7}[length % 3];
            case ALPHANUMERIC -> 11 * (length / 2) + 6 * (length % 2);
            case BYTE -> 8 * length;
        };
    }

    /** The most compact mode covering every character: numeric ⊂ alphanumeric ⊂ byte. */
    static EncodingWay chooseMode(String text) {
        if (text.matches("\\d+")) {
            return EncodingWay.NUMERIC;
        } else if (text.chars().allMatch(c -> AlphanumericCharMap.contains((char) c))) {
            return EncodingWay.ALPHANUMERIC;
        } else {
            return EncodingWay.BYTE;
        }
    }

    /** Numbers become their decimal text, so the mode follows the characters: "-5" is alphanumeric, not numeric. */
    private static Object normalize(Object input) {
        Object normalized = switch (input) {
            case null -> throw new InvalidInputFormatException("nothing to encode: the input is null");
            case Integer _, Long _ -> String.valueOf(input);
            case String _, byte[] _ -> input;
            default -> throw new InvalidInputFormatException("unsupported input type: " + input.getClass().getName());
        };
        boolean empty = normalized instanceof byte[] bytes ? bytes.length == 0 : ((String) normalized).isEmpty();
        if (empty) {
            throw new InvalidInputFormatException("nothing to encode: the input is empty");
        }
        return normalized;
    }

    private StringBuilder encodeBits() {
        Encoder encoder = switch (encodingWay) {
            case NUMERIC -> NUMERIC_ENCODER;
            case ALPHANUMERIC -> ALPHANUMERIC_ENCODER;
            case BYTE -> BYTE_ENCODER;
        };
        String[] binaryArr = encoder.encode(payload, version);
        StringBuilder bitStringBuilder = UtilityMethods.binaryArrayToBitString(binaryArr);

        int maxBitSeq = capacityBits(version);
        StringBuilder bitStringBuilderWithTerminator = UtilityMethods.addTerminator(bitStringBuilder, maxBitSeq);
        String bitString = UtilityMethods.addLagZeros(bitStringBuilderWithTerminator);
        int[] decimalRawArr = UtilityMethods.binaryStringToDecimalArray(bitString);
        int[] decimalArr = UtilityMethods.addRotationalBytes(decimalRawArr, maxBitSeq / 8);

        int blocksCount = BlocksCountMap.getBlocksCountByVersionAndCorrectionLevel(version, correctionLevel);
        int[][] decimalArrays = UtilityMethods.splitIntoBlocks(decimalArr, blocksCount);

        int correctionBytesPerBlock = CorrectionBytesPerBlockMap.getCorrectionBytesSizeByVersionAndCorrectionLevel(version, correctionLevel);
        Block[] blocks = UtilityMethods.calculateCorrectionBytes(decimalArrays, correctionBytesPerBlock);

        String[] unitedArr = UtilityMethods.uniteBlocks(blocks);
        return UtilityMethods.codewordsToBits(unitedArr);
    }

}
