package com.example.a431transit.objects.exceptions;

public class BadRequestException extends RuntimeException{
    public BadRequestException(String s) {
        super(s);
    }
}
