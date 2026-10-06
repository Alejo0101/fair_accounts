package backend.fair_accounts.resumen.repository;

import backend.fair_accounts.resumen.dto.ProductoMasVendidoResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Consulta de solo lectura sobre la tabla que creará el módulo venta.
 * Contrato requerido: venta(nombre_producto, cantidad, precio_unitario, fecha_venta).
 */
@Repository
public class ResumenVentasRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public SalesTotals totals(LocalDate from, LocalDate to) {
        Object[] row = (Object[]) entityManager.createNativeQuery("""
                SELECT COALESCE(SUM(cantidad * precio_unitario), 0), COUNT(*)
                FROM venta
                WHERE fecha_venta >= :from AND fecha_venta < :until
                """)
                .setParameter("from", Timestamp.valueOf(from.atStartOfDay()))
                .setParameter("until", Timestamp.valueOf(to.plusDays(1).atStartOfDay()))
                .getSingleResult();
        return new SalesTotals(toBigDecimal(row[0]), ((Number) row[1]).longValue());
    }

    @SuppressWarnings("unchecked")
    public List<ProductoMasVendidoResponse> topProducts(LocalDate from, LocalDate to, int limit) {
        List<Object[]> rows = entityManager.createNativeQuery("""
                SELECT nombre_producto, SUM(cantidad), COALESCE(SUM(cantidad * precio_unitario), 0)
                FROM venta
                WHERE fecha_venta >= :from AND fecha_venta < :until
                GROUP BY nombre_producto
                ORDER BY SUM(cantidad) DESC, nombre_producto ASC
                """)
                .setParameter("from", Timestamp.valueOf(from.atStartOfDay()))
                .setParameter("until", Timestamp.valueOf(to.plusDays(1).atStartOfDay()))
                .setMaxResults(limit)
                .getResultList();

        return rows.stream()
                .map(row -> new ProductoMasVendidoResponse(
                        (String) row[0], ((Number) row[1]).longValue(), toBigDecimal(row[2])))
                .toList();
    }

    private BigDecimal toBigDecimal(Object value) {
        return value instanceof BigDecimal decimal ? decimal : new BigDecimal(value.toString());
    }

    public record SalesTotals(BigDecimal totalRecaudado, long numeroTransacciones) {
    }
}
