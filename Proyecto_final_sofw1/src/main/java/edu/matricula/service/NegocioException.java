package edu.matricula.service;

import org.springframework.http.HttpStatus;

/** Violación de una regla de negocio (RN-xx / RC-xx) o recurso inexistente. */
public class NegocioException extends RuntimeException {

    private final String regla;
    private final HttpStatus status;

    public NegocioException(String regla, String mensaje) {
        this(regla, mensaje, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    public NegocioException(String regla, String mensaje, HttpStatus status) {
        super(mensaje);
        this.regla = regla;
        this.status = status;
    }

    public static NegocioException noEncontrado(String mensaje) {
        return new NegocioException("NO_ENCONTRADO", mensaje, HttpStatus.NOT_FOUND);
    }

    public static NegocioException conflicto(String regla, String mensaje) {
        return new NegocioException(regla, mensaje, HttpStatus.CONFLICT);
    }

    public String getRegla() { return regla; }
    public HttpStatus getStatus() { return status; }
}
