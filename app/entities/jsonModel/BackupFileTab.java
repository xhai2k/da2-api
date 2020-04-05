package entities.jsonModel;

import models.BackupFile;
import utils.CommonUtils;

import java.sql.Timestamp;

public class BackupFileTab {
    public Long id;
    public String fileName;
    public String fileLocation;
    public Long fileSize;
    public String fileSizeGb;
    public Timestamp uploadTime;
    public int userId;
    public String status;
    public BackupFileTab(BackupFile backupFile){
        this.id = backupFile.id;
        this.fileName = backupFile.fileName;
        this.fileLocation = backupFile.fileLocation;
        this.fileSize = backupFile.fileSize;
        this.fileSizeGb = CommonUtils.humanReadableByteCountBin(backupFile.fileSize);
        this.userId = backupFile.userId;
        this.status = backupFile.status.getTextJp();
        this.uploadTime = backupFile.uploadTime;

    }
}
