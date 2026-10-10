package educational.dmitriigurylev.v1NumericTests;

import educational.dmitriigurylev.FieldUtil;
import educational.dmitriigurylev.QrCreator;
import educational.dmitriigurylev.custom_exceptions.InsufficientCapacityException;
import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.Version;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** Snapshot tests: numeric payloads on V1-H with mask 2 must give the stored matrices. */
class QrCode_V1_H_Mask2_Test {

    @Test
    void val_1_Test() {
        Assertions.assertEquals(
                QrCodeFields_V1_H_Mask2.getFieldOfValue_1(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, 1).forceMask(2).createQr().getQrCodeField().getField())
        );
    }

    @Test
    void val_12_Test() {
        Assertions.assertEquals(
                QrCodeFields_V1_H_Mask2.getFieldOfValue_12(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, 12).forceMask(2).createQr().getQrCodeField().getField())
        );
    }

    @Test
    void val_123_Test() {
        Assertions.assertEquals(
                QrCodeFields_V1_H_Mask2.getFieldOfValue_123(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, 123).forceMask(2).createQr().getQrCodeField().getField())
        );
    }

    @Test
    void val_1234_Test() {
        Assertions.assertEquals(
                QrCodeFields_V1_H_Mask2.getFieldOfValue_1234(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, 1234).forceMask(2).createQr().getQrCodeField().getField())
        );
    }

    @Test
    void val_12345_Test() {
        Assertions.assertEquals(
                QrCodeFields_V1_H_Mask2.getFieldOfValue_12345(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, 12345).forceMask(2).createQr().getQrCodeField().getField())
        );
    }

    @Test
    void val_123456_Test() {
        Assertions.assertEquals(
                QrCodeFields_V1_H_Mask2.getFieldOfValue_123456(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, 123456).forceMask(2).createQr().getQrCodeField().getField())
        );
    }

    @Test
    void val_1234567_Test() {
        Assertions.assertEquals(
                QrCodeFields_V1_H_Mask2.getFieldOfValue_1234567(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, 1234567).forceMask(2).createQr().getQrCodeField().getField())
        );
    }

    @Test
    void val_12345689_Test() {
        Assertions.assertEquals(
                QrCodeFields_V1_H_Mask2.getFieldOfValue_123456789(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, 123456789).forceMask(2).createQr().getQrCodeField().getField())
        );
    }

    @Test
    void val_123456890_Test() {
        Assertions.assertEquals(
                QrCodeFields_V1_H_Mask2.getFieldOfValue_1234567890(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, 1234567890).forceMask(2).createQr().getQrCodeField().getField())
        );
    }

    @Test
    void val_1234568901_Test() {
        Assertions.assertEquals(
                QrCodeFields_V1_H_Mask2.getFieldOfValue_12345678901(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, 12345678901L).forceMask(2).createQr().getQrCodeField().getField())
        );
    }

    @Test
    void val_12345689012_Test() {
        Assertions.assertEquals(
                QrCodeFields_V1_H_Mask2.getFieldOfValue_123456789012(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, 123456789012L).forceMask(2).createQr().getQrCodeField().getField())
        );
    }
    @Test
    void val_99999999999999999_Test() {
        Assertions.assertEquals(
                QrCodeFields_V1_H_Mask2.getFieldOfValue_99999999999999999(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, "99999999999999999").forceMask(2).createQr().getQrCodeField().getField())
        );
    }
    @Test
    void val_int_max_value_Test() {
        Assertions.assertEquals(
                QrCodeFields_V1_H_Mask2.getFieldOfValue_int_max_value(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, Integer.MAX_VALUE).forceMask(2).createQr().getQrCodeField().getField())
        );
    }
    @Test
    void val_long_max_value_Test() {
        Assertions.assertThrowsExactly(
                InsufficientCapacityException.class,
                () -> new QrCreator(Version.V_1, CorrectionLevel.HIGH, Long.MAX_VALUE).createQr()
        );
    }

}
