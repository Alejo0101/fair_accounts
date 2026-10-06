package backend.fair_accounts.resumen.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ResumenVentasResponse(
        LocalDate desde,
        LocalDate hasta,
        BigDecimal totalRecaudado,
        long numeroTransacciones,
        List<ProductoMasVendidoResponse> productosMasVendidos
) {
}
