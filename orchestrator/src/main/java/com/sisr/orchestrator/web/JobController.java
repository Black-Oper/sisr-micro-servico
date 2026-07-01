package com.sisr.orchestrator.web;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import com.sisr.orchestrator.domain.ScaleFactor;
import com.sisr.orchestrator.service.CreateJobResult;
import com.sisr.orchestrator.service.JobDetails;
import com.sisr.orchestrator.service.JobNotFoundException;
import com.sisr.orchestrator.service.JobNotReadyException;
import com.sisr.orchestrator.service.JobResult;
import com.sisr.orchestrator.service.JobService;

@RestController
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping("/api/v1/jobs")
    public ResponseEntity<CreateJobResult> create(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "scale", defaultValue = "2") int scale) {
        // Valida o scale (lança IllegalArgumentException -> 400, ver handler abaixo)
        ScaleFactor scaleFactor = ScaleFactor.of(scale);

        CreateJobResult result = jobService.createJob(file, scaleFactor);
        return ResponseEntity.accepted().body(result);
    }

    @GetMapping("/api/v1/jobs/{id}")
    public ResponseEntity<JobDetails> get(@PathVariable String id) {
        return jobService.findJob(id)
                .map(ResponseEntity::ok)                       // achou -> 200 + corpo
                .orElseGet(() -> ResponseEntity.notFound().build()); // vazio -> 404
    }

    @GetMapping("/api/v1/jobs/{id}/result")
    public ResponseEntity<byte[]> result(@PathVariable String id) {
        JobResult result = jobService.getResult(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(result.contentType()))
                .body(result.content());
    }

    /** scale fora de 2/3/4 -> 400 Bad Request. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", "Bad Request",
                "message", ex.getMessage()));
    }

    /** Requisição não-multipart (ou malformada) no upload -> 400 Bad Request. */
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Map<String, Object>> handleMultipart(MultipartException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", "Bad Request",
                "message", "requisição precisa ser multipart/form-data com o campo 'file'"));
    }

    /** Job inexistente -> 404 Not Found. */
    @ExceptionHandler(JobNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(JobNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "status", 404,
                "error", "Not Found",
                "message", ex.getMessage()));
    }

    /** Job ainda não concluído -> 409 Conflict. */
    @ExceptionHandler(JobNotReadyException.class)
    public ResponseEntity<Map<String, Object>> handleNotReady(JobNotReadyException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                "status", 409,
                "error", "Conflict",
                "message", ex.getMessage()));
    }
}
