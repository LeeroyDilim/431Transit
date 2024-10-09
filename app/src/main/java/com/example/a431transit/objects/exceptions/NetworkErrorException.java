package com.example.a431transit.objects.exceptions;

public class NetworkErrorException extends RuntimeException{
    public NetworkErrorException(String message) {
        super(message);
    }
}
