package com.babyvo.babyvo.service.auth;

import com.babyvo.babyvo.common.exception.BusinessException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GoogleTokenVerifier {

    @Value("${babyvo.auth.google.client-id}")
    private String googleClientId;

    public GoogleIdToken.Payload verify(String idTokenString) {
        try {
            var transport = new NetHttpTransport();
            var jsonFactory = GsonFactory.getDefaultInstance();

            var verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                    .setAudience(List.of(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new BusinessException(HttpStatus.UNAUTHORIZED, "INVALID_GOOGLE_ID_TOKEN");
            }
            return idToken.getPayload();
        } catch (Exception e) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "INVALID_GOOGLE_ID_TOKEN");
        }
    }
}