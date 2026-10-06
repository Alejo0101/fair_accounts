package backend.fair_accounts.archivo.repository;

import backend.fair_accounts.archivo.entity.Archivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ArchivoRepository extends JpaRepository<Archivo, UUID> {
}
