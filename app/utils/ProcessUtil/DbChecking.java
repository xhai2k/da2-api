package utils.ProcessUtil;

import entities.enums.FileStatus;
import models.BackupFile;
import models.User;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import play.Logger;
import play.Play;
import play.libs.F;
import services.IMailService;
import services.impl.MailService;
import utils.CommonUtils;
import utils.Constants;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DbChecking extends Thread {
    private BackupFile backupFile;
    private User user;
    private List<DbCheckingListener> listeners = new ArrayList<>();
    private final IMailService mailService;

    public DbChecking(BackupFile backupFile, User user) {
        this.backupFile = backupFile;
        this.user = user;
        this.mailService = new MailService();
    }

    public void run() {
        String dbName = null;
        FileStatus fileStatus = FileStatus.NG;
        List<String> resultOutput = new ArrayList<>();
        String dirPath;
        try {
            dbName = createDb();
            String ext = FilenameUtils.getExtension(backupFile.fileLocation);
            if (!ext.equals("zip")) {
                resultOutput.addAll(restoreDb(backupFile.fileLocation, dbName));
            } else {
                // unzip files
                dirPath = CommonUtils.unzipfile(backupFile.fileLocation);
                if (dirPath == null) {
                    // invokes the listeners
                    for (DbCheckingListener listener : listeners) {
                        listener.finished(new DbCheckingResult(FileStatus.NG, resultOutput));
                    }
                    resultOutput.add("バックアップファイルが存在しません。");
                    F.Promise<String> m = mailService.restoreError(backupFile, user, resultOutput);
                    return;
                }

                // get restore files
                Stream<Path> walk = Files.walk(Paths.get(dirPath));
                List<String> files = walk.filter(Files::isRegularFile).map(Path::toString).collect(Collectors.toList());

                // restore DB schema
                String fileDbSchema = Play.configuration.getProperty("fileDbRestoreSchema");
                Path fileSchemaPath = Paths.get(dirPath, fileDbSchema);
                String fileSchema = fileSchemaPath.toString();
                Optional<String> fileFilterOtp = files.stream().filter(x -> x.contains(fileSchema)).findFirst();
                if (fileFilterOtp.isPresent()) {
                    resultOutput.addAll(restoreDb(fileFilterOtp.get(), dbName));
                }

                // restore DB data
                for (String file : files) {
                    if (file.equals(fileSchema)) continue;
                    resultOutput.addAll(restoreDb(file, dbName));
                }

                // delete files
                File dir = new File(dirPath);
                FileUtils.deleteDirectory(dir);
            }

            if (resultOutput.isEmpty()) {
                fileStatus = FileStatus.OK;
            } else {
                fileStatus = FileStatus.NG;
                F.Promise<String> m = mailService.restoreError(backupFile, user, resultOutput);
                while (!m.isDone()) {
                    Thread.sleep(1000);
                }
                String mailResult = m.get();
                if (mailResult != null) {
                    FileUtils.forceDelete(new File(m.get()));
                }
            }
        } catch (InterruptedException | IOException | ExecutionException e) {
            e.printStackTrace();
            Logger.error(e, e.getMessage());
        } finally {
            dropDb(dbName);
            // invokes the listeners
            for (DbCheckingListener listener : listeners) {
                listener.finished(new DbCheckingResult(fileStatus, resultOutput));
            }
        }
    }

    public void addListener(DbCheckingListener listener) {
        listeners.add(listener);
    }

    public void removeListener(DbCheckingListener listener) {
        listeners.remove(listener);
    }

    private String createDb() {
        try {
            Process p;
            ProcessBuilder pb;
            String pgcreate = System.getProperty("user.dir") + "\\postgre\\createdb.exe";
            String host = Play.configuration.getProperty("test.backup.host");
            String port = Play.configuration.getProperty("test.backup.port");
            String username = Play.configuration.getProperty("test.backup.username");
            String encoding = Play.configuration.getProperty("test.backup.encoding");
            String password = Play.configuration.getProperty("test.backup.password");
            String dbName = Play.configuration.getProperty("test.backup.dbname") + "_" + CommonUtils.formatCurrnetDate(Constants.YYYYMMDDHHmmss);

            pb = new ProcessBuilder(
                    pgcreate,
                    "--host=" + host,
                    "--port=" + port,
                    "--username=" + username,
                    "--encoding=" + encoding,
                    dbName);
            pb.redirectErrorStream(true);
            Map<String, String> env = pb.environment();
            env.put("PGPASSWORD", password);
            p = pb.start();
            p.waitFor();
            System.out.println("Create DB has finished.");
            if (p.exitValue() != 0) {
                return null;
            } else {
                return dbName;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Logger.error(e, e.getMessage());
            return null;
        }
    }

    private List<String> restoreDb(String filePath, String dbName) {
        try {
            Process p;
            ProcessBuilder pb;
            String psql = System.getProperty("user.dir") + "\\postgre\\psql.exe";
            String host = Play.configuration.getProperty("test.backup.host");
            String port = Play.configuration.getProperty("test.backup.port");
            String username = Play.configuration.getProperty("test.backup.username");
            String password = Play.configuration.getProperty("test.backup.password");
            if (dbName == null) {
                dbName = Play.configuration.getProperty("test.backup.dbname");
            }
            pb = new ProcessBuilder(
                    psql,
                    //"-v",
                    //"ON_ERROR_STOP=ON",
                    "--host=" + host,
                    "--port=" + port,
                    "--username=" + username,
                    "--dbname=" + dbName,
                    "--file=" + filePath);
            pb.redirectErrorStream(true);
            Map<String, String> env = pb.environment();
            env.put("PGPASSWORD", password);

            p = pb.start();
            InputStream is = p.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String ll;
            List<String> logs = new ArrayList<>();
            while ((ll = br.readLine()) != null) {
                System.out.println(ll);
                logs.add(ll);
            }

            System.out.println("Restore DB has finished.");
            logs = logs.stream()
                    .filter(x -> !x.contains("ERROR:  language \"plpgsql\" already exists") && x.contains("ERROR:"))
                    .collect(Collectors.toList());

            if (p.exitValue() == 0 && logs.isEmpty()) {
                return new ArrayList<>();
            } else {
                return logs;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Logger.error(e, e.getMessage());
            return new ArrayList<>();
        }
    }

    private void dropDb(String dbName) {
        if (dbName == null) return;

        try {
            Process p;
            ProcessBuilder pb;
            String pgdropdb = System.getProperty("user.dir") + "\\postgre\\dropdb.exe";
            String host = Play.configuration.getProperty("test.backup.host");
            String port = Play.configuration.getProperty("test.backup.port");
            String username = Play.configuration.getProperty("test.backup.username");
            String password = Play.configuration.getProperty("test.backup.password");

            pb = new ProcessBuilder(
                    pgdropdb,
                    "--host=" + host,
                    "--port=" + port,
                    "--username=" + username,
                    dbName);
            pb.redirectErrorStream(true);
            Map<String, String> env = pb.environment();
            env.put("PGPASSWORD", password);
            p = pb.start();
            p.waitFor();
            System.out.println("Drop DB has finished.");
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            Logger.error(e, e.getMessage());
        }
    }
}
