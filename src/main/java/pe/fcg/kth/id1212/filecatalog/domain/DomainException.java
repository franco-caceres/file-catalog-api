package pe.fcg.kth.id1212.filecatalog.domain;

public class DomainException extends RuntimeException {
    private ErrorCode code;

    public DomainException(ErrorCode code) {
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }
}
