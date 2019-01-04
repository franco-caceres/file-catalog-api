package pe.fcg.kth.id1212.filecatalog.presentation.model;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

public class UploadForm {
    @Size(min = 5, max = 30, message = "The file name must have between 5 and 30 characters")
    private String name;
    @Min(value = 1, message = "The file must be at least 1 byte")
    @Max(value = 15000, message = "The file must be at most 15K bytes")
    private int size;
    private byte[] content;
    private boolean readOnly;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}
