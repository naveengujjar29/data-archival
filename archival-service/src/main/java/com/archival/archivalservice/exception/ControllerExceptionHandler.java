package com.archival.archivalservice.exception;

import com.archival.archivalservice.controller.ArchivalController;
import com.archival.archivalservice.dto.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Naveen Kumar
 */
@ControllerAdvice(assignableTypes = {ArchivalController.class})
@ResponseBody
public class ControllerExceptionHandler {

    @ExceptionHandler(EntityDoesNotExistException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    protected MessageResponse entityDoesNotExistException(final EntityDoesNotExistException ex) {
        return new MessageResponse(ex.getMessage());
    }

    @ExceptionHandler(PermissionDeniedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    protected MessageResponse permissionDeniedException(final PermissionDeniedException ex) {
        return new MessageResponse(ex.getMessage());
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    protected MessageResponse handleException(final Exception ex) {
        return new MessageResponse(ex.getMessage());
    }

}
