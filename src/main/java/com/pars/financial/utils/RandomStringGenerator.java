package com.pars.financial.utils;

import java.util.Random;

public class RandomStringGenerator {

    private static final String UPPERCASE_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String UPPERCASE_LETTERS_AND_NUMBERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String NUMBERS = "0123456789";
    private static final String HEX_UPPERCASE_LETTERS = "0123456789ABCDEF";
    private static final Random random = new Random();

    /**
     * Generates a random hexadecimal uppercase string of the specified length.
     *
     * @param length The length of the random uppercase string.
     * @return A random hexadecimal uppercase string.
     */
    public static String generateRandomHexString(int length) {
        return generateRandomString(HEX_UPPERCASE_LETTERS, length);
    }


    /**
     * Generates a random uppercase string of the specified length.
     *
     * @param length The length of the random uppercase string.
     * @return A random uppercase string.
     */
    public static String generateRandomUppercaseString(int length) {
        return generateRandomString(UPPERCASE_LETTERS, length);
    }

    /**
     * Generates a random uppercase string of the specified length.
     *
     * @param length The length of the random uppercase string.
     * @return A random uppercase string.
     */
    public static String generateRandomUppercaseStringWithNumbers(int length) {
        return generateRandomString(UPPERCASE_LETTERS_AND_NUMBERS, length);
    }

    /**
     * Generates a random numeric string of the specified length.
     *
     * @param length The length of the random numeric string.
     * @return A random numeric string.
     */
    public static String generateRandomNumericString(int length) {
        return generateRandomString(NUMBERS, length);
    }

    /**
     * Helper method to generate a random string from a given character set.
     *
     * @param characterSet The set of characters to choose from.
     * @param length       The length of the random string.
     * @return A random string.
     */
    private static String generateRandomString(String characterSet, int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be a positive integer.");
        }

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characterSet.length());
            char ch = characterSet.charAt(randomIndex);
            sb.append(ch);
        }
        return sb.toString();
    }

    // Example usage (removed main method to avoid VS Code main class selection prompt)
    // To test this class, use:
    // String randomUppercase = RandomStringGenerator.generateRandomUppercaseString(10);
    // String randomNumeric = RandomStringGenerator.generateRandomNumericString(8);
}
