package educational.dmitriigurylev;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static educational.dmitriigurylev.VersionMap.Version;
import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals(21, qrCodeField.getField().length);
        assertEquals(21, qrCodeField.getField()[0].length);
        Arrays.stream(qrCodeField.getField())
                .flatMap(Arrays::stream)
                .forEach(cell -> assertEquals(0, cell.getValue()));
    }

    @Test
    void getFinderPatternsTest() {
        qrCodeField.addFinderPatterns();
        var field = qrCodeField.getField();
        for (int y = 0; y < field.length; y++) {
            for (int x = 0; x < field[0].length; x++) {
                Cell cell = field[y][x];
                if (y>=7 && y<=13) {
                    assertEquals(0, cell.getValue());
                } else if (y<=6 && x >=7 && x<=13) {
                    assertEquals(0, cell.getValue());
                } else if (y>=14 && x >=7) {
                    assertEquals(0, cell.getValue());
                } else if ((y == 1 || y == 5) && ((x >= 1 && x <= 5) || (x >= 15 && x <= 19))) {
                    assertEquals(0, cell.getValue());
                } else if ((y==2 || y==3 || y==4) && (x == 1 || x == 5 || x == 15 || x == 19)) {
                    assertEquals(0, cell.getValue());
                } else if ((y==16 || y==17 || y==18) && (x==1 || x==5)) {
                    assertEquals(0, cell.getValue());
                } else if ((y==15 || y==19) && (x >= 1 && x <= 5)) {
                    assertEquals(0, cell.getValue());
                } else {
                    assertEquals(1, cell.getValue());
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
                    assertTrue(cell.isBusy());
                    assertEquals(switch (y) {
                        case 8, 10, 12 -> 1;
                        default -> 0;
                    }, cell.getValue());
                } else if (y == 6 && x>=7 && x<=13) {
                    assertTrue(cell.isBusy());
                    assertEquals(switch (x) {
                        case 8, 10, 12 -> 1;
                        default -> 0;
                    }, cell.getValue());
                } else {
                    assertFalse(cell.isBusy());
                    assertEquals(0, cell.getValue());
                }
            }
        }
    }

    @Test
    void addTypeInformationBitsTest() {
        var field = qrCodeField.getField();
        CorrectionLevel cl = qrCodeField.getLevel();
        byte mask = 2;
        assertEquals("001110011100111", CorrectionLevelAndMaskCodeMap.getMaskAndCorrectionLevelCode(cl, mask));

        field[8][6].setBusy(true);
        field[6][8].setBusy(true);
        qrCodeField.addInformationTypeBits();
        for (int y = 0; y < field.length; y++) {
            for (int x = 0; x < field[0].length; x++) {
                Cell cell = field[y][x];
                if (y == 8 && (x<=8 || x>=13)) {
                    assertTrue(cell.isBusy());
                } else if (x == 8 && (y<=8 || y>=13)) {
                    assertTrue(cell.isBusy());
                } else {
                    assertFalse(cell.isBusy());
                }
            }
        }
    }

    @Test
    void givenNull_WhenDoubleInteger_ThenNull() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Cell cell = new Cell();
        assertEquals(0, cell.getValue());
        assertFalse(cell.isBusy());

        getSetValueAndBusyMethod().invoke(qrCodeField, cell, '1');
        assertEquals(1, cell.getValue());
        assertTrue(cell.isBusy());

        getSetValueAndBusyMethod().invoke(qrCodeField, cell, '0');
        assertEquals(0, cell.getValue());
        assertTrue(cell.isBusy());
    }

    private Method getSetValueAndBusyMethod() throws NoSuchMethodException {
        Method method = QrCodeField.class.getDeclaredMethod("setValueAndBusy", Cell.class, char.class);
        method.setAccessible(true);
        return method;
    }


}
