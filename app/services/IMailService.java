package services;

import models.BackupFile;
import models.User;
import org.apache.commons.mail.EmailException;
import play.libs.F;

import java.util.List;

public interface IMailService {

    F.Promise<String> restoreError(BackupFile backupFile, User user, List<String> logs);
    //void backupError(BackupFile backupFile, List<String> logs) throws EmailException;
    void sentMailToUser(User user, String title, String msg) throws EmailException;
}