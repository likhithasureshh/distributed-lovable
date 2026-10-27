package com.projects.distributed_lovable.common_lib.errors;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

public record ApiError(
        HttpStatus status,
        String message,
        Instant timeStamp,
       @JsonInclude(JsonInclude.Include.NON_NULL) List<ApiFieldError> subErrors
) {
    public ApiError(HttpStatus status,String message)
    {
         this(status,message,Instant.now(),null);
    }
    public ApiError(HttpStatus status,String message,List<ApiFieldError> subErrors)
    {
        this(status,message,Instant.now(),subErrors);
    }
}

record ApiFieldError(String field,String message){}
