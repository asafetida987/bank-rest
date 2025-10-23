package com.example.bankcards.util;

import java.security.SecureRandom;

/**
 * Утилитарный класс для работы с номерами банковских карт.
 * Предоставляет методы для маскирования номера карты и генерации случайного номера карты.
 */
public class CardUtil {

    private static final String mask = "**** **** **** ";

    /**
     * Маскирует номер карты, оставляя видимыми только последние 4 цифры.
     *
     * @param cardNumber полный номер карты
     * @return замаскированный номер карты в формате "**** **** **** 1234"
     */
    public static String maskingNumber(String cardNumber) {

        return mask + cardNumber.substring(cardNumber.length() - 4);
    }

    /**
     * Генерирует случайный номер карты длиной 16 цифр.
     * Первая цифра не равна 0.
     *
     * @return случайный номер карты в виде строки
     */
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
