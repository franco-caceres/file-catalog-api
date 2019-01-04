package pe.fcg.kth.id1212.filecatalog.entity;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Arrays;
import java.util.Objects;

@Entity
@Table(name = "file")
public class FileEntity {
    private Long id;
    private UserEntity userEntity;
    private String name;
    private long size;
    private byte[] content;
    private Boolean readOnly;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    public UserEntity getUserEntity() {
        return userEntity;
    }

    public void setUserEntity(UserEntity userEntity) {
        this.userEntity = userEntity;
    }

    @Basic
    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "size")
    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Basic
    @Column(name = "content", length = 100000)
    public byte[] getContent() {
        return content;
    }
    public void setContent(byte[] content) {
        this.content = content;
    }

    @Basic
    @Column(name = "readOnly", nullable = false)
    @Type(type = "org.hibernate.type.NumericBooleanType")
    public Boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(Boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileEntity fileEntity = (FileEntity) o;
        return Objects.equals(id, fileEntity.id) &&
                readOnly == fileEntity.readOnly &&
                Objects.equals(name, fileEntity.name) &&
                Objects.equals(size, fileEntity.size) &&
                Arrays.equals(content, fileEntity.content);
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(id, name, size, readOnly);
        result = 31 * result + Arrays.hashCode(content);
        return result;
    }
}