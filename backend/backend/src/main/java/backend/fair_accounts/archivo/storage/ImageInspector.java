package backend.fair_accounts.archivo.storage;

import backend.fair_accounts.archivo.config.FileStorageProperties;
import backend.fair_accounts.shared.exception.InvalidFileException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ImageInspector {

    private static final Map<String, InspectedImage> ALLOWED_FORMATS = Map.of(
            "jpeg", new InspectedImage("image/jpeg", "jpg"),
            "png", new InspectedImage("image/png", "png")
    );

    private final FileStorageProperties properties;

    /** Valida la firma y dimensiones reales: no confía en la extensión ni en el MIME recibido. */
    public InspectedImage inspect(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Debe adjuntar una imagen no vacía.");
        }
        if (file.getSize() > properties.maxFileSizeBytes()) {
            throw new InvalidFileException("Cada imagen puede pesar como máximo 5 MB.");
        }

        try (ImageInputStream input = ImageIO.createImageInputStream(file.getInputStream())) {
            if (input == null) {
                throw new InvalidFileException("No fue posible leer el archivo enviado.");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new InvalidFileException("El archivo no es una imagen JPEG o PNG válida.");
            }

            ImageReader reader = readers.next();
            try {
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                InspectedImage image = ALLOWED_FORMATS.get(format);
                if (image == null) {
                    throw new InvalidFileException("Solo se permiten imágenes JPEG o PNG.");
                }

                reader.setInput(input, true, true);
                long pixels = Math.multiplyExact((long) reader.getWidth(0), reader.getHeight(0));
                if (pixels <= 0 || pixels > properties.maxPixels()) {
                    throw new InvalidFileException("Las dimensiones de la imagen exceden el límite permitido.");
                }
                return image;
            } finally {
                reader.dispose();
            }
        } catch (InvalidFileException exception) {
            throw exception;
        } catch (ArithmeticException | IOException exception) {
            throw new InvalidFileException("El archivo no contiene una imagen válida.");
        }
    }
}
