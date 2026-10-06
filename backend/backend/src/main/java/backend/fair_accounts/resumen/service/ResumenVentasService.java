package backend.fair_accounts.resumen.service;

import backend.fair_accounts.resumen.dto.ResumenVentasResponse;
import backend.fair_accounts.resumen.repository.ResumenVentasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ResumenVentasService {

    private static final int DEFAULT_TOP_PRODUCTS = 5;
    private static final int MAX_TOP_PRODUCTS = 20;

    private final ResumenVentasRepository resumenVentasRepository;

    @Transactional(readOnly = true)
    public ResumenVentasResponse getSummary(LocalDate desde, LocalDate hasta, Integer limite) {
        LocalDate from = desde == null ? LocalDate.now() : desde;
        LocalDate to = hasta == null ? LocalDate.now() : hasta;
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("La fecha hasta no puede ser anterior a la fecha desde.");
        }
        int topLimit = limite == null ? DEFAULT_TOP_PRODUCTS : limite;
        if (topLimit < 1 || topLimit > MAX_TOP_PRODUCTS) {
            throw new IllegalArgumentException("El límite debe estar entre 1 y " + MAX_TOP_PRODUCTS + ".");
        }

        ResumenVentasRepository.SalesTotals totals = resumenVentasRepository.totals(from, to);
        return new ResumenVentasResponse(
                from, to, totals.totalRecaudado(), totals.numeroTransacciones(),
                resumenVentasRepository.topProducts(from, to, topLimit)
        );
    }
}
