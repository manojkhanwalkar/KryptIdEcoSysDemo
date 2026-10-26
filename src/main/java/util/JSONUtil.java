package util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;

public class JSONUtil {

    /*
     * Polymorphic type handling is needed for the Merkle tree (Node interface) and collection fields,
     * but unrestricted default typing lets a sender name any class on the classpath and trigger
     * deserialization gadgets. Only the application's own data packages and JDK collections/time
     * types may be instantiated. The wire format is unchanged (OBJECT_AND_NON_CONCRETE, wrapper array).
     */
    static final PolymorphicTypeValidator TYPE_VALIDATOR = BasicPolymorphicTypeValidator.builder()
            .allowIfSubType("data.")
            .allowIfSubType("shareablemerkletree.")
            .allowIfSubType("blockchain.")
            .allowIfSubType("services.discovery.data.")
            .allowIfSubType("java.util.")
            .allowIfSubType("java.time.")
            .allowIfSubType("java.lang.String")
            .allowIfSubType("java.lang.Integer")
            .allowIfSubType("java.lang.Long")
            .allowIfSubType("java.lang.Boolean")
            .allowIfSubType("java.lang.Double")
            .allowIfSubTypeIsArray()
            .build();

    static ObjectMapper mapper = new ObjectMapper();


    static {
        mapper.registerModule(new JavaTimeModule());
        enableSafeDefaultTyping(mapper);
    }


    public static ObjectMapper enableSafeDefaultTyping(ObjectMapper objectMapper)
    {
        objectMapper.activateDefaultTyping(TYPE_VALIDATOR, ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
        return objectMapper;
    }


    public static String toJSON(Object object)
    {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static Object fromJSON(String json, Class<?> clazz)
    {
        try {
            return mapper.readValue(json,clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String toPrettyJSON(Object object)
    {
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
    }


}
