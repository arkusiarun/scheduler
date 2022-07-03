package com.airtel.scheduler.scheduling.configurations;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.PackageVersion;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class CustomMapperModule extends SimpleModule {

    private static final long serialVersionUID = 1L;
    private static final String NAME = "customModule";

    public CustomMapperModule() {
        super(NAME, PackageVersion.VERSION);
        this.addSerializer(OffsetDateTime.class, new JsonSerializer<OffsetDateTime>() {
            @Override
            public void serialize(OffsetDateTime offsetDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeObject(offsetDateTime.toInstant().toEpochMilli());
            }
        });

        this.addSerializer(ZonedDateTime.class, new JsonSerializer<ZonedDateTime>() {
            @Override
            public void serialize(ZonedDateTime zonedDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeObject(zonedDateTime.toInstant().toEpochMilli());
            }
        });
        this.addSerializer(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
            @Override
            public void serialize(LocalDateTime localDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeObject(localDateTime.atZone(ZoneOffset.systemDefault()).toInstant().toEpochMilli());
            }
        });
        this.addSerializer(org.joda.time.LocalDateTime.class, new JsonSerializer<org.joda.time.LocalDateTime>() {
            @Override
            public void serialize(org.joda.time.LocalDateTime localDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
                jsonGenerator.writeObject(localDateTime.toDateTime().getMillis());
            }
        });
    }
}