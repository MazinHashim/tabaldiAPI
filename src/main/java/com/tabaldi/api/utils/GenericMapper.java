package com.tabaldi.api.utils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.List;

@NoArgsConstructor
public class GenericMapper {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String objectToJSONMapper(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    public static <T> T jsonToObjectMapper(String object, Class<T> responseClassType) throws JsonParseException, JsonMappingException, IOException {
        T classObject = objectMapper.readValue(object, responseClassType);
        return classObject;
    }

    public static <T> List<T> jsonToListObjectMapper(String object, Class<T> responseClassType) throws JsonParseException, JsonMappingException, IOException {
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        List<T> list = (List)objectMapper.readValue(object, typeFactory.constructCollectionType(List.class, responseClassType));
        return list;
    }

    public static <T> T convertListToObject(Object object, TypeReference<T> typeReference) throws JsonGenerationException, JsonMappingException, IOException {
        return objectMapper.convertValue(object, typeReference);
    }

    static {
        objectMapper.registerModule(new JavaTimeModule());
    }
}
