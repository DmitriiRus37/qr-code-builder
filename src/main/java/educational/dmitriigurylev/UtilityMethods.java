package educational.dmitriigurylev;

import educational.dmitriigurylev.custom_exceptions.InsufficientCapacityException;
import educational.dmitriigurylev.custom_exceptions.InvalidInputFormatException;
import educational.dmitriigurylev.reed_solomon_mapping.GaloisField;
import educational.dmitriigurylev.utility_maps.GeneratingPolynomialMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Static helpers for the bit-stream and Reed-Solomon stages of the encoding pipeline. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UtilityMethods {
    /**
     * Parses a big-endian string of 0s and 1s as a non-negative integer.
     *
     * @throws InvalidInputFormatException if the string is null, empty or not binary
     */
    public static int binaryToDecimal(String binaryString) {
        if (binaryString == null || binaryString.isEmpty() || !containsZerosAndOnesOnly(binaryString)) {
            throw new InvalidInputFormatException("not a binary string: " + quote(binaryString));
        }
        int decimal = 0;
        int length = binaryString.length();
        for (int i = 0; i < length; i++) {
            if (binaryString.charAt(i) == '1') {
                decimal += (int) Math.pow(2, length - i - 1);
            }
        }
        return decimal;
    }

    /**
     * Big-endian binary representation of {@code value} in exactly {@code width} bits, left-padded with zeros.
     * Every QR field has a fixed width (e.g. 8 bits per byte), so a value that does not fit is an error, never truncated.
     */
    public static String toBits(int value, int width) {
        if (width < 1 || width > 31 || value < 0 || value >= 1 << width) {
            throw new InvalidInputFormatException("value " + value + " does not fit in " + width + " bits");
        }
        StringBuilder bits = new StringBuilder(width);
        for (int i = width - 1; i >= 0; i--) {
            bits.append((value >>> i) & 1);
        }
        return bits.toString();
    }

    /**
     * Character-count indicator in exactly {@code width} bits. A count that does not fit the field
     * can never fit the symbol either, so it is reported as a capacity problem, not as bad input.
     */
    public static String characterCountToBits(int count, int width) {
        if (count >= 1 << width) {
            throw new InsufficientCapacityException(
                    "character count " + count + " does not fit the " + width + "-bit count field");
        }
        return toBits(count, width);
    }

    /** Concatenates the segment parts (mode, count, data bits) into one bit stream. */
    public static StringBuilder binaryArrayToBitString(String[] strAr) {
        if (strAr == null) {
            throw new InvalidInputFormatException("no bit strings to join: null");
        }
        return new StringBuilder(String.join("", strAr));
    }

    /** Pads the bit stream with zeros up to the next multiple of 8 (byte boundary). */
    public static String addLagZeros(StringBuilder binaryString) {
        StringBuilder res = new StringBuilder(binaryString);
        byte remainToWholeByte = (byte) (8 - binaryString.length() % 8);
        return res.append("0".repeat(remainToWholeByte == 8 ? 0 : remainToWholeByte)).toString();
    }

    /** Cuts a bit string into 8-bit codewords and returns their values 0-255. */
    public static int[] binaryStringToDecimalArray(String bitString) {
        if (bitString == null || bitString.isEmpty() || bitString.length() % 8 != 0) {
            throw new InvalidInputFormatException("bit string length must be a positive multiple of 8: "
                    + quote(bitString));
        }
        int[] decimalIntArray = new int[bitString.length() / 8];
        int i = 0;
        while (!bitString.isEmpty()) {
            decimalIntArray[i++] = UtilityMethods.binaryToDecimal(bitString.substring(0, 8));
            bitString = bitString.substring(8);
        }
        return decimalIntArray;
    }

    /** Fills the codewords up to {@code maxByteSequence} with the alternating pad bytes 236 and 17. */
    public static int[] addRotationalBytes(int[] decimalArr, int maxByteSequence) {
        int[] resArray = new int[maxByteSequence];
        System.arraycopy(decimalArr, 0, resArray, 0, decimalArr.length);
        int i = decimalArr.length;
        while (true) {
            if (i>=resArray.length) break;
            resArray[i++] = 236;
            if (i>=resArray.length) break;
            resArray[i++] = 17;
        }
        return resArray;
    }

    /**
     * Whether the string is non-empty and consists of 0s and 1s only.
     *
     * @throws InvalidInputFormatException if {@code str} is null
     */
    public static boolean containsZerosAndOnesOnly(String str) {
        if (str == null) {
            throw new InvalidInputFormatException("not a binary string: null");
        }
        Pattern pattern = Pattern.compile("^[01]+$");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    /**
     * Reed–Solomon EC codewords = remainder of data(x) * x^n divided by the generator g(x).
     * Classic shift-register division: the remainder never grows beyond n coefficients,
     * so it works for any ratio of data to EC codewords.
     */
    public static int[] getCorrectionBytes(int[] decimalArr, int correctionBytesPerBlock) {
        // α-exponents of g(x) without the leading coefficient α^0
        int[] generatingPolynomial = GeneratingPolynomialMap.getGeneratingPolynomial(correctionBytesPerBlock);
        int[] remainder = new int[correctionBytesPerBlock];

        for (int dataByte : decimalArr) {
            int factor = dataByte ^ remainder[0];
            System.arraycopy(remainder, 1, remainder, 0, correctionBytesPerBlock - 1);
            remainder[correctionBytesPerBlock - 1] = 0;
            if (factor == 0) {
                continue;
            }
            int logFactor = GaloisField.log(factor);
            for (int i = 0; i < correctionBytesPerBlock; i++) {
                remainder[i] ^= GaloisField.exp((generatingPolynomial[i] + logFactor) % 255);
            }
        }
        return remainder;
    }

    /**
     * Splits the data codewords into {@code blocksCount} blocks; when they do not divide evenly,
     * the last blocks (group 2) get one codeword more.
     */
    public static int[][] splitIntoBlocks(int[] inputDecimalArr, int blocksCount) {
        int quotient = inputDecimalArr.length / blocksCount;
        int remainder = inputDecimalArr.length % blocksCount;
        int[][] decimalArrays = new int[blocksCount][];

        int counter=0;
        for (int i = 0; i < blocksCount; i++) {
            int[] perBlockDecimalArr = new int[(i < (blocksCount - remainder)) ? quotient : (quotient + 1)];

            for (int j = 0; j < perBlockDecimalArr.length; j++) {
                perBlockDecimalArr[j] = inputDecimalArr[counter++];
            }
            decimalArrays[i] = perBlockDecimalArr;
        }
        return decimalArrays;
    }

    /** Computes the EC codewords for each data block. */
    public static Block[] calculateCorrectionBytes(int[][] decimalArrays, int correctionBytesPerBlock) {
        return Arrays.stream(decimalArrays)
                .map(arr -> new Block(arr, UtilityMethods.getCorrectionBytes(arr, correctionBytesPerBlock)))
                .toArray(Block[]::new);
    }

    /** Interleaves the blocks: data codewords column by column, then EC codewords column by column. */
    public static String[] uniteBlocks(Block[] blocks) {
        LinkedList<List<Integer>> dataCodewords = new LinkedList<>();
        LinkedList<List<Integer>> correctionBytesList = new LinkedList<>();

        for (Block block : blocks) {
            dataCodewords.add(Arrays.stream(block.getDataCodewords())
                    .boxed()
                    .collect(Collectors.toList()));
            List<Integer> list = Arrays.stream(block.getEcCodewords())
                    .boxed()
                    .collect(Collectors.toCollection(LinkedList::new));
            correctionBytesList.add(list);
        }

        List<Integer> resList = new LinkedList<>();
        while (!dataCodewords.isEmpty()) {
            dataCodewords = dataCodewords.stream()
                    .peek(currentList -> resList.add(currentList.remove(0)))
                    .filter(list -> !list.isEmpty())
                    .collect(Collectors.toCollection(LinkedList::new));
        }

        while (!correctionBytesList.isEmpty()) {
            correctionBytesList = correctionBytesList.stream()
                    .peek(currentList -> resList.add(currentList.remove(0)))
                    .filter(list -> !list.isEmpty())
                    .collect(Collectors.toCollection(LinkedList::new));
        }

        return resList.stream()
                .map(String::valueOf)
                .toArray(String[]::new);
    }

    /** The final bit stream: each codeword (decimal text, 0-255) as 8 bits. The input array is not changed. */
    public static StringBuilder codewordsToBits(String[] codewords) {
        StringBuilder sb = new StringBuilder(codewords.length * 8);
        for (String codeword : codewords) {
            sb.append(toBits(Integer.parseInt(codeword), 8));
        }
        return sb;
    }

    /** Wraps a string in quotes for error messages, or returns "null". */
    private static String quote(String s) {
        return s == null ? "null" : "'" + s + "'";
    }

    /**
     * Appends the terminator: up to 4 zero bits, fewer if the symbol is nearly full.
     *
     * @throws InsufficientCapacityException if the stream is already longer than {@code maxBitSequence}
     */
    public static StringBuilder addTerminator(StringBuilder bs, int maxBitSequence) {
        int remainZeros = maxBitSequence - bs.length();
        if (remainZeros < 0) {
            throw new InsufficientCapacityException(
                    "needs " + bs.length() + " bits, the symbol holds " + maxBitSequence);
        }
        return bs.append("0".repeat(Math.min(remainZeros, 4)));
    }

}
