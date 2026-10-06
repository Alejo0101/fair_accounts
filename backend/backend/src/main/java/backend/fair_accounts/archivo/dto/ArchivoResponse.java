package backend.fair_accounts.archivo.dto;

import java.time.Instant;
import java.util.UUID;

public record ArchivoResponse(
        UUID id,
        Long ventaId,
        String nombreOriginal,
        String tipoContenido,
        long tamanoBytes,
        Instant creadoEn,
        String urlConsulta
) {
}
