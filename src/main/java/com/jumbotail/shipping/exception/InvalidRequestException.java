package com.jumbotail.shipping.exception;

/**
 * Exception thrown when request parameters are invalid
 */
public class InvalidRequestException extends RuntimeException {
    
    public InvalidRequestException(String message) {
        super(message);
    }
    
    public InvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
