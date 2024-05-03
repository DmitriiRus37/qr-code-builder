package educational.dmitriigurylev;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class QrCodeV1FieldTest {

    QrCodeField qrCodeField;
    Version version = Version.V_1;
    CorrectionLevel level = CorrectionLevel.HIGH;

    @BeforeEach
    void initField() {
        qrCodeField = new QrCodeField(version, level);
    }

    @Test
    void initFieldV1IsWithZerosTest() {
        Assertions.assertEquals(21, qrCodeField.getField().length);
        Assertions.assertEquals(21, qrCodeField.getField()[0].length);
        Arrays.stream(qrCodeField.getField())
                .flatMap(Arrays::stream)
                .forEach(cell -> Assertions.assertEquals(0, cell.getValue()));
    }

    @Test
    void getFinderPatternsTest() {
        qrCodeField.addFinderPatterns();
        var field = qrCodeField.getField();
        for (int y = 0; y < field.length; y++) {
            for (int x = 0; x < field[0].length; x++) {
                Cell cell = field[y][x];
                if (y>=7 && y<=13) {
                    Assertions.assertEquals(0, cell.getValue());
                } else if (y<=6 && x >=7 && x<=13) {
                    Assertions.assertEquals(0, cell.getValue());
                } else if (y>=14 && x >=7) {
                    Assertions.assertEquals(0, cell.getValue());
                } else if ((y == 1 || y == 5) && ((x >= 1 && x <= 5) || (x >= 15 && x <= 19))) {
                    Assertions.assertEquals(0, cell.getValue());
                } else if ((y==2 || y==3 || y==4) && (x == 1 || x == 5 || x == 15 || x == 19)) {
                    Assertions.assertEquals(0, cell.getValue());
                } else if ((y==16 || y==17 || y==18) && (x==1 || x==5)) {
                    Assertions.assertEquals(0, cell.getValue());
                } else if ((y==15 || y==19) && (x >= 1 && x <= 5)) {
                    Assertions.assertEquals(0, cell.getValue());
                } else {
                    Assertions.assertEquals(1, cell.getValue());
                }
            }
        }
    }

    @Test
    void addSynchronizationLinesTest() {
        qrCodeField.addSynchronizationLines();
        var field = qrCodeField.getField();
        for (int y = 0; y < field.length; y++) {
            for (int x = 0; x < field[0].length; x++) {
                Cell cell = field[y][x];
                if (x==6 && y>=7 && y<=13) {
                    Assertions.assertTrue(cell.isBusy());
                    Assertions.assertEquals(switch (y) {
                        case 8, 10, 12 -> 1;
                        default -> 0;
                    }, cell.getValue());
                } else if (y == 6 && x>=7 && x<=13) {
                    Assertions.assertTrue(cell.isBusy());
                    Assertions.assertEquals(switch (x) {
                        case 8, 10, 12 -> 1;
                        default -> 0;
                    }, cell.getValue());
                } else {
                    Assertions.assertFalse(cell.isBusy());
                    Assertions.assertEquals(0, cell.getValue());
                }
            }
        }
    }

}
