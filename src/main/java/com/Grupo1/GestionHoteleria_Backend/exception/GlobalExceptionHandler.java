package com.Grupo1.GestionHoteleria_Backend.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.Grupo1.GestionHoteleria_Backend.dto.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException exception) {
		return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), null);
	}

	@ExceptionHandler(HabitacionNumeroAlreadyExistsException.class)
	public ResponseEntity<ErrorResponse> handleHabitacionNumeroAlreadyExists(HabitacionNumeroAlreadyExistsException exception) {
		return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), null);
	}

	@ExceptionHandler(HabitacionNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleHabitacionNotFound(HabitacionNotFoundException exception) {
		return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), null);
	}

	@ExceptionHandler(UsuarioNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleUsuarioNotFound(UsuarioNotFoundException exception) {
		return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), null);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentials() {
		return buildResponse(HttpStatus.UNAUTHORIZED, "Email o contrasena incorrectos", null);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
		Map<String, String> validations = new HashMap<>();
		exception.getBindingResult().getFieldErrors()
				.forEach(error -> validations.put(error.getField(), error.getDefaultMessage()));

		return buildResponse(HttpStatus.BAD_REQUEST, "Datos de entrada invalidos", validations);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleMessageNotReadable() {
		return buildResponse(HttpStatus.BAD_REQUEST, "El cuerpo de la peticion tiene datos invalidos", null);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
		Map<String, String> validations = new HashMap<>();
		validations.put(exception.getName(), "Valor invalido para el parametro " + exception.getName());

		return buildResponse(HttpStatus.BAD_REQUEST, "Parametros de entrada invalidos", validations);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneral(Exception exception) {
		return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), null);
	}

	private ResponseEntity<ErrorResponse> buildResponse(
			HttpStatus status,
			String message,
			Map<String, String> validations
	) {
		ErrorResponse response = new ErrorResponse(
				LocalDateTime.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				validations
		);

		return ResponseEntity.status(status).body(response);
	}
}
