package backend.fair_accounts.resumen.dto;

import java.math.BigDecimal;

public record ProductoMasVendidoResponse(
        String nombreProducto,
        long cantidadVendida,
        BigDecimal totalRecaudado
) {
}
