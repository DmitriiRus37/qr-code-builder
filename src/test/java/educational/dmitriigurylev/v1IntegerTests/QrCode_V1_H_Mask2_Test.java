package educational.dmitriigurylev.v1IntegerTests;

import educational.dmitriigurylev.FieldUtil;
import educational.dmitriigurylev.QrCreator;
import educational.dmitriigurylev.custom_exceptions.InsuffiecientQrLengthToEncode;
import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.Version;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class QrCode_V1_H_Mask2_Test {

    @Test
    void val_1_Test() {
        Assertions.assertEquals(
                QrCodeFileds_V1_H_Mask2.getFieldOfValue_1(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, 1).createQr().getQrCodeField().getField())
        );
    }

    @Test
    void val_12_Test() {
        Assertions.assertEquals(
                QrCodeFileds_V1_H_Mask2.getFieldOfValue_12(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, 12).createQr().getQrCodeField().getField())
        );
    }

    @Test
    void val_123_Test() {
        Assertions.assertEquals(
                QrCodeFileds_V1_H_Mask2.getFieldOfValue_123(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, 123).createQr().getQrCodeField().getField())
        );
    }

    @Test
    void val_1234_Test() {
        Assertions.assertEquals(
                QrCodeFileds_V1_H_Mask2.getFieldOfValue_1234(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, 1234).createQr().getQrCodeField().getField())
        );
    }

    @Test
    void val_12345_Test() {
        Assertions.assertEquals(
                QrCodeFileds_V1_H_Mask2.getFieldOfValue_12345(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, 12345).createQr().getQrCodeField().getField())
        );
    }

    @Test
    void val_123456_Test() {
        Assertions.assertEquals(
                QrCodeFileds_V1_H_Mask2.getFieldOfValue_123456(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, 123456).createQr().getQrCodeField().getField())
        );
    }

    @Test
    void val_1234567_Test() {
        Assertions.assertEquals(
                QrCodeFileds_V1_H_Mask2.getFieldOfValue_1234567(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, 1234567).createQr().getQrCodeField().getField())
        );
    }

    @Test
    void val_12345689_Test() {
        Assertions.assertEquals(
                QrCodeFileds_V1_H_Mask2.getFieldOfValue_123456789(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, 123456789).createQr().getQrCodeField().getField())
        );
    }

    @Test
    void val_123456890_Test() {
        Assertions.assertEquals(
                QrCodeFileds_V1_H_Mask2.getFieldOfValue_1234567890(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, 1234567890).createQr().getQrCodeField().getField())
        );
    }

    @Test
    void val_1234568901_Test() {
        Assertions.assertEquals(
                QrCodeFileds_V1_H_Mask2.getFieldOfValue_12345678901(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, 12345678901L).createQr().getQrCodeField().getField())
        );
    }

    @Test
    void val_12345689012_Test() {
        Assertions.assertEquals(
                QrCodeFileds_V1_H_Mask2.getFieldOfValue_123456789012(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, 123456789012L).createQr().getQrCodeField().getField())
        );
    }
    @Test
    void val_99999999999999999_Test() {
        Assertions.assertEquals(
                QrCodeFileds_V1_H_Mask2.getFieldOfValue_99999999999999999(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, "99999999999999999").createQr().getQrCodeField().getField())
        );
    }
    @Test
    void val_int_max_value_Test() {
        Assertions.assertEquals(
                QrCodeFileds_V1_H_Mask2.getFieldOfValue_int_max_value(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, Integer.MAX_VALUE).createQr().getQrCodeField().getField())
        );
    }
    @Test
    void val_long_max_value_Test() {
        Assertions.assertThrowsExactly(
                InsuffiecientQrLengthToEncode.class,
                () -> new QrCreator(Version.V_1, CorrectionLevel.HIGH, Long.MAX_VALUE).createQr()
        );
    }

}
