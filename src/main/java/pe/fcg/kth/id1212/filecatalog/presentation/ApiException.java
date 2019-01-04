package pe.fcg.kth.id1212.filecatalog.presentation;

import pe.fcg.kth.id1212.filecatalog.domain.ErrorCode;

public class ApiException extends RuntimeException {
    private ErrorCode code;

    public ApiException(ErrorCode code) {
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }
}
