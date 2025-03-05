package com.archival.archivalservice.dto;

/**
 * @author Naveen Kumar
 */
public class MessageResponse {

    public MessageResponse(String message) {
        this.message = message;
    }

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
