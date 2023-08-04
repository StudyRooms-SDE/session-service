package com.sde.project.session.configurations;

import com.sde.project.session.models.responses.ExceptionResponse;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.PermissionDeniedDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.time.Instant;

@ControllerAdvice
@OpenAPIDefinition
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {DataIntegrityViolationException.class, DuplicateKeyException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    @ApiResponse(content = @Content(schema = @Schema(implementation = ExceptionResponse.class), mediaType = "application/json"))
    protected ResponseEntity<Object> handleConflict(RuntimeException ex, WebRequest request) {
        ExceptionResponse responseBody = new ExceptionResponse(
                Instant.now().toString(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(responseBody, new HttpHeaders(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = {PermissionDeniedDataAccessException.class, AccessDeniedException.class, IllegalAccessError.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ApiResponse(content = @Content(schema = @Schema(implementation = ExceptionResponse.class), mediaType = "application/json"))
    protected ResponseEntity<Object> handlePermissionDenied(RuntimeException ex, WebRequest request) {
        ExceptionResponse responseBody = new ExceptionResponse(Instant.now().toString(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(responseBody, new HttpHeaders(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(value = {DataRetrievalFailureException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = ExceptionResponse.class), mediaType = "application/json"))
    protected ResponseEntity<Object> handleDataRetrievalFailure(RuntimeException ex, WebRequest request) {
        ExceptionResponse responseBody = new ExceptionResponse(Instant.now().toString(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(responseBody, new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {IllegalArgumentException.class, IllegalStateException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = ExceptionResponse.class), mediaType = "application/json"))
    protected ResponseEntity<Object> handleBadRequest(RuntimeException ex, WebRequest request) {
        ExceptionResponse responseBody = new ExceptionResponse(Instant.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(responseBody, new HttpHeaders(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = {Exception.class, RuntimeException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ApiResponse(content = @Content(schema = @Schema(implementation = ExceptionResponse.class), mediaType = "application/json"))
    protected ResponseEntity<Object> handleOtherExceptions(RuntimeException ex, WebRequest request) {
        ExceptionResponse responseBody = new ExceptionResponse(Instant.now().toString(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(responseBody, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
