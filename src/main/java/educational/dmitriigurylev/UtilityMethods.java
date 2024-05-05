package educational.dmitriigurylev;


import educational.dmitriigurylev.custom_exceptions.InvalidInputFormatException;
import educational.dmitriigurylev.reed_solomon_mapping.ABMap;
import educational.dmitriigurylev.reed_solomon_mapping.CDMap;
import educational.dmitriigurylev.utility_maps.GeneratingPolynomialMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UtilityMethods {
    public static int binaryToDecimal(String binaryString) {
        if (binaryString == null || binaryString.isEmpty() || !containsZerosAndOnesOnly(binaryString)) {
            throw new InvalidInputFormatException();
        }
        int decimal = 0;
        int length = binaryString.length();
        for (int i = 0; i < length; i++) {
            if (binaryString.charAt(i) == '1') {
                decimal += (int) Math.pow(2, length - i - 1);
            }
        }
        return decimal;
    }

    public static StringBuilder binaryArrayToBitString(String[] strAr) {
        if (strAr == null) {
            throw new InvalidInputFormatException();
        }
        return new StringBuilder(String.join("", strAr));
    }

    public static String addLagZeros(StringBuilder binaryString) {
        StringBuilder res = new StringBuilder(binaryString);
        byte remainToWholeByte = (byte) (8 - binaryString.length() % 8);
        return res.append("0".repeat(remainToWholeByte == 8 ? 0 : remainToWholeByte)).toString();
    }

    public static int[] binaryStringToDecimalArray(String bitString) {
        if (bitString == null || bitString.isEmpty() || bitString.length() % 8 != 0) {
            throw new InvalidInputFormatException();
        }
        int[] decimalIntArray = new int[bitString.length() / 8];
        int i = 0;
        while (!bitString.isEmpty()) {
            decimalIntArray[i++] = UtilityMethods.binaryToDecimal(bitString.substring(0, 8));
            bitString = bitString.substring(8);
        }
        return decimalIntArray;
    }

    public static int[] addRotationalBytes(int[] decimalArr, int maxByteSequence) {
        int[] resArray = new int[maxByteSequence];
        System.arraycopy(decimalArr, 0, resArray, 0, decimalArr.length);
        int i = decimalArr.length;
        while (true) {
            if (i>=resArray.length) break;
            resArray[i++] = 236;
            if (i>=resArray.length) break;
            resArray[i++] = 17;
        }
        return resArray;
    }

    public static boolean containsAllLettersUpperCase(String str) {
        if (str == null) {
            throw new InvalidInputFormatException();
        }
        return str.chars()
                .filter(Character::isLetter)
                .allMatch(Character::isUpperCase);
    }

    public static boolean containsZerosAndOnesOnly(String str) {
        if (str == null) {
            throw new InvalidInputFormatException();
        }
        Pattern pattern = Pattern.compile("^[01]+$");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    public static List<Integer> getCorrectionBytes(int[] decimalArr, int correctionBytesPerBlock) {
        int[] generatingPolynomial = GeneratingPolynomialMap.getGeneratingPolynomial(correctionBytesPerBlock);
        List<Integer> listCorrectBytes = Arrays.stream(decimalArr)
                .boxed()
                .collect(Collectors.toCollection(ArrayList::new));
        while (listCorrectBytes.size() < generatingPolynomial.length) {
            listCorrectBytes.add(0);
        }

        for (int count = 0; count < decimalArr.length; count++) {
            int aValue = listCorrectBytes.remove(0);
            listCorrectBytes.add(0);
            if (aValue == 0) {
                continue;
            }
            int bValue = ABMap.getBackGaluaFieldValue(aValue);
            if (listCorrectBytes.size() < correctionBytesPerBlock) {
                listCorrectBytes.add(0);
            }

            for (int i = 0; i < correctionBytesPerBlock; i++) {
                int cValue = (generatingPolynomial[i] + bValue) % 255;
                int dValue = CDMap.getGaluaFieldValue(cValue);
                listCorrectBytes.set(i, dValue ^ listCorrectBytes.get(i));
            }
        }
        return listCorrectBytes;
    }

    public static Block[] splitIntoBlocks(int[] inputDecimalArr, int blocksCount) {
        int quotient = inputDecimalArr.length / blocksCount;
        int remainder = inputDecimalArr.length % blocksCount;
        Block[] blocks = new Block[blocksCount];

        int counter=0;
        for (int i = 0; i < blocksCount; i++) {
            int[] perBlockDecimalArr = new int[(i < (blocksCount - remainder)) ? quotient : (quotient + 1)];

            for (int j = 0; j < perBlockDecimalArr.length; j++) {
                perBlockDecimalArr[j] = inputDecimalArr[counter++];
            }
            blocks[i] = new Block(perBlockDecimalArr);
        }
        return blocks;
    }

    public static void calculateCorrectionBytes(Block[] blocks, int correctionBytesPerBlock) {
        for (Block block : blocks) {
            int[] decimalArr = block.getDecimalArr();
            List<Integer> listCorrectBytes = UtilityMethods.getCorrectionBytes(decimalArr, correctionBytesPerBlock);
            block.setListCorrectBytes(listCorrectBytes);
        }
    }

    public static String[] uniteBlocks(Block[] blocks) {

        LinkedList<List<Integer>> decimalArraysList = new LinkedList<>();
        LinkedList<LinkedList<Integer>> correctionBytesList = new LinkedList<>();

        List<Integer> decimalIntegersList = new LinkedList<>();
        List<Integer> correctionIntegersList = new LinkedList<>();
        for (Block block : blocks) {
            LinkedList<Integer> decimalList = new LinkedList<>();
            for (int intValue : block.getDecimalArr()) {
                decimalList.add(intValue);
            }
            decimalArraysList.add(decimalList);
            correctionBytesList.add(new LinkedList<>(block.getListCorrectBytes()));
        }

        while (!decimalArraysList.isEmpty()) {
            for (List<Integer> currentList : decimalArraysList) {
                decimalIntegersList.add(currentList.remove(0));
            }
            decimalArraysList = decimalArraysList.stream()
                    .filter(list -> !list.isEmpty())
                    .collect(Collectors.toCollection(LinkedList::new));
        }

        while (!correctionBytesList.isEmpty()) {
            for (List<Integer> currentList : correctionBytesList) {
                correctionIntegersList.add(currentList.remove(0));
            }
            correctionBytesList = correctionBytesList.stream()
                    .filter(list -> !list.isEmpty())
                    .collect(Collectors.toCollection(LinkedList::new));
        }


        String[] resList = new String[decimalIntegersList.size() + correctionIntegersList.size()];
        int i = 0;
        for (int intVal : decimalIntegersList) {
            resList[i++] = String.valueOf(intVal);
        }
        for (int intVal : correctionIntegersList) {
            resList[i++] = String.valueOf(intVal);
        }
        return resList;
    }
}
