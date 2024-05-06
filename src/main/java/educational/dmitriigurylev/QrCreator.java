package educational.dmitriigurylev;

import educational.dmitriigurylev.custom_exceptions.InsuffiecientQrLengthToEncode;
import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.Version;
import educational.dmitriigurylev.utility_maps.BlocksCountMap;
import educational.dmitriigurylev.utility_maps.CorrectionBytesPerBlockMap;
import educational.dmitriigurylev.utility_maps.EncoderMap;
import educational.dmitriigurylev.utility_maps.InformationBitSizeMap;
import lombok.Getter;

import java.util.List;

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
        qrCodeField.fillFieldWithBitsSequence(encodeBits(objectToEncode));
        qrCodeField.applyMaskPattern();
        new QrImageDrawer(qrCodeField).drawImage(filename, "jpg");
        return new int[0][0];
    }

    private StringBuilder encodeBits(Object objectToEncode) {
        String[] binaryArr = getBinaryArray(objectToEncode, version);
        StringBuilder bitStringBuilder = UtilityMethods.binaryArrayToBitString(binaryArr);
        String bitString = UtilityMethods.addLagZeros(bitStringBuilder);
        int[] decimalRawArr = UtilityMethods.binaryStringToDecimalArray(bitString);
        int maxByteSequence = InformationBitSizeMap.getInformationBitsSizeByVersionAndCorrectionLevel(version, correctionLevel) / 8;
        if (decimalRawArr.length > maxByteSequence) {
            throw new InsuffiecientQrLengthToEncode();
        }
        int[] decimalArr = UtilityMethods.addRotationalBytes(decimalRawArr, maxByteSequence);

        int blocksCount = BlocksCountMap.getBlocksCountByVersionAndCorrectionLevel(version, correctionLevel);
        int correctionBytesPerBlock = CorrectionBytesPerBlockMap.getCorrectionBytesSizeByVersionAndCorrectionLevel(version, correctionLevel);

        Block[] blocks = UtilityMethods.splitIntoBlocks(decimalArr, blocksCount);
        UtilityMethods.calculateCorrectionBytes(blocks, correctionBytesPerBlock);

        String[] unitedArr = UtilityMethods.uniteBlocks(blocks);
        return UtilityMethods.getStringBuilder(unitedArr);
      }

    private String[] getBinaryArray(Object objectToEncode, Version version) {
        return EncoderMap.getEncoder(objectToEncode.getClass())
                .setValueAndVersion(objectToEncode, version)
                .transformToBinaryArray();
    }

}
