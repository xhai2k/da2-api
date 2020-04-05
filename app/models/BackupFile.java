package models;

import entities.enums.FileStatus;
import play.db.jpa.GenericModel;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Data returned from the database
 */
@Entity
@Table(name = "backup_file")
public class BackupFile extends GenericModel {

    public BackupFile() {
    }

    public BackupFile(String fileName, String fileLocation, Long fileSize, Timestamp uploadTime, FileStatus status, int userId) {
        this.fileName = fileName;
        this.fileLocation = fileLocation;
        this.fileSize = fileSize;
        this.uploadTime = uploadTime;
        this.status = status;
        this.userId = userId;
    }

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @Column(name = "file_name")
    public String fileName;

    @Column(name = "file_location")
    public String fileLocation;

    @Column(name = "file_size")
    public Long fileSize;

    @Column(name = "upload_time")
    public Timestamp uploadTime;

    @Enumerated
    @Column(name = "status", columnDefinition = "smallint")
    public FileStatus status;

    @Column(name="user_id")
    public int userId;
}
