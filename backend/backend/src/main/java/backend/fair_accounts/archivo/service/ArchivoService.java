package backend.fair_accounts.archivo.service;

import backend.fair_accounts.archivo.dto.ArchivoResponse;
import backend.fair_accounts.archivo.entity.Archivo;
import backend.fair_accounts.archivo.repository.ArchivoRepository;
import backend.fair_accounts.archivo.storage.ImageInspector;
import backend.fair_accounts.archivo.storage.InspectedImage;
import backend.fair_accounts.archivo.storage.LocalPrivateFileStorage;
import backend.fair_accounts.archivo.storage.StoredFile;
import backend.fair_accounts.shared.exception.InvalidFileException;
import backend.fair_accounts.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArchivoService {

    private final ArchivoRepository archivoRepository;
    private final ImageInspector imageInspector;
    private final LocalPrivateFileStorage privateStorage;

    @Transactional
    public List<ArchivoResponse> upload(Long ventaId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new InvalidFileException("Debe adjuntar al menos una imagen.");
        }
        if (files.size() > 5) {
            throw new InvalidFileException("Se permiten como máximo cinco imágenes por solicitud.");
        }

        List<StoredFile> storedFiles = new ArrayList<>();
        try {
            List<ArchivoResponse> response = new ArrayList<>();
            for (MultipartFile file : files) {
                InspectedImage image = imageInspector.inspect(file);
                StoredFile stored = privateStorage.store(file, image);
                storedFiles.add(stored);

                Archivo archivo = new Archivo(
                        ventaId,
                        safeOriginalName(file.getOriginalFilename()),
                        stored.storedName(),
                        image.contentType(),
                        stored.sizeBytes(),
                        stored.sha256()
                );
                response.add(toResponse(archivoRepository.saveAndFlush(archivo)));
            }
            return response;
        } catch (RuntimeException exception) {
            storedFiles.forEach(file -> privateStorage.deleteQuietly(file.storedName()));
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public ArchivoDownload download(UUID id) {
        Archivo archivo = archivoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe un archivo con id " + id));
        return new ArchivoDownload(
                archivo.getNombreOriginal(),
                archivo.getTipoContenido(),
                archivo.getTamanoBytes(),
                privateStorage.load(archivo.getNombreAlmacenado())
        );
    }

    private ArchivoResponse toResponse(Archivo archivo) {
        return new ArchivoResponse(
                archivo.getId(), archivo.getVentaId(), archivo.getNombreOriginal(),
                archivo.getTipoContenido(), archivo.getTamanoBytes(), archivo.getCreadoEn(),
                "/api/v1/archivos/" + archivo.getId()
        );
    }

    private String safeOriginalName(String submittedName) {
        if (submittedName == null || submittedName.isBlank()) {
            return "imagen";
        }
        String filename = submittedName.replace('\\', '/');
        filename = filename.substring(filename.lastIndexOf('/') + 1)
                .replaceAll("[\\p{Cntrl}]", "_")
                .trim();
        if (filename.isBlank()) {
            return "imagen";
        }
        return filename.substring(0, Math.min(filename.length(), 255));
    }
}
