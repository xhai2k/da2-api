package services.impl;

import models.BackupFile;
import models.User;
import org.apache.commons.mail.*;
import play.Logger;
import play.Play;
import play.libs.F;
import play.libs.Mail;
import repositories.UserRepository;
import repositories.impl.UserRepositoryImpl;
import services.IMailService;
import utils.CommonUtils;
import utils.Constants;
import utils.ProcessUtil.EmailExitDetector;

import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Singleton
public class MailService implements IMailService {

    public MailService(){
    }
@Override
public void sentMailToUser(User user, String title, String msg) throws EmailException{
        if(user == null) return;

        MultiPartEmail email = new MultiPartEmail();
        //SimpleEmail email = new SimpleEmail();
        String admin = Play.configuration.getProperty("mail.admin");
        email.setFrom(admin);
        email.addTo(user.getEmail());
        email.setSubject(title);
        email.setMsg(msg);
        Mail.send(email);
}
    /*@Override
    public void restoreError(BackupFile backupFile, User user, List<String> logs){
        try {
            if (backupFile == null) return;
            if (user == null) return;

            SimpleEmail email = new SimpleEmail();
            String admin = Play.configuration.getProperty("mail.admin");
            email.setFrom(admin);
            email.addTo(user.getEmail());
            email.setSubject("Restore Error");
            email.setMsg(String.format("%sの%s は正常にリストアすることができませんでした。\n%s", user.getName(), backupFile.fileName, String.join("\n", logs)));
            Mail.send(email);

        }
        catch (EmailException ex){
            ex.printStackTrace();
        }
    }*/

    @Override
    public F.Promise<String> restoreError(BackupFile backupFile, User user, List<String> logs){
        F.Promise<String> promise = new F.Promise<>();
        try {
            if (backupFile == null) return promise;
            if (user == null) return promise;

            String admin = Play.configuration.getProperty("mail.admin");
            String directory = Play.configuration.getProperty("directory.default");

            // create file log
            String content = String.join("\n", logs);
            Path path = Files.write(Paths.get(directory, CommonUtils.formatCurrnetDate(Constants.YYYYMMDDHHmmss)
                    + backupFile.fileName + ".log"), content.getBytes());
            File file = new File(path.toString());

            String title = "[SmileBackup+] 復元エラー";

            // send mail
            MultiPartEmail email = new MultiPartEmail();
            email.attach(file);
            email.setFrom(admin);
            email.addTo(user.getEmail());
            email.setSubject(title);
            email.setMsg(String.format("%sの%s は正常にリストアすることができませんでした。", user.getName(), backupFile.fileName));

            EmailExitDetector emailExitDetector = new EmailExitDetector(email);
            emailExitDetector.addProcessListener(emailResult -> promise.invoke(file.toString()));
            emailExitDetector.start();
        }
        catch (EmailException | IOException e){
            e.printStackTrace();
            Logger.error(e, e.getMessage());
            promise.invoke(null);
        }

        return promise;
    }

    /*@Override
    public void backupError(BackupFile backupFile, List<String> logs) throws EmailException {
        if (backupFile == null) return;

        User user = userRepository.getBy(backupFile.userId);
        if (user == null) return;

        SimpleEmail email = new SimpleEmail();
        String admin = Play.configuration.getProperty("mail.admin");
        email.setFrom(admin);
        email.addTo(user.getEmail());
        email.setSubject("Backup Error");
        email.setMsg(String.format("%sの%s は正常にバックアップされていません。", user.getName(), backupFile.fileName));
        Mail.send(email);
    }*/
}