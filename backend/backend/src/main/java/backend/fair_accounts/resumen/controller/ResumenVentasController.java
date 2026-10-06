package backend.fair_accounts.resumen.controller;

import backend.fair_accounts.resumen.dto.ResumenVentasResponse;
import backend.fair_accounts.resumen.service.ResumenVentasService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/resumen/ventas")
@RequiredArgsConstructor
public class ResumenVentasController {

    private final ResumenVentasService resumenVentasService;

    @GetMapping
    public ResponseEntity<ResumenVentasResponse> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Integer limite) {
        return ResponseEntity.ok(resumenVentasService.getSummary(desde, hasta, limite));
    }
}
