package com.example.bankcards.util;

import java.security.SecureRandom;

public class CardUtil {

    private static final String mask = "**** **** **** ";

    public static String maskingNumber(String cardNumber) {

        return mask + cardNumber.substring(cardNumber.length() - 4);
    }

    public static String generateRandomNumberCard(){
        SecureRandom random = new SecureRandom();
        StringBuilder builder = new StringBuilder();
        builder.append(random.nextInt(9) + 1);
        for (int i = 0; i < 15; i++){
            builder.append(random.nextInt(10));
        }

        return builder.toString();
    }
}
