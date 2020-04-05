package services;

import models.BackupFile;
import play.libs.F;

public interface IPostgresService {
    F.Promise<String> check(BackupFile backupFile);
}
