package com.example.bankcards.util;

public class MaskUtil {

    private static final String mask = "**** **** **** ";

    public static String maskingNumber(String cardNumber) {

        return mask + cardNumber.substring(cardNumber.length() - 4);
    }
}
