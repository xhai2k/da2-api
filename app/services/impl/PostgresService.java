package services.impl;

import models.BackupFile;
import models.User;
import play.libs.F.Promise;
import repositories.UserRepository;
import repositories.impl.UserRepositoryImpl;
import services.IPostgresService;
import utils.ProcessUtil.DbChecking;

public class PostgresService extends Thread implements IPostgresService {
    private final UserRepository userRepository;

    public PostgresService() {
        userRepository = new UserRepositoryImpl();
    }

    @Override
    public Promise<String> check(BackupFile backupFile) {
        Promise<String> promise = new Promise<>();
        User user = userRepository.getBy(backupFile.userId);
        DbChecking dbChecking = new DbChecking(backupFile, user);
        dbChecking.addListener(res -> promise.invoke(res.getStatus().name()));
        dbChecking.start();
        return promise;
    }
}
