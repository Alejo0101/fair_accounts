package backend.fair_accounts.archivo.controller;

import backend.fair_accounts.archivo.dto.ArchivoResponse;
import backend.fair_accounts.archivo.service.ArchivoDownload;
import backend.fair_accounts.archivo.service.ArchivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/archivos")
@RequiredArgsConstructor
public class ArchivoController {

    private final ArchivoService archivoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ArchivoResponse>> upload(
            @RequestParam(value = "ventaId", required = false) Long ventaId,
            @RequestParam("files") List<MultipartFile> files) {
        return ResponseEntity.status(HttpStatus.CREATED).body(archivoService.upload(ventaId, files));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> download(@PathVariable UUID id) {
        ArchivoDownload file = archivoService.download(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.tipoContenido()))
                .contentLength(file.tamanoBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(file.nombreOriginal(), StandardCharsets.UTF_8)
                        .build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(file.resource());
    }
}
