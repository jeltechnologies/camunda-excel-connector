package com.jeltechnologies.camunda.connectors.excel.camunda;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonUtils {
    private static final String ISO_8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonUtils.class);

    public String toJSON(Object object) {
        return toJSON(object, false);
    }

    public String toJSON(Object object, boolean prettyPrint) {
        String json;
        if (object == null) {
            json = null;
        }
        ObjectMapper mapper = new ObjectMapper();
        if (prettyPrint) {
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
        }
        mapper.registerModule(new JavaTimeModule());
        mapper.setDateFormat(new SimpleDateFormat(ISO_8601));
        mapper.setSerializationInclusion(Include.NON_NULL);

        try {
            json = mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOGGER.warn("Error converting to JSON " + e.getMessage() + " for " + object.toString());
            json = e.getMessage();
        }
        return json;
    }

    public Object fromJSON(String json, Class<?> clazz) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_NULL);
        return mapper.readValue(json, clazz);
    }

}
