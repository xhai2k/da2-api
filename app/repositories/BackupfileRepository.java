package repositories;

import entities.enums.FileStatus;
import models.BackupFile;

import java.sql.Date;
import java.util.List;

public interface BackupfileRepository {
    List<BackupFile> listFile(int userId, Date startDate, Date endDate, List<FileStatus> status, String searchName);
    List<BackupFile> listFileByUser(int userId);
    BackupFile create(BackupFile backupFile);

    BackupFile get(Long id);

    BackupFile updateStatus(Long id, FileStatus status);

    void delete(Long id);
}
