package educational.dmitriigurylev.v1IntLetterTests.v1IntegerTests;

import educational.dmitriigurylev.FieldUtil;
import educational.dmitriigurylev.QrCreator;
import educational.dmitriigurylev.custom_exceptions.InsuffiecientQrLengthToEncode;
import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.Version;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class QrCode_V1_H_Mask2_Test {

    @Test
    void val_A_Test() {
        Assertions.assertEquals(
                QrCodeFileds_V1_H_Mask2.getFieldOfValue_A(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, "A").createQr().getQrCodeField().getField())
        );
    }
    @Test
    void val_AB_Test() {
        Assertions.assertEquals(
                QrCodeFileds_V1_H_Mask2.getFieldOfValue_AB(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, "AB").createQr().getQrCodeField().getField())
        );
    }
    @Test
    void val_ABC_Test() {
        Assertions.assertEquals(
                QrCodeFileds_V1_H_Mask2.getFieldOfValue_ABC(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, "ABC").createQr().getQrCodeField().getField())
        );
    }
    @Test
    void val_ABCD_Test() {
        Assertions.assertEquals(
                QrCodeFileds_V1_H_Mask2.getFieldOfValue_ABCD(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, "ABCD").createQr().getQrCodeField().getField())
        );
    }
    @Test
    void val_ABCDE_Test() {
        Assertions.assertEquals(
                QrCodeFileds_V1_H_Mask2.getFieldOfValue_ABCDE(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, "ABCDE").createQr().getQrCodeField().getField())
        );
    }
    @Test
    void val_ABCDEF_Test() {
        Assertions.assertEquals(
                QrCodeFileds_V1_H_Mask2.getFieldOfValue_ABCDEF(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, "ABCDEF").createQr().getQrCodeField().getField())
        );
    }
    @Test
    void val_ABCDEFG_Test() {
        Assertions.assertEquals(
                QrCodeFileds_V1_H_Mask2.getFieldOfValue_ABCDEFG(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, "ABCDEFG").createQr().getQrCodeField().getField())
        );
    }
    @Test
    void val_ABCDEFGH_Test() {
        Assertions.assertEquals(
                QrCodeFileds_V1_H_Mask2.getFieldOfValue_ABCDEFGH(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, "ABCDEFGH").createQr().getQrCodeField().getField())
        );
    }
    @Test
    void val_ABCDEFGHI_Test() {
        Assertions.assertEquals(
                QrCodeFileds_V1_H_Mask2.getFieldOfValue_ABCDEFGHI(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, "ABCDEFGHI").createQr().getQrCodeField().getField())
        );
    }
    @Test
    void val_ABCDEFGHIJ_Test() {
        Assertions.assertEquals(
                QrCodeFileds_V1_H_Mask2.getFieldOfValue_ABCDEFGHIJ(),
                FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, "ABCDEFGHIJ").createQr().getQrCodeField().getField())
        );
    }
    @Test
    void val_ABCDEFGHIJK_Test() {
        Assertions.assertThrowsExactly(
                InsuffiecientQrLengthToEncode.class,
                () -> new QrCreator(Version.V_1, CorrectionLevel.HIGH, "ABCDEFGHIJK").createQr()
        );
    }
}
