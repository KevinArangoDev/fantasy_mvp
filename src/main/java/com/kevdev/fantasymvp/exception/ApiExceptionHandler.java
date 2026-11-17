package com.kevdev.fantasymvp.exception;

import com.kevdev.fantasymvp.controller.EquipoRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = EquipoRestController.class)
public class ApiExceptionHandler {

    public record ErrorApi(String error) {}

    @ExceptionHandler(ReglaFichajeException.class)
    public ResponseEntity<ErrorApi> reglaIncumplida(ReglaFichajeException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorApi(e.getMessage()));
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorApi> noEncontrado(RecursoNoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorApi(e.getMessage()));
    }
}