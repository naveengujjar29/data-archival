package com.archival.archivalservice.exception;


/**
 * @author Naveen Kumar
 */
public class EntityDoesNotExistException extends Throwable {

    public EntityDoesNotExistException(String s) {
    }

    public EntityDoesNotExistException() {
        super();
    }

    public EntityDoesNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntityDoesNotExistException(Throwable cause) {
        super(cause);
    }

    protected EntityDoesNotExistException(String message, Throwable cause, boolean enableSuppression,
                                          boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
