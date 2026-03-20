package ru.gentleman.order.advice;

import lombok.RequiredArgsConstructor;
import org.axonframework.commandhandling.CommandExecutionException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.gentleman.common.exception.AlreadyExistsException;
import ru.gentleman.common.exception.NotFoundException;
import ru.gentleman.common.exception.ValidationException;

import java.util.Locale;

@RestControllerAdvice
@RequiredArgsConstructor
public class AdviceController {

    private final MessageSource messageSource;

    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleException(NotFoundException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, this.messageSource.getMessage(
                        exception.getMessageKey(),
                        exception.getArgs(),
                        Locale.getDefault()
                )
        );
        problemDetail.setTitle(
                this.messageSource.getMessage(
                        "error.title.not_found",
                        null,
                        Locale.getDefault()
                )
        );
        return problemDetail;
    }

    @ExceptionHandler(ValidationException.class)
    public ProblemDetail handleException(ValidationException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, exception.getMessage()
        );
        problemDetail.setTitle(
                this.messageSource.getMessage(
                        "error.title.validation",
                        null,
                        Locale.getDefault()
                )
        );
        return problemDetail;
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ProblemDetail handleException(AlreadyExistsException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND, this.messageSource.getMessage(
                        exception.getMessageKey(),
                        exception.getArgs(),
                        Locale.getDefault()
                )
        );
        problemDetail.setTitle(
                this.messageSource.getMessage(
                        "error.title.already_exist",
                        null,
                        Locale.getDefault()
                )
        );
        return problemDetail;
    }

    @ExceptionHandler(CommandExecutionException.class)
    public ProblemDetail handleCommandExecutionException(CommandExecutionException exception) {
        String message = exception.getMessage();
        String details = exception.getDetails().orElse("unknown").toString();
        ProblemDetail problemDetail = ProblemDetail.forStatus(
                HttpStatus.BAD_REQUEST
        );

        if(message.endsWith("not_found")) {
            problemDetail.setDetail(this.messageSource.getMessage(
                    message,
                    new Object[]{details},
                    Locale.getDefault())
            );
            problemDetail.setTitle(
                    this.messageSource.getMessage(
                            "error.title.not_found",
                            null,
                            Locale.getDefault()
                    )
            );
        } else{
            problemDetail.setDetail(this.messageSource.getMessage(
                    message,
                    new Object[]{details},
                    Locale.getDefault())
            );
            problemDetail.setTitle(
                    this.messageSource.getMessage(
                            "error.title.validation",
                            null,
                            Locale.getDefault()
                    )
            );
        }

        return problemDetail;
    }
}
