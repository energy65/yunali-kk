package com.fongmi.android.tv.utils;

import android.util.Base64;

public class EncryptUtil {

    private static final String KEY1 = "ylys_2027";
    private static final String KEY2 = "fongmi_tv";

    public static String decrypt(String encrypted) {
        byte[] decoded = Base64.decode(encrypted, Base64.DEFAULT);
        return xor(new String(decoded), KEY1);
    }

    public static String encrypt(String plain) {
        String encrypted = xor(plain, KEY1);
        return Base64.encodeToString(encrypted.getBytes(), Base64.DEFAULT);
    }

    public static String encryptMulti(String plain) {
        String layer1 = Base64.encodeToString(xor(plain, KEY1).getBytes(), Base64.NO_WRAP);
        String reversed = new StringBuilder(layer1).reverse().toString();
        return Base64.encodeToString(xor(reversed, KEY2).getBytes(), Base64.NO_WRAP);
    }

    public static String decryptMulti(String encrypted) {
        String reversed = xor(new String(Base64.decode(encrypted, Base64.DEFAULT)), KEY2);
        String layer1 = new StringBuilder(reversed).reverse().toString();
        return xor(new String(Base64.decode(layer1, Base64.DEFAULT)), KEY1);
    }

    private static String xor(String text, String key) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            result.append((char) (text.charAt(i) ^ key.charAt(i % key.length())));
        }
        return result.toString();
    }
}