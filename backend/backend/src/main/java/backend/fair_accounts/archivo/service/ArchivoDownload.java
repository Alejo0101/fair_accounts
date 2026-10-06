package backend.fair_accounts.archivo.service;

import org.springframework.core.io.Resource;

public record ArchivoDownload(String nombreOriginal, String tipoContenido, long tamanoBytes, Resource resource) {
}
