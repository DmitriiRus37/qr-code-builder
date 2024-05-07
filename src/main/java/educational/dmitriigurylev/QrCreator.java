package educational.dmitriigurylev;

import educational.dmitriigurylev.custom_exceptions.InsuffiecientQrLengthToEncode;
import educational.dmitriigurylev.custom_exceptions.InvalidInputFormatException;
import educational.dmitriigurylev.encoders.ByteEncoder;
import educational.dmitriigurylev.encoders.Encoder;
import educational.dmitriigurylev.encoders.IntLetterEncoder;
import educational.dmitriigurylev.encoders.IntegerEncoder;
import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.Version;
import educational.dmitriigurylev.utility_maps.BlocksCountMap;
import educational.dmitriigurylev.utility_maps.CorrectionBytesPerBlockMap;
import educational.dmitriigurylev.utility_maps.InformationBitSizeMap;
import educational.dmitriigurylev.utility_maps.IntLetterEncoderCharMap;
import lombok.Getter;

@Getter
public class QrCreator {
    private final QrCodeField qrCodeField;
    private final Version version;
    private final CorrectionLevel correctionLevel;
    private final Object objectToEncode;

    public QrCreator(Version version, CorrectionLevel correctionLevel, Object objectToEncode) {
        this.version = version;
        this.correctionLevel = correctionLevel;
        this.objectToEncode = objectToEncode;
        this.qrCodeField = new QrCodeField(version, correctionLevel);
    }

    public int[][] createQr(String filename) {
        qrCodeField.addFinderPatterns();
        qrCodeField.addSynchronizationLines();
        qrCodeField.addInformationTypeBits();
        qrCodeField.addAlignmentPatterns();
        qrCodeField.fillFieldWithBitsSequence(encodeBits(objectToEncode));
        qrCodeField.applyMaskPattern();
        new QrImageDrawer(qrCodeField).drawImage(filename, "jpg");
        return new int[0][0];
    }

    private StringBuilder encodeBits(Object objectToEncode) {
        String[] binaryArr = createEncoder(objectToEncode)
                .setValueAndVersion(objectToEncode, version)
                .transformToBinaryArray();
        StringBuilder bitStringBuilder = UtilityMethods.binaryArrayToBitString(binaryArr);
        String bitString = UtilityMethods.addLagZeros(bitStringBuilder);
        int[] decimalRawArr = UtilityMethods.binaryStringToDecimalArray(bitString);
        int maxByteSequence = InformationBitSizeMap.getInformationBitsSizeByVersionAndCorrectionLevel(version, correctionLevel) / 8;
        if (decimalRawArr.length > maxByteSequence) {
            throw new InsuffiecientQrLengthToEncode();
        }
        int[] decimalArr = UtilityMethods.addRotationalBytes(decimalRawArr, maxByteSequence);

        int blocksCount = BlocksCountMap.getBlocksCountByVersionAndCorrectionLevel(version, correctionLevel);
        Block[] blocks = UtilityMethods.splitIntoBlocks(decimalArr, blocksCount);

        int correctionBytesPerBlock = CorrectionBytesPerBlockMap.getCorrectionBytesSizeByVersionAndCorrectionLevel(version, correctionLevel);
        UtilityMethods.calculateCorrectionBytes(blocks, correctionBytesPerBlock);

        String[] unitedArr = UtilityMethods.uniteBlocks(blocks);
        return UtilityMethods.getStringBuilder(unitedArr);
    }

    private Encoder createEncoder(Object objectToEncode) {
        Class objectClass = objectToEncode.getClass();
        if (objectClass == byte[].class) {
            return new ByteEncoder();
        } else if (objectClass == Integer.class) {
            return new IntegerEncoder();
        } else if (objectClass == String.class) {
            String stringObjectToEncode = (String) objectToEncode;
            boolean res = stringObjectToEncode.chars()
                    .mapToObj(i->(char) i)
                    .allMatch(IntLetterEncoderCharMap::contains);

            if (stringObjectToEncode.matches("\\d+")) {
                return new IntegerEncoder();
            } else if (res) {
                return new IntLetterEncoder();
            } else {
                return new ByteEncoder();
            }
        } else {
            throw new InvalidInputFormatException();
        }
    }

}
