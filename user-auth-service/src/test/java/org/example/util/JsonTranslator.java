package org.example.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonTranslator {

    public JsonTranslator() {
    }

    public static final ObjectMapper MAPPER = new ObjectMapper()
            .findAndRegisterModules();


}
