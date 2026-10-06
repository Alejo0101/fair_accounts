package backend.fair_accounts.archivo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "archivo")
public class Archivo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Referencia lógica a venta.id; la FK se integra cuando llegue el módulo de ventas. */
    @Column(name = "venta_id")
    private Long ventaId;

    @Column(name = "nombre_original", nullable = false, length = 255)
    private String nombreOriginal;

    @Column(name = "nombre_almacenado", nullable = false, unique = true, length = 100)
    private String nombreAlmacenado;

    @Column(name = "tipo_contenido", nullable = false, length = 50)
    private String tipoContenido;

    @Column(name = "tamano_bytes", nullable = false)
    private long tamanoBytes;

    @Column(name = "sha256", nullable = false, length = 64)
    private String sha256;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    protected Archivo() {
    }

    public Archivo(Long ventaId, String nombreOriginal, String nombreAlmacenado,
                   String tipoContenido, long tamanoBytes, String sha256) {
        this.ventaId = ventaId;
        this.nombreOriginal = nombreOriginal;
        this.nombreAlmacenado = nombreAlmacenado;
        this.tipoContenido = tipoContenido;
        this.tamanoBytes = tamanoBytes;
        this.sha256 = sha256;
        this.creadoEn = Instant.now();
    }

    public UUID getId() { return id; }
    public Long getVentaId() { return ventaId; }
    public String getNombreOriginal() { return nombreOriginal; }
    public String getNombreAlmacenado() { return nombreAlmacenado; }
    public String getTipoContenido() { return tipoContenido; }
    public long getTamanoBytes() { return tamanoBytes; }
    public String getSha256() { return sha256; }
    public Instant getCreadoEn() { return creadoEn; }
}
