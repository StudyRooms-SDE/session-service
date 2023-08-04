package com.sde.project.session.models.responses;

public record ExceptionResponse(String timestamp, int status, String error, String message, String path) {


}
