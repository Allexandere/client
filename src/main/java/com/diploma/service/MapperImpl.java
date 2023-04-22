package com.diploma.service;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mashape.unirest.http.ObjectMapper;
import lombok.SneakyThrows;

public class MapperImpl extends com.fasterxml.jackson.databind.ObjectMapper implements ObjectMapper {
    @Override
    @SneakyThrows
    public <T> T readValue(String s, Class<T> aClass) {
        return super.readValue(s, aClass);
    }

    @Override
    @SneakyThrows
    public String writeValue(Object o) {
        return this.writeValueAsString(o);
    }

    public MapperImpl() {
        super();
        this.registerModule(new JavaTimeModule());
    }
}
