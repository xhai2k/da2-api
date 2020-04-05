package controllers;

import entities.enums.ErrorCode;
import entities.enums.FileStatus;
import entities.jsonModel.BackupFileTab;
import models.BackupFile;
import models.User;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.http.HttpStatus;
import play.Logger;
import play.Play;
import play.i18n.Messages;
import play.libs.F;
import repositories.BackupfileRepository;
import repositories.UserRepository;
import repositories.impl.BackupFileRepositoryImpl;
import repositories.impl.UserRepositoryImpl;
import services.IMailService;
import services.IPostgresService;
import services.impl.MailService;
import services.impl.PostgresService;
import utils.CommonUtils;
import utils.Constants;
import utils.DateUtlis;
import utils.TokenUtlis;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class BackupFileController extends BaseController {
    private final BackupfileRepository backupfileRepository;
    private final IMailService mailService;
    private final IPostgresService postgresService;
    private final UserRepository userRepository;

    public BackupFileController() {
        backupfileRepository = new BackupFileRepositoryImpl();
        mailService = new MailService();
        postgresService = new PostgresService();
        userRepository = new UserRepositoryImpl();
    }
public void sentMailIfBackupFail() throws EmailException{
    Logger.info("CALL API SENT MAIL IF BACKUP FAIL");
    Logger.info("API create user called");
    Map<String, String> bodyRequest = null;
    try {
        bodyRequest = mapper.readValue(request.body, Map.class);
    } catch (Exception e) {
        Logger.error(e, "Body is null");
        responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.PARAMS_MISSING, Messages.get("PARAMS_MISSING"));
    }
    String title = Objects.requireNonNull(bodyRequest).get("title");
    String mgs = Objects.requireNonNull(bodyRequest).get("mgs");
    String token = TokenUtlis.getTokenFromRequest(request);
    if (!StringUtils.isNotEmpty(token)) {
        responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.PARAMS_MISSING, Messages.get("PARAMS_MISSING"));
    }
    User userSelected = new User();
    if (token != null) {
        User user = TokenUtlis.decode(token);
        userSelected = User.findById(user.getId());
    }
    if(userSelected == null){
        responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.USER_NOT_FOUND, Messages.get("USER_NOT_FOUND"));
    }
    mailService.sentMailToUser(userSelected, title, mgs);

}
    public void getListFileByUser(int userId, String startDate, String endDate, String status, String searchName) {
        Logger.info("CALL API LIST FILE");
        String token = TokenUtlis.getTokenFromRequest(request);
        List<FileStatus> statusLst = null;
        if(status != null && !status.equals(""))
            statusLst = Arrays.stream(status.split(";")).map(value-> FileStatus.of(Integer.parseInt(value))).collect(Collectors.toList());
        java.sql.Date startDateParam = null;
        try {
            if (startDate != null) {
                startDateParam = DateUtlis.convertString2DateSql(startDate);
            }
        } catch (ParseException e) {
            Logger.error(e, e.getMessage());
        }
        java.sql.Date endDateParam = null;
        try {
            if (endDate != null) {
                endDateParam = DateUtlis.convertString2DateSql(endDate);
            }
        } catch (ParseException e) {
            Logger.error(e, e.getMessage());
        }
        try {
            List<BackupFileTab> backupFileTabs = new ArrayList<BackupFileTab>();
            List<BackupFile> backupFiles = backupfileRepository.listFile(userId, startDateParam, endDateParam, statusLst, searchName);
for(BackupFile backupFile : backupFiles){
    backupFileTabs.add(new BackupFileTab(backupFile));
}
            String json = mapper.valueToTree(backupFileTabs).toString();
            renderJSON(json);
        } catch (Exception e) {
            Logger.error(e, e.getMessage());
        }
    }

    public void upload(File backup, String name) {
        try {
            String admin = Play.configuration.getProperty("admin.username");
            if (userLogin.getUid().equals(admin)){
                responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.UPLOAD_PERMISSION, Messages.get("UPLOAD_PERMISSION"));
                return;
            }

            if (backup == null) {
                responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.PARAMS_MISSING, Messages.get("PARAMS_MISSING"));
                return;
            }

            final long size = Files.size(backup.toPath());
            final long time = System.currentTimeMillis();
            final String timeFormat = CommonUtils.formatDate(new Date(time), Constants.YYYYMMDDHHmmssSSS);

            // check max size
            List<BackupFile> backupFiles = backupfileRepository.listFileByUser(userLogin.id);
            Long sumSize = backupFiles.stream().mapToLong(x -> x.fileSize).sum();
            Long storage = userLogin.getStorage();

            if (sumSize + size > storage) {
                String title = "[SmileBackup+] ファイルアップロードの警告";
                String mgs = "格納可能の容量制限を超えてしまいます。不要なファイルを削除するか、管理者に問い合わせ下さい。";
                User user = User.findById(userLogin.id);
                mailService.sentMailToUser(user,title, mgs);
                responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.OVER_STORAGE, Messages.get("OVER_STORAGE"));
                return;
            }

            // get file name
            final String fileName;
            File fileUpload = new File(backup.getName());
            if (name == null || name.isEmpty()) {
                fileName = fileUpload.getName();
            } else {
                fileName = name;
            }

            // check directory
            final String directory = Play.configuration.getProperty("directory.default");
            File theDir = new File(directory, userLogin.uid);
            if (!theDir.exists()) {
                theDir.mkdir();
            }

            // save file
            String ext = FilenameUtils.getExtension(fileName);
            if (!ext.isEmpty()){
                ext = "." + ext;
            }
            Path destPath = Paths.get(theDir.toString(), timeFormat + ext);
            Files.copy(backup.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
            Files.deleteIfExists(backup.toPath());
            final BackupFile data = new BackupFile(fileName, destPath.toString(), size, new Timestamp(time), FileStatus.UNCHECK, userLogin.id);

            backupfileRepository.create(data);
            if(sumSize + size > (0.9*storage)){
                String title = "[SmileBackup+] 容量制限";
                String mgs = "格納可能の容量制限の90％を超える恐れがあります。不要なファイルを削除するか、管理者に問い合わせ下さい。";
                User user = User.findById(userLogin.id);
                mailService.sentMailToUser(user,title, mgs);
            }
            String json = mapper.valueToTree(data).toString();
            renderJSON(json);
        }catch (IOException | EmailException e){
            Logger.error(e, e.getMessage());
        }
    }

    public void check(Long id) {
        BackupFile backupFile = backupfileRepository.get(id);
        if (backupFile == null) {
            renderJSON(null);
            return;
        }

        backupfileRepository.updateStatus(id, FileStatus.CHECKING);
        try {
            F.Promise<String> check = postgresService.check(backupFile);
            await(check);
            String status = check.get();

            BackupFile data = backupfileRepository.updateStatus(id, FileStatus.valueOf(status));
            renderJSON(data);
        } catch (InterruptedException | ExecutionException e) {
            Logger.error(e, e.getMessage());
            e.printStackTrace();
        }
    }

    public void download(Long id) {
        BackupFile backupFile = backupfileRepository.get(id);
        File file = new File(backupFile.fileLocation);
        if (file.exists()){
            renderBinary(file, backupFile.fileName);
        }else{
            responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.FILE_NOT_FOUND, "ダウンロードファイルが存在しません。");
        }
    }

    public void delete(Long id) {
        BackupFile backupFile = backupfileRepository.get(id);
        File file = new File(backupFile.fileLocation);
        if (file.exists()) {
            if (!file.delete()) {
                responseError(HttpStatus.SC_BAD_REQUEST, ErrorCode.DELETE_ERROR, Messages.get("DELETE_ERROR"));
            }
        }

        backupfileRepository.delete(id);
        String json = mapper.valueToTree(backupFile).toString();
        renderJSON(json);
    }
}
