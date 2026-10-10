package educational.dmitriigurylev;

import educational.dmitriigurylev.enums.CorrectionLevel;
import educational.dmitriigurylev.enums.Version;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class QrCodeFieldIsolationTest {

    @Test
    void creatorsWithSameVersionDoNotShareMatrix() {
        var first = new QrCreator(Version.V_1, CorrectionLevel.HIGH, 1);
        var second = new QrCreator(Version.V_1, CorrectionLevel.HIGH, 2);

        assertNotSame(first.getQrCodeField().getField(), second.getQrCodeField().getField());
    }

    @Test
    void interleavedCreationKeepsEachResultIntact() {
        String expected1 = FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, 1).createQr().getQrCodeField().getField());
        String expected2 = FieldUtil.fieldToString(new QrCreator(Version.V_1, CorrectionLevel.HIGH, 2).createQr().getQrCodeField().getField());

        var first = new QrCreator(Version.V_1, CorrectionLevel.HIGH, 1);
        var second = new QrCreator(Version.V_1, CorrectionLevel.HIGH, 2);
        first.createQr();
        second.createQr();

        assertEquals(expected1, FieldUtil.fieldToString(first.getQrCodeField().getField()));
        assertEquals(expected2, FieldUtil.fieldToString(second.getQrCodeField().getField()));
    }

    @ParameterizedTest
    @EnumSource(Version.class)
    void fieldSizeAndTimingPatternsFollowVersion(Version version) {
        var qrCodeField = new QrCodeField(version, CorrectionLevel.HIGH);
        qrCodeField.addSynchronizationLines();
        Cell[][] field = qrCodeField.getField();
        int size = 17 + 4 * version.getNumber();

        assertEquals(size, field.length);
        assertEquals(size, field[0].length);
        for (int i = 8; i <= size - 9; i++) {
            int expected = i % 2 == 0 ? 1 : 0;
            assertEquals(expected, field[6][i].getValue(), "row 6, col " + i);
            assertEquals(expected, field[i][6].getValue(), "col 6, row " + i);
        }
    }
}
