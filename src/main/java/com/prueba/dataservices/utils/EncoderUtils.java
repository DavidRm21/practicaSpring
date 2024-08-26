package com.prueba.dataservices.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.prueba.dataservices.entity.ErrorDatabase;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Log4j2
@Configuration
@RequiredArgsConstructor
public class EncoderUtils {

    private final Gson gson;

    public String serializedJson(Object response){
        String jsonString = gson.toJson(response);
        return encodeBase64(jsonString);
    }

    public String deserializedJson(String json){
        String decoded = decodeBase64(json);
        Type tipoListaRegistros = new TypeToken<List<ErrorDatabase>>() {}.getType();
        List<ErrorDatabase> listError = gson.fromJson(decoded, tipoListaRegistros);
        return encodeBase64(listError.toString());
    }

    public String encodeBase64(Object data){
        return Base64.getEncoder().encodeToString(data.toString().getBytes());
    }

    public String decodeBase64(String encodedData){
        byte[] decodedData = Base64.getDecoder().decode(encodedData);
        return new String(decodedData, StandardCharsets.UTF_8);
    }
}
