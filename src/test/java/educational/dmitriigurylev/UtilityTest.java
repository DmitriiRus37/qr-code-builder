package educational.dmitriigurylev;

import educational.dmitriigurylev.custom_exceptions.InvalidInputFormatException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static educational.dmitriigurylev.UtilityMethods.*;

class UtilityTest {

    @Test
    void binaryToDecimalTest() {
        Assertions.assertEquals(0, binaryToDecimal("0"));
        Assertions.assertEquals(1, binaryToDecimal("00000001"));
        Assertions.assertEquals(255, binaryToDecimal("11111111"));
        Assertions.assertEquals(1048575, binaryToDecimal("11111111111111111111"));
        Assertions.assertThrowsExactly(InvalidInputFormatException.class, () -> binaryToDecimal(""));
        Assertions.assertThrowsExactly(InvalidInputFormatException.class, () -> binaryToDecimal("q110"));
        Assertions.assertThrowsExactly(InvalidInputFormatException.class, () -> binaryToDecimal(null));
    }

    @Test
    void containsZerosAndOnesOnlyTest() {
        Assertions.assertTrue(containsZerosAndOnesOnly("0"));
        Assertions.assertTrue(containsZerosAndOnesOnly("0111110"));
        Assertions.assertFalse(containsZerosAndOnesOnly("012345678910"));
        Assertions.assertFalse(containsZerosAndOnesOnly(""));
        Assertions.assertFalse(containsZerosAndOnesOnly("000111w"));
        Assertions.assertThrowsExactly(InvalidInputFormatException.class, () -> containsZerosAndOnesOnly(null));
    }

    @Test
    void containsAllLettersUpperCaseTest() {
        Assertions.assertTrue(containsAllLettersUpperCase("0"));
        Assertions.assertTrue(containsAllLettersUpperCase("ABC"));
        Assertions.assertTrue(containsAllLettersUpperCase("54321ABC12345"));
        Assertions.assertTrue(containsAllLettersUpperCase(""));
        Assertions.assertFalse(containsAllLettersUpperCase("ABCqwe"));
        Assertions.assertThrowsExactly(InvalidInputFormatException.class, () -> containsAllLettersUpperCase(null));
    }

    @Test
    void binaryStringToDecimalArrayTest() {
        Assertions.assertArrayEquals(new int[]{28,236,17,236,17,236,17,236,17}, binaryStringToDecimalArray("00011100"));
        Assertions.assertArrayEquals(new int[]{28,241,236,17,236,17,236,17,236}, binaryStringToDecimalArray("0001110011110001"));
        Assertions.assertThrowsExactly(InvalidInputFormatException.class, () -> binaryStringToDecimalArray(""));
        Assertions.assertThrowsExactly(InvalidInputFormatException.class, () -> binaryStringToDecimalArray("0010"));
        Assertions.assertThrowsExactly(InvalidInputFormatException.class, () -> binaryStringToDecimalArray("02100000"));
        Assertions.assertThrowsExactly(InvalidInputFormatException.class, () -> binaryStringToDecimalArray(null));
    }

    @Test
    void binaryArrayToBitStringTest() {
        Assertions.assertEquals("10000000", binaryArrayToBitString(new String[]{"1"}));
        Assertions.assertEquals("01001100", binaryArrayToBitString(new String[]{"0", "1", "0011"}));
        Assertions.assertEquals("0100110011000000", binaryArrayToBitString(new String[]{"0", "1", "00110011"}));
        Assertions.assertEquals("", binaryArrayToBitString(new String[]{}));
        Assertions.assertThrowsExactly(InvalidInputFormatException.class, () -> binaryArrayToBitString(null));
    }
}
