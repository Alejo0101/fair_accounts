package backend.fair_accounts.archivo.storage;

public record StoredFile(String storedName, String sha256, long sizeBytes) {
}
