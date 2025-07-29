package com.server.api.presentation.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Clase genérica para estandarizar las respuestas de la API.
 * Implementa el principio DRY al centralizar el formato de respuesta.
 * Sigue el principio KISS manteniendo una estructura simple y clara.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    private Object errors;

    /**
     * Constructor para respuestas exitosas con datos.
     */
    public ApiResponse(String message, T data) {
        this.success = true;
        this.message = message;
        this.data = data;
    }

    /**
     * Constructor para respuestas de error.
     */
    public ApiResponse(String message, Object errors, boolean success) {
        this.success = success;
        this.message = message;
        this.errors = errors;
    }

    /**
     * Método estático para crear respuesta exitosa.
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(message, data);
    }

    /**
     * Método estático para crear respuesta de error.
     */
    public static <T> ApiResponse<T> error(String message, Object errors) {
        return new ApiResponse<>(message, errors, false);
    }

    /**
     * Método estático para crear respuesta de error simple.
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(message, null, false);
    }

    // Getters y setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Object getErrors() {
        return errors;
    }

    public void setErrors(Object errors) {
        this.errors = errors;
    }
}
