package backend.fair_accounts.archivo.storage;

import backend.fair_accounts.archivo.config.FileStorageProperties;
import backend.fair_accounts.shared.exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LocalPrivateFileStorage {

    private final FileStorageProperties properties;

    public StoredFile store(MultipartFile file, InspectedImage image) {
        Path root = storageRoot();
        String storedName = UUID.randomUUID() + "." + image.extension();
        Path temporary = null;
        try {
            temporary = Files.createTempFile(root, "upload-", ".tmp");
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream source = new DigestInputStream(file.getInputStream(), digest);
                 OutputStream target = Files.newOutputStream(temporary, StandardOpenOption.TRUNCATE_EXISTING)) {
                source.transferTo(target);
            }

            Path destination = safePath(root, storedName);
            moveAtomically(temporary, destination);
            return new StoredFile(storedName, HexFormat.of().formatHex(digest.digest()), file.getSize());
        } catch (IOException | NoSuchAlgorithmException exception) {
            deletePath(temporary);
            throw new FileStorageException("No fue posible guardar la imagen.", exception);
        }
    }

    public Resource load(String storedName) {
        try {
            Path file = safePath(storageRoot(), storedName);
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new FileStorageException("El archivo asociado no está disponible.", null);
            }
            return resource;
        } catch (IOException exception) {
            throw new FileStorageException("No fue posible leer la imagen.", exception);
        }
    }

    public void deleteQuietly(String storedName) {
        try {
            Files.deleteIfExists(safePath(storageRoot(), storedName));
        } catch (IOException | FileStorageException ignored) {
            // Una tarea de limpieza puede recuperar archivos huérfanos posteriormente.
        }
    }

    private Path storageRoot() {
        try {
            Path root = Path.of(properties.storageLocation()).toAbsolutePath().normalize();
            Files.createDirectories(root);
            return root;
        } catch (IOException exception) {
            throw new FileStorageException("No fue posible preparar el almacenamiento privado.", exception);
        }
    }

    private Path safePath(Path root, String storedName) {
        Path resolved = root.resolve(storedName).normalize();
        if (!resolved.startsWith(root) || resolved.getFileName() == null || !resolved.getFileName().toString().equals(storedName)) {
            throw new FileStorageException("La referencia de archivo no es válida.", null);
        }
        return resolved;
    }

    private void moveAtomically(Path source, Path destination) throws IOException {
        try {
            Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(source, destination);
        }
    }

    private void deletePath(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // Se evita ocultar el error principal de la carga.
        }
    }
}
