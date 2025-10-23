package com.example.bankcards.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.stereotype.Component;

/**
 * JPA-конвертер для шифрования и дешифрования строковых атрибутов сущностей при сохранении в базу данных.
 * Использует {@link StringEncryptor} для выполнения шифрования при записи в базу данных
 * и дешифрования при чтении из базы.
 */
@Component
@Converter
@RequiredArgsConstructor
public class EncryptionConverter implements AttributeConverter<String, String> {

    private final StringEncryptor stringEncryptor;

    /**
     * Шифрует атрибут перед сохранением в базу данных.
     *
     * @param attribute строковый атрибут сущности
     * @return зашифрованное значение для хранения в базе или {@code null}, если атрибут {@code null}
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }

        return stringEncryptor.encrypt(attribute);
    }

    /**
     * Дешифрует значение, извлеченное из базы данных.
     *
     * @param dbData зашифрованное значение из базы
     * @return расшифрованный атрибут сущности или {@code null}, если значение из базы {@code null}
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        return stringEncryptor.decrypt(dbData);
    }
}
