package com.paradigma.rt.streaming.kstreams.fraudchecker.serializers;

import com.google.gson.Gson;
import com.paradigma.rt.streaming.kstreams.fraudchecker.model.FraudCase;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class JsonDeserializer<T> implements Deserializer<T> {
    private Gson gson = new Gson();
    private Class<T> deserializedClass;

    private static Class VALUE_DEFAULT_TYPE = FraudCase.class;

    public JsonDeserializer(Class<T> deserializedClass) {
        this.deserializedClass = deserializedClass;
    }

    public JsonDeserializer() {
        this.deserializedClass = VALUE_DEFAULT_TYPE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void configure(Map<String, ?> map, boolean b) {
        if(deserializedClass == null) {
            deserializedClass = (Class<T>) map.get("serializedClass");
        }
    }

    @Override
    public T deserialize(String s, byte[] bytes) {
        if(bytes == null){
            return null;
        }

        return gson.fromJson(new String(bytes), deserializedClass);

    }

    @Override
    public void close() {

    }
}