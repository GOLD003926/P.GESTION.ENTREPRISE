package com.gntfh.sirh_api.common.exception;

import com.gntfh.sirh_api.common.dto.ApiError;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * Transforme TOUTES les exceptions en réponses au format {@link ApiError}.
 * Les contrôleurs et services n'ont jamais de try/catch pour cela.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final HttpStatusCode UNPROCESSABLE = HttpStatusCode.valueOf(422);

    // =====================================================================
    // Erreurs métier
    // =====================================================================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex, WebRequest request) {
        return build(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateResourceException ex, WebRequest request) {
        return build(HttpStatus.CONFLICT, "DUPLICATE_RESOURCE", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ApiError> handleInvalidTransition(InvalidStateTransitionException ex, WebRequest request) {
        return build(UNPROCESSABLE, "INVALID_STATE_TRANSITION", ex.getMessage(), request);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiError> handleBusinessRule(BusinessRuleException ex, WebRequest request) {
        return build(UNPROCESSABLE, "BUSINESS_RULE_VIOLATION", ex.getMessage(), request);
    }

    // =====================================================================
    // Sécurité
    // =====================================================================

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "BAD_CREDENTIALS", "Email ou mot de passe incorrect.", request);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiError> handleDisabled(DisabledException ex, WebRequest request) {
        return build(HttpStatus.FORBIDDEN, "ACCOUNT_DISABLED",
                "Ce compte est désactivé. Contactez l'administrateur.", request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex, WebRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentification requise.", request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        return build(HttpStatus.FORBIDDEN, "FORBIDDEN",
                "Vous n'avez pas les droits nécessaires pour cette action.", request);
    }

    // =====================================================================
    // Base de données
    // =====================================================================

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleOptimisticLock(ObjectOptimisticLockingFailureException ex,
                                                         WebRequest request) {
        return build(HttpStatus.CONFLICT, "CONCURRENT_MODIFICATION",
                "Cette ressource a été modifiée entre-temps par quelqu'un d'autre. Rechargez puis réessayez.",
                request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, WebRequest request) {
        // Le détail SQL est loggé mais jamais renvoyé au client.
        log.warn("Violation d'intégrité sur {} : {}", path(request), ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION",
                "L'opération est en conflit avec des données existantes.", request);
    }

    // =====================================================================
    // Validation
    // =====================================================================

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        List<ApiError.FieldError> errors = ex.getConstraintViolations().stream()
                .map(v -> new ApiError.FieldError(v.getPropertyPath().toString(), v.getMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Certains champs sont invalides.", request, errors);
    }

    /** Corps JSON invalide : @Valid sur un @RequestBody. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        List<ApiError.FieldError> errors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> errors.add(new ApiError.FieldError(fe.getField(), fe.getDefaultMessage())));
        ex.getBindingResult().getGlobalErrors()
                .forEach(ge -> errors.add(new ApiError.FieldError(ge.getObjectName(), ge.getDefaultMessage())));
        ApiError body = ApiError.of(status, "VALIDATION_ERROR", "Certains champs sont invalides.",
                path(request), errors);
        return handleExceptionInternal(ex, body, headers, status, request);
    }

    /** Paramètres de méthode invalides : @RequestParam, @PathVariable avec contraintes. */
    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
                                                                            HttpHeaders headers,
                                                                            HttpStatusCode status,
                                                                            WebRequest request) {
        List<ApiError.FieldError> errors = new ArrayList<>();
        ex.getParameterValidationResults().forEach(result -> {
            String name = result.getMethodParameter().getParameterName();
            result.getResolvableErrors()
                    .forEach(err -> errors.add(new ApiError.FieldError(name, err.getDefaultMessage())));
        });
        ApiError body = ApiError.of(status, "VALIDATION_ERROR", "Certains paramètres sont invalides.",
                path(request), errors);
        return handleExceptionInternal(ex, body, headers, status, request);
    }

    // =====================================================================
    // Erreurs standard de Spring MVC (route inconnue, JSON illisible, etc.)
    // =====================================================================

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        if (body instanceof ApiError) {
            return super.handleExceptionInternal(ex, body, headers, statusCode, request);
        }
        ApiError error = ApiError.of(statusCode, codeFor(statusCode), messageFor(ex, statusCode), path(request));
        return super.handleExceptionInternal(ex, error, headers, statusCode, request);
    }

    // =====================================================================
    // Filet de sécurité : toute erreur imprévue
    // =====================================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, WebRequest request) {
        log.error("Erreur inattendue sur {}", path(request), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Une erreur interne est survenue. Réessayez plus tard.", request);
    }

    // =====================================================================
    // Utilitaires
    // =====================================================================

    private ResponseEntity<ApiError> build(HttpStatusCode status, String code, String message,
                                           WebRequest request) {
        return build(status, code, message, request, List.of());
    }

    private ResponseEntity<ApiError> build(HttpStatusCode status, String code, String message,
                                           WebRequest request, List<ApiError.FieldError> fieldErrors) {
        return ResponseEntity.status(status).body(ApiError.of(status, code, message, path(request), fieldErrors));
    }

    private static String path(WebRequest request) {
        return request instanceof ServletWebRequest servletRequest
                ? servletRequest.getRequest().getRequestURI()
                : request.getDescription(false);
    }

    private static String codeFor(HttpStatusCode status) {
        HttpStatus resolved = HttpStatus.resolve(status.value());
        return resolved != null ? resolved.name() : "HTTP_" + status.value();
    }

    private static String messageFor(Exception ex, HttpStatusCode status) {
        if (ex instanceof HttpMessageNotReadableException) {
            return "Corps de la requête illisible ou invalide.";
        }
        if (ex instanceof MissingServletRequestParameterException missing) {
            return "Paramètre obligatoire manquant : " + missing.getParameterName() + ".";
        }
        if (ex instanceof TypeMismatchException mismatch) {
            return "Valeur invalide pour le paramètre « " + mismatch.getPropertyName() + " ».";
        }
        if (ex instanceof HttpRequestMethodNotSupportedException) {
            return "Méthode HTTP non supportée pour cette route.";
        }
        if (ex instanceof HttpMediaTypeNotSupportedException) {
            return "Type de contenu non supporté.";
        }
        if (ex instanceof NoResourceFoundException) {
            return "Ressource introuvable.";
        }
        return status.is5xxServerError() ? "Erreur interne du serveur." : "Requête invalide.";
    }
}
