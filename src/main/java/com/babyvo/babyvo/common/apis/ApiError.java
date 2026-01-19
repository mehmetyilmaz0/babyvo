package com.babyvo.babyvo.common.apis;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private String detail; // developer-friendly veya UI friendly detay
    private List<FieldErrorItem> fieldErrors; // validation hataları

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FieldErrorItem {
        private String field;
        private String message;
    }
}
