package educational.dmitriigurylev;

import educational.dmitriigurylev.custom_exceptions.InsuffiecientQrLengthToEncode;
import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.Version;
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

    public int[][] createQr() {
        qrCodeField.addFinderPatterns();
        qrCodeField.addSynchronizationLines();
        qrCodeField.addInformationTypeBits();
        qrCodeField.fillFieldWithBitsSequence(encodeBits(objectToEncode));
        qrCodeField.applyMaskPattern();
        new QrImageDrawer(qrCodeField).drawImage("qr_image.jpg", "jpg");
        return new int[0][0];
    }

    private StringBuilder encodeBits(Object objectToEncode) {
        String[] binaryArr = getBinaryArray(objectToEncode, version);
        StringBuilder bitStringBuilder = UtilityMethods.binaryArrayToBitString(binaryArr);
        String bitString = UtilityMethods.addLeadZeros(bitStringBuilder);
        int maxBitSequence = InformationBitSizeMap.getInformationBitsSizeByVersionAndCorrectionLevel(version, correctionLevel);
        if (bitString.length() > maxBitSequence) {
            throw new InsuffiecientQrLengthToEncode();
        }
        int[] decimalArr = UtilityMethods.binaryStringToDecimalArray(bitString);
        int correctionBytesPerBlock = CorrectionBytesPerBlockMap.getCorrectionBytesSizeByVersionAndCorrectionLevel(version, correctionLevel);
        List<Integer> listCorrectBytes = UtilityMethods.getCorrectionBytes(decimalArr, correctionBytesPerBlock);
        String[] unitedArr = new String[decimalArr.length + listCorrectBytes.size()];
        for (int i = 0; i < decimalArr.length; i++) {
            unitedArr[i] = String.valueOf(decimalArr[i]);
        }
        for (int i = decimalArr.length; i < unitedArr.length; i++) {
            unitedArr[i] = String.valueOf(listCorrectBytes.remove(0));
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < unitedArr.length; i++) {
            int decimalValue = Integer.parseInt(unitedArr[i]);
            String binStr = Integer.toBinaryString(decimalValue);
            unitedArr[i] = String.format("%8s", binStr).replace(' ', '0');
            sb.append(unitedArr[i]);
        }
        return sb;
    }

    private String[] getBinaryArray(Object objectToEncode, Version version) {
        return EncoderMap.getEncoder(objectToEncode.getClass())
                .setValueAndVersion(objectToEncode, version)
                .transformToBinaryArray();
    }

}
