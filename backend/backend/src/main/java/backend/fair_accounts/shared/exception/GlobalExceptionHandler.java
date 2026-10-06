package backend.fair_accounts.shared.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(problem(HttpStatus.NOT_FOUND, exception.getMessage()));
    }

    @ExceptionHandler({InvalidFileException.class, IllegalArgumentException.class})
    public ResponseEntity<ProblemDetail> handleBadRequest(RuntimeException exception) {
        return ResponseEntity.badRequest()
                .body(problem(HttpStatus.BAD_REQUEST, exception.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ProblemDetail> handleFileTooLarge() {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(problem(HttpStatus.PAYLOAD_TOO_LARGE, "El archivo excede el tamaño permitido."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        ProblemDetail detail = problem(HttpStatus.BAD_REQUEST, "La solicitud contiene datos inválidos.");
        detail.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(detail);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ProblemDetail> handleStorage(FileStorageException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(problem(HttpStatus.INTERNAL_SERVER_ERROR, "No fue posible procesar el archivo."));
    }

    private ProblemDetail problem(HttpStatus status, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setType(URI.create("https://fair-accounts.dev/problems/" + status.value()));
        return problem;
    }
}
