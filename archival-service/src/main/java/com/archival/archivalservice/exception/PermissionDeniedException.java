package com.archival.archivalservice.exception;


/**
 * @author Naveen Kumar
 */
public class PermissionDeniedException extends Exception {

    public PermissionDeniedException(String s) {
        super(s);
    }

    public PermissionDeniedException() {
        super();
    }

    public PermissionDeniedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PermissionDeniedException(Throwable cause) {
        super(cause);
    }

    protected PermissionDeniedException(String message, Throwable cause, boolean enableSuppression,
                                        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
