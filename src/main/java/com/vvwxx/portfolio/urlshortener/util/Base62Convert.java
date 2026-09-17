package com.vvwxx.portfolio.urlshortener.util;


public class Base62Convert {

    private static final String ALLOWED_CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String encode(long id) {

        StringBuilder sb = new StringBuilder();

        if (id == 0) {
            return String.valueOf(ALLOWED_CHARACTERS.charAt(0));
        }

        while (id > 0) {
            int remainder = (int) (id % 62);
            sb.append(ALLOWED_CHARACTERS.charAt(remainder));
            id = id / 62;
        }

        return sb.reverse().toString();
    }

    public static long decode(String shortCode) {
        long id = 0;

        for (int i=0; i<shortCode.length(); i++) {
            char c = shortCode.charAt(i);
            int val = ALLOWED_CHARACTERS.indexOf(c);

            if (val == -1) {
                throw new IllegalArgumentException("Invalid character in short code: " + c);
            }

            id = id * 62 + val;
        }

        return id;
    }
}
