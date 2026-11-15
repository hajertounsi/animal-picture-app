package com.example.animalpictures;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToAnimalTypeConverter implements Converter<String, AnimalType> {

    @Override
    public AnimalType convert(String source) {
        return AnimalType.valueOf(source.trim().toUpperCase());
    }
}
