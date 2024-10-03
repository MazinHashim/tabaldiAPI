package com.tabaldi.api.exception;

import com.tabaldi.api.serviceImpl.PaymentServiceImpl;
import com.tabaldi.api.utils.MessagesUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
@RequiredArgsConstructor
public class ExceptionHandlerAdvice {

    private final MessageSource messageSource;
    final Logger logger = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

    @ExceptionHandler({ Exception.class })
    public @ResponseBody CustomErrorResponse handleExceptions(HttpServletResponse response, Exception exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(response);
        logger.error(exception.getMessage());
        exception.printStackTrace();
        String message = messageSource.getMessage("error.unexpected.error", null,
                LocaleContextHolder.getLocale());

        errorResponse.setMessage(message);

        return errorResponse;
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({ ClassCastException.class })
    public @ResponseBody CustomErrorResponse handleClassCastExceptions(HttpServletResponse response,
            ClassCastException exception) {
        System.out.println("Cast Exception " + (exception instanceof ClassCastException));
        String message = messageSource.getMessage("error.authentication.failed", null,
                LocaleContextHolder.getLocale());
        return getCustomErrorResponse(response, message, HttpServletResponse.SC_UNAUTHORIZED);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({ AccessDeniedException.class })
    public @ResponseBody CustomErrorResponse handleClassAccessDeniedExceptions(HttpServletResponse response,
            AccessDeniedException exception) {
        System.out.println("Access Denied Exception " + (exception instanceof AccessDeniedException));
        String message = messageSource.getMessage("error.authentication.failed", null,
                LocaleContextHolder.getLocale());
        return getCustomErrorResponse(response, message, HttpServletResponse.SC_FORBIDDEN);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({ NoResourceFoundException.class })
    public @ResponseBody CustomErrorResponse handleClassNoResourceFoundExceptions(HttpServletResponse response,
            NoResourceFoundException exception) {
        System.out.println("No Resource Found Exception " + (exception instanceof NoResourceFoundException));
        String message = messageSource.getMessage("error.no.resource.found", null,
                LocaleContextHolder.getLocale());
        return getCustomErrorResponse(response, message, HttpServletResponse.SC_NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({ ObjectOptimisticLockingFailureException.class })
    public @ResponseBody CustomErrorResponse handleObjectOptimisticLockingFailureExceptions(
            HttpServletResponse response,
            ObjectOptimisticLockingFailureException exception) {
        System.out
                .println("Object Optimistic Locking Failure Exception "
                        + (exception instanceof ObjectOptimisticLockingFailureException));
        String message = MessagesUtils.getAltreadyUpdatedByTransactionMessage(messageSource, "Information", "البيانات");
        return getCustomErrorResponse(response, message, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ MethodArgumentNotValidException.class })
    public @ResponseBody CustomErrorResponse handleValidationExceptions(HttpServletResponse response,
            MethodArgumentNotValidException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(response);
        System.out.println(
                "Method Argument Not Valid Exception " + (exception instanceof MethodArgumentNotValidException));
        String message = "";
        for (ObjectError error : exception.getBindingResult().getAllErrors()) {
            String fieldName = ((FieldError) error).getField();
            message += fieldName + " " + error.getDefaultMessage() + " : ";
        }
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        errorResponse.setCode(HttpServletResponse.SC_BAD_REQUEST);
        errorResponse.setMessage(message.substring(0, message.lastIndexOf(":")).trim());
        return errorResponse;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ BadCredentialsException.class })
    public @ResponseBody CustomErrorResponse handleBadCredentialsExceptions(HttpServletResponse response,
            BadCredentialsException exception) {
        System.out.println("Bad Credentials Exception " + (exception instanceof BadCredentialsException));
        String message = messageSource.getMessage("error.bad.credentials", null,
                LocaleContextHolder.getLocale());
        return getCustomErrorResponse(response, message, HttpServletResponse.SC_BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({ DataIntegrityViolationException.class })
    public @ResponseBody CustomErrorResponse handleDataIntegrityViolationExceptions(HttpServletResponse response,
            DataIntegrityViolationException exception) {
        System.out
                .println("Data Integrity Violation Exception " + (exception instanceof DataIntegrityViolationException)
                        + ": " + exception.getMessage());
        String message = messageSource.getMessage("error.some.data.lost", null, LocaleContextHolder.getLocale());
        return getCustomErrorResponse(response, message, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ HttpRequestMethodNotSupportedException.class })
    public @ResponseBody CustomErrorResponse handleHttpRequestMethodNotSupportedExceptions(HttpServletResponse response,
            HttpRequestMethodNotSupportedException exception) {
        System.out.println(
                "Data Integrity Violation Exception " + (exception instanceof HttpRequestMethodNotSupportedException)
                        + ": " + exception.getMessage());
        return getCustomErrorResponse(response, exception.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
    }

    @ExceptionHandler({ TabaldiGenericException.class })
    public @ResponseBody CustomErrorResponse handleTabaldiExceptions(HttpServletResponse response,
            TabaldiGenericException exception) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(exception);
        errorResponse.setMessage(exception.getMessage());
        response.setStatus(exception.getCode());

        return errorResponse;
    }

    private CustomErrorResponse getCustomErrorResponse(HttpServletResponse response,
            String message, int status) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(response);
        response.setStatus(status);
        errorResponse.setCode(status);
        errorResponse.setMessage(message);
        logger.error(errorResponse.getCode() + " : " + errorResponse.getMessage());
        return errorResponse;
    }
}
