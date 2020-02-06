package ru.nanit.vasyascheduler.api.util;

public final class StringUtil {

    private StringUtil(){}

    /**
     * Levenshtein algorithm required to compare two strings to similarity
     * @param str1 First string
     * @param str2 Second string
     * @return Number of modifications of first string to convert it to a second
     */
    public static int levenshtein(String str1, String str2) {
        if(str1 == null || str2 == null){
            return -1;
        }

        int[] Di_1 = new int[str2.length() + 1];
        int[] Di = new int[str2.length() + 1];

        for (int j = 0; j <= str2.length(); j++) {
            Di[j] = j;
        }

        for (int i = 1; i <= str1.length(); i++) {
            System.arraycopy(Di, 0, Di_1, 0, Di_1.length);

            Di[0] = i;
            for (int j = 1; j <= str2.length(); j++) {
                int cost = (str1.charAt(i - 1) != str2.charAt(j - 1)) ? 1 : 0;
                Di[j] = min(
                        Di_1[j] + 1,
                        Di[j - 1] + 1,
                        Di_1[j - 1] + cost
                );
            }
        }

        return Di[Di.length - 1];
    }

    private static int min(int n1, int n2, int n3) {
        return Math.min(Math.min(n1, n2), n3);
    }

}
