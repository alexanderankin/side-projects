package side.notes.backend.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {
    @SneakyThrows
    @ExceptionHandler
    public ProblemDetail exceptionHandler(Exception e) {
        log.error(e.getMessage(), e);
        throw e;
    }
}
