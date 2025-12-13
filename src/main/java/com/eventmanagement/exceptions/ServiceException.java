package com.eventmanagement.exceptions;

public class ServiceException extends RuntimeException {
   public ServiceException(String message) {
       super(message);
   }
}
