package com.dev.tagashira.exception;

//Handles the exception that the parameter value does not exist is passed into the database for all entities
public class UserInfoException extends Exception {
    public UserInfoException(String message){
        super(message);
    }
}
