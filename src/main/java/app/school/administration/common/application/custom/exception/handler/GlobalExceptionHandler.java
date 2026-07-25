package app.school.administration.common.application.custom.exception.handler;

import app.school.administration.common.api.response.AppResponse;
import app.school.administration.common.api.response.FieldErrorResponse;
import app.school.administration.common.application.custom.exception.CustomNullPointerException;
import app.school.administration.common.application.custom.exception.EntityNotFoundedException;
import app.school.administration.common.application.custom.exception.NoDataFoundException;
import com.fasterxml.jackson.core.JsonParseException;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.NonUniqueResultException;
import org.hibernate.TypeMismatchException;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.exception.SQLGrammarException;
import org.springframework.beans.NotWritablePropertyException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.net.BindException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * ====================================================================================
 * ADVICE: GlobalExceptionHandler
 * ====================================================================================
 *
 * <h2>FUNCTIONALITY</h2>
 * Centralized exception handler intercepting all application exceptions across Spring Controllers using {@link RestControllerAdvice}.
 * Translates Java, Spring Data, Security, SQL, and HTTP exceptions into a uniform JSON response structure ({@link AppResponse}).
 *
 * <h2>WHY IMPLEMENTED</h2>
 * User experience and API security:
 * - Formats all REST error outputs with standard timestamp, HTTP status code, request URL, message, and field errors list.
 * - Prevents raw stack traces and internal database schemas from leaking to external callers.
 * - Dynamically registers specialized exception handlers for database exceptions (`SQLException`, `ConstraintViolationException`),
 *   validation failures (`MethodArgumentNotValidException`), authentication failures (`BadCredentialsException`), and custom business exceptions.
 *
 * <h2>WHAT HAPPENS IF NOT IMPLEMENTED</h2>
 * - Uncaught exceptions will return raw HTTP 500 HTML pages or unformatted Spring trace objects to client applications.
 * - Internal system implementation details and database query paths will be exposed to potential attackers.
 * ====================================================================================
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private Map<Class<? extends Throwable>, BiFunction<Throwable, HttpServletRequest, ResponseEntity<AppResponse>>> exceptionHandlers;

    public GlobalExceptionHandler() {
        this.exceptionHandlers = new HashMap<>();
        exceptionRegister(Exception.class, this::defaultError);


        exceptionRegister(SQLException.class, this::sqlExceptionHandler);
        exceptionRegister(InvalidDataAccessApiUsageException.class, this::invalidDataAccessApiUsageExceptionHandler);
        exceptionRegister(ArithmeticException.class, this::arithmeticExceptionHandler);
        exceptionRegister(IndexOutOfBoundsException.class, this::indexOutOfBoundExceptionHandler);
        exceptionRegister(IllegalArgumentException.class, this::illegalArgumentExceptionHandler);
        exceptionRegister(SQLGrammarException.class, this::sqlGrammarExceptionHandler);
        exceptionRegister(GenericJDBCException.class, this::genericJDBCExceptionHandler);
        exceptionRegister(ConstraintViolationException.class, this::constraintViolationExceptionHandler);
        exceptionRegister(NonUniqueResultException.class, this::nonUniqueResultExceptionHandler);
        exceptionRegister(BadSqlGrammarException.class, this::badSqlGrammarExceptionHandler);
        exceptionRegister(InvalidDataAccessResourceUsageException.class, this::invalidDataAccessResourceUsageExceptionHandler);
        exceptionRegister(NullPointerException.class, this::nullPointerExceptionExceptionHandler);
        exceptionRegister(IllegalStateException.class, this::illegalStateExceptionHandler);
        exceptionRegister(NotWritablePropertyException.class, this::notWritablePropertyExceptionHandler);
        exceptionRegister(HttpMessageNotWritableException.class, this::httpMessageNotWritableExceptionHandler);

        exceptionRegister(JDBCConnectionException.class, this::jDBCConnectionExceptionHandler);
        exceptionRegister(ConnectException.class, this::connectExceptionHandler);
        exceptionRegister(UnknownHostException.class, this::unknownHostExceptionHandler);

        exceptionRegister(NoDataFoundException.class, this::noDataFoundedExceptionHandler);
        exceptionRegister(NoHandlerFoundException.class, this::noHandlerFoundExceptionHandler);
        exceptionRegister(EntityNotFoundedException.class, this::entityNotFoundedExceptionHandler);
        exceptionRegister(NoResourceFoundException.class, this::noResourceFoundExceptionHandler);

        exceptionRegister(MethodArgumentNotValidException.class, this::badRequestHandler);
        exceptionRegister(HttpMessageNotReadableException.class, this::badRequestHandler);
        exceptionRegister(BindException.class, this::badRequestHandler);
        exceptionRegister(org.springframework.validation.BindException.class, this::badRequestHandler);
        exceptionRegister(HttpClientErrorException.BadRequest.class, this::badRequestExceptionHandler);
        exceptionRegister(MissingServletRequestParameterException.class, this::missingParamExceptionHandler);
        exceptionRegister(MethodArgumentTypeMismatchException.class, this::methodArgumentTypeMismatchExceptionHandler);
        exceptionRegister(MissingPathVariableException.class, this::missingPathVariableExceptionHandler);
        exceptionRegister(TypeMismatchException.class, this::typeMismatchExceptionExceptionHandler);
        exceptionRegister(JsonParseException.class, this::jsonParseExceptionHandler);
        exceptionRegister(PropertyReferenceException.class, this::propertyReferenceExceptionHandler);

        exceptionRegister(HttpMediaTypeNotSupportedException.class, this::handleUnsupportedMediaExceptionHandler);

        exceptionRegister(DataIntegrityViolationException.class, this::dataIntegrityViolationExceptionHandler);

        exceptionRegister(HttpRequestMethodNotSupportedException.class, this::handleMethodNotAllowedExceptionHandler);

        exceptionRegister(AccessDeniedException.class, this::handleForbiddenExceptionHandler);
        exceptionRegister(SecurityException.class, this::handleForbiddenExceptionHandler);

        exceptionRegister(AuthenticationException.class, this::handleUnauthorizedExceptionHandler);
        exceptionRegister(BadCredentialsException.class, this::handleUnauthorizedExceptionHandler);
        exceptionRegister(HttpClientErrorException.Unauthorized.class, this::handleUnauthorizedExceptionHandler);
    }

    private <E extends Throwable> void exceptionRegister(Class<E> exceptionClass, BiFunction<? super E, HttpServletRequest,
            ResponseEntity<AppResponse>> handler) {
        exceptionHandlers.put(exceptionClass, (exception, httpServletRequest) ->
                handler.apply(exceptionClass.cast(exception), httpServletRequest)
        );
    }

    private Throwable getRootException(Throwable wrappedException) {
        Throwable actualException = wrappedException;
        while (actualException.getCause() != null && actualException.getCause() != actualException) {
            actualException = actualException.getCause();
        }
        return actualException;
    }

    private ResponseEntity<AppResponse> defaultError(Throwable exception, HttpServletRequest httpServletRequest) {
        String message = Optional.ofNullable(exception.getMessage()).orElse("");
        return buildResponse(message.toLowerCase().contains("not found") ?
                HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR, (Exception) exception, httpServletRequest);
    }

    private String getRootExceptionMessage(Throwable throwable, String message) {
        return Optional.ofNullable(throwable.getMessage()).orElse(Optional.ofNullable(message).isPresent() ? message : "Unexpected error occurred");
    }

    private String getRequestUrl(HttpServletRequest request) {
        return request.getRequestURL().toString();
    }

    private ResponseEntity<AppResponse> buildResponse(HttpStatus status, Exception exception, HttpServletRequest request) {
        String finalMessage = "";
        if (exception != null) {
            finalMessage = getRootExceptionMessage(exception, null);
        }
        return ResponseEntity.status(status)
                .body(new AppResponse(status.value(), status.getReasonPhrase(), getRequestUrl(request), null, LocalDateTime.now(), finalMessage));
    }

    private ResponseEntity<AppResponse> buildResponse(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(new AppResponse(status.value(), status.getReasonPhrase(), getRequestUrl(request), null, LocalDateTime.now(), message));

    }

    private ResponseEntity<AppResponse> buildResponse(HttpStatus status, String message, List<FieldErrorResponse> errorResponseList,
                                                      HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(new AppResponse(status.value(), status.getReasonPhrase(), getRequestUrl(request), errorResponseList, LocalDateTime.now(), message));
    }

    private ResponseEntity<AppResponse> buildResponse(HttpStatus status, String message, Exception exception, List<FieldErrorResponse> errorResponseList,
                                                      HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(new AppResponse(status.value(), status.getReasonPhrase(),
                        getRequestUrl(request), errorResponseList, LocalDateTime.now(),
                        getRootExceptionMessage(exception, message)));
    }

    private ResponseEntity<AppResponse> getRootExceptionOrDefaultException(Throwable rootException, HttpServletRequest httpServletRequest) {
        return exceptionHandlers.getOrDefault(rootException.getClass(), this::defaultError).apply(rootException, httpServletRequest);
    }

    @ExceptionHandler(value = RuntimeException.class)
    private ResponseEntity<AppResponse> runTimeExceptionHandler(RuntimeException exception, HttpServletRequest httpServletRequest) {
        Throwable rootException = getRootException(exception);
        return getRootExceptionOrDefaultException(rootException, httpServletRequest);
    }

    @ExceptionHandler(value = Throwable.class)
    private ResponseEntity<AppResponse> throwableExceptionHandler(Throwable exception, HttpServletRequest httpServletRequest) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, (Exception) exception, httpServletRequest);
    }

    /**Type Based Exception Handlers*/
    /**
     * Internal Server Exceptions
     */
    @ExceptionHandler(value = SQLException.class)
    private ResponseEntity<AppResponse> sqlExceptionHandler(SQLException exception, HttpServletRequest httpServletRequest) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Data Base Error : " + exception.getMessage(), httpServletRequest);
    }

    @ExceptionHandler(value = ArithmeticException.class)
    private ResponseEntity<AppResponse> arithmeticExceptionHandler(ArithmeticException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception, request);
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    private ResponseEntity<AppResponse> illegalArgumentExceptionHandler(IllegalArgumentException exception, HttpServletRequest httpServletRequest) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception, httpServletRequest);
    }

    @ExceptionHandler(value = InvalidDataAccessApiUsageException.class)
    private ResponseEntity<AppResponse> invalidDataAccessApiUsageExceptionHandler(InvalidDataAccessApiUsageException exception,
                                                                                  HttpServletRequest httpServletRequest) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception, httpServletRequest);
    }

    @ExceptionHandler(value = IndexOutOfBoundsException.class)
    private ResponseEntity<AppResponse> indexOutOfBoundExceptionHandler(IndexOutOfBoundsException exception, HttpServletRequest httpServletRequest) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception, httpServletRequest);
    }

    @ExceptionHandler(SQLGrammarException.class)
    public ResponseEntity<AppResponse> sqlGrammarExceptionHandler(SQLGrammarException exception, HttpServletRequest httpServletRequest) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception, httpServletRequest);
    }

    @ExceptionHandler(GenericJDBCException.class)
    public ResponseEntity<AppResponse> genericJDBCExceptionHandler(GenericJDBCException exception, HttpServletRequest httpServletRequest) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception, httpServletRequest);
    }

    @ExceptionHandler(JDBCConnectionException.class)
    public ResponseEntity<AppResponse> jDBCConnectionExceptionHandler(JDBCConnectionException exception, HttpServletRequest httpServletRequest) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, exception, httpServletRequest);
    }

    @ExceptionHandler(ConnectException.class)
    public ResponseEntity<AppResponse> connectExceptionHandler(ConnectException exception, HttpServletRequest httpServletRequest) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, exception, httpServletRequest);
    }

    @ExceptionHandler(UnknownHostException.class)
    public ResponseEntity<AppResponse> unknownHostExceptionHandler(UnknownHostException exception, HttpServletRequest httpServletRequest) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, exception, httpServletRequest);
    }

    @ExceptionHandler(NonUniqueResultException.class)
    public ResponseEntity<AppResponse> nonUniqueResultExceptionHandler(NonUniqueResultException exception, HttpServletRequest httpServletRequest) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception, httpServletRequest);
    }

    @ExceptionHandler(BadSqlGrammarException.class)
    public ResponseEntity<AppResponse> badSqlGrammarExceptionHandler(BadSqlGrammarException exception, HttpServletRequest httpServletRequest) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception, httpServletRequest);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<AppResponse> constraintViolationExceptionHandler(ConstraintViolationException exception, HttpServletRequest httpServletRequest) {
        return buildResponse(HttpStatus.CONFLICT, exception, httpServletRequest);
    }

    @ExceptionHandler(InvalidDataAccessResourceUsageException.class)
    public ResponseEntity<AppResponse> invalidDataAccessResourceUsageExceptionHandler(InvalidDataAccessResourceUsageException exception,
                                                                                      HttpServletRequest httpServletRequest) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception, httpServletRequest);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<AppResponse> nullPointerExceptionExceptionHandler(NullPointerException exception,
                                                                            HttpServletRequest httpServletRequest) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, new CustomNullPointerException(), httpServletRequest);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<AppResponse> illegalStateExceptionHandler(IllegalStateException exception,
                                                                    HttpServletRequest httpServletRequest) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, new CustomNullPointerException(), httpServletRequest);
    }

    /**
     * Bad Request Exceptions
     */
    @ExceptionHandler(value = HttpClientErrorException.BadRequest.class)
    private ResponseEntity<AppResponse> badRequestExceptionHandler(HttpClientErrorException.BadRequest exception, HttpServletRequest httpServletRequest) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception, httpServletRequest);
    }

    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    private ResponseEntity<AppResponse> missingParamExceptionHandler(MissingServletRequestParameterException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Missing required parameter: " + exception.getParameterName(), request);
    }

    @ExceptionHandler(value = JsonParseException.class)
    private ResponseEntity<AppResponse> jsonParseExceptionHandler(JsonParseException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception, request);
    }

    @ExceptionHandler(value = {PropertyReferenceException.class})
    private ResponseEntity<AppResponse> propertyReferenceExceptionHandler(PropertyReferenceException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception, request);
    }

    @ExceptionHandler(value = {NotWritablePropertyException.class})
    private ResponseEntity<AppResponse> notWritablePropertyExceptionHandler(NotWritablePropertyException exception, HttpServletRequest request) {
        String rootExceptionMessage = getRootExceptionMessage(exception, null);
        if (rootExceptionMessage.contains("Could not find field for property during fallback access")) {
            return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Projection DTO Field not matches with respective Entity fields, ".concat(rootExceptionMessage), request);
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception, request);
    }

    @ExceptionHandler(value = {HttpMessageNotWritableException.class})
    private ResponseEntity<AppResponse> httpMessageNotWritableExceptionHandler(HttpMessageNotWritableException exception, HttpServletRequest request) {
        String rootExceptionMessage = getRootExceptionMessage(exception, null);
        if (rootExceptionMessage.contains("Could not find field for property during fallback access")) {
            return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Projection DTO Field not matches with respective Entity fields, ".concat(rootExceptionMessage), request);
        }
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception, request);
    }

    @ExceptionHandler(value = {
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class,
            BindException.class,
            org.springframework.validation.BindException.class
    })
    public ResponseEntity<AppResponse> badRequestHandler(Exception exception, HttpServletRequest httpServletRequest) {
        String message = "Request payload or parameters Invalid ";
        if (exception instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException validationEx = (MethodArgumentNotValidException) exception;
            List<FieldErrorResponse> errorResponseList = validationEx.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(error -> new FieldErrorResponse(error.getField(), error.getRejectedValue(), error.getDefaultMessage()))
                    .collect(Collectors.toList());
            return buildResponse(HttpStatus.BAD_REQUEST, message, exception, errorResponseList, httpServletRequest);
        } else if (exception instanceof org.springframework.validation.BindException) {
            org.springframework.validation.BindException bindException = (org.springframework.validation.BindException) exception;
            List<FieldErrorResponse> errorResponseList = bindException.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(error -> new FieldErrorResponse(error.getField(), error.getRejectedValue(), error.getDefaultMessage()))
                    .collect(Collectors.toList());
            return buildResponse(HttpStatus.BAD_REQUEST, message, exception, errorResponseList, httpServletRequest);
        } else {
            message = exception.getMessage();
        }
        return buildResponse(HttpStatus.BAD_REQUEST, message, httpServletRequest);
    }

    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    private ResponseEntity<AppResponse> methodArgumentTypeMismatchExceptionHandler(MethodArgumentTypeMismatchException exception,
                                                                                   HttpServletRequest httpServletRequest) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception, httpServletRequest);
    }

    @ExceptionHandler(value = MissingPathVariableException.class)
    private ResponseEntity<AppResponse> missingPathVariableExceptionHandler(MissingPathVariableException exception,
                                                                            HttpServletRequest httpServletRequest) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception, httpServletRequest);
    }

    @ExceptionHandler(value = TypeMismatchException.class)
    private ResponseEntity<AppResponse> typeMismatchExceptionExceptionHandler(TypeMismatchException exception, HttpServletRequest httpServletRequest) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception, httpServletRequest);
    }

    /**
     * Not Found Exceptions
     */
    @ExceptionHandler(value = NoHandlerFoundException.class)
    private ResponseEntity<AppResponse> noHandlerFoundExceptionHandler(NoHandlerFoundException exception, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "No API endpoint found for " + exception.getRequestURL(), request);
    }

    @ExceptionHandler(value = NoDataFoundException.class)
    private ResponseEntity<AppResponse> noDataFoundedExceptionHandler(NoDataFoundException exception, HttpServletRequest httpServletRequest) {
        return buildResponse(HttpStatus.NOT_FOUND, exception, httpServletRequest);
    }

    @ExceptionHandler(value = NoResourceFoundException.class)
    private ResponseEntity<AppResponse> noResourceFoundExceptionHandler(NoResourceFoundException exception, HttpServletRequest httpServletRequest) {
        return buildResponse(HttpStatus.NOT_FOUND, exception, httpServletRequest);
    }

    @ExceptionHandler(value = EntityNotFoundedException.class)
    private ResponseEntity<AppResponse> entityNotFoundedExceptionHandler(EntityNotFoundedException exception, HttpServletRequest httpServletRequest) {
        return buildResponse(HttpStatus.NOT_FOUND, exception, httpServletRequest);
    }

    /**
     * Un support media type Exception
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<AppResponse> handleUnsupportedMediaExceptionHandler(HttpMediaTypeNotSupportedException exception,
                                                                              HttpServletRequest httpServletRequest) {
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, exception, httpServletRequest);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<AppResponse> dataIntegrityViolationExceptionHandler(DataIntegrityViolationException exception,
                                                                              HttpServletRequest httpServletRequest) {
        return buildResponse(HttpStatus.CONFLICT, exception, httpServletRequest);
    }

    /**
     * Method not allowed Exception
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<AppResponse> handleMethodNotAllowedExceptionHandler(HttpRequestMethodNotSupportedException exception,
                                                                              HttpServletRequest httpServletRequest) {
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, exception, httpServletRequest);
    }

    /**
     * Forbidden Exception
     */
    @ExceptionHandler({
            AccessDeniedException.class,
            SecurityException.class
    })
    public ResponseEntity<AppResponse> handleForbiddenExceptionHandler(Exception exception, HttpServletRequest httpServletRequest) {
        return buildResponse(HttpStatus.FORBIDDEN, "Access denied " + exception.getMessage(), httpServletRequest);
    }

    /**
     * Unauthorized Exception
     */
    @ExceptionHandler({
            AuthenticationException.class,
            BadCredentialsException.class,
            HttpClientErrorException.Unauthorized.class
    })
    public ResponseEntity<AppResponse> handleUnauthorizedExceptionHandler(Exception exception, HttpServletRequest httpServletRequest) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Authentication failed " + exception.getMessage(), httpServletRequest);
    }

}
