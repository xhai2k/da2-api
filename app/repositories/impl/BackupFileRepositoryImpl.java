package repositories.impl;

import entities.enums.FileStatus;
import models.BackupFile;
import play.db.jpa.JPA;
import repositories.BackupfileRepository;

import javax.persistence.Query;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

public class BackupFileRepositoryImpl implements BackupfileRepository {
    @Override
    public List<BackupFile> listFileByUser(int userId){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT b FROM BackupFile b WHERE b.userId=:userId ORDER BY b.id DESC");
        Query query  = JPA.em().createQuery(stringBuilder.toString(), BackupFile.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }
    public List<BackupFile> listFile(int userId, Date startDate, Date endDate, List<FileStatus> status, String searchName){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT b FROM BackupFile b WHERE b.userId=:userId ");
        if(startDate != null){
            stringBuilder.append(" AND b.uploadTime >= :startDate");
        }
        if(endDate != null){
            stringBuilder.append(" AND b.uploadTime <= :endDate");
        }
        if (status != null){
            stringBuilder.append(" AND b.status IN :status");
        }
        if(searchName!= null && !searchName.equals("")){
           stringBuilder.append(" AND b.fileName like :name");
        }
        stringBuilder.append(" ORDER BY b.id DESC");
        Query query  = JPA.em().createQuery(stringBuilder.toString(), BackupFile.class);
        query.setParameter("userId", userId);
        Timestamp startDateP = null;
        if(startDate != null){
             startDateP = new Timestamp(startDate.getTime());
            query.setParameter("startDate", startDateP);
        }
        Timestamp endDateP = null;
        if(endDate != null){
            endDateP = new Timestamp(endDate.getTime() + 86400000);
            query.setParameter("endDate", endDateP);
        }
        if (status != null){
           query.setParameter("status",status);
        }
        if(searchName!= null && !searchName.equals("")){
           searchName = "%"+ searchName +"%";
           query.setParameter("name", searchName);
        }
        return query.getResultList();
    }
    @Override
    public BackupFile create(BackupFile backupFile){
        return backupFile.save();
    }

    @Override
    public BackupFile get(Long id) {
        BackupFile backupFile = new BackupFile();
        return backupFile.find("byId", id).first();
    }

    @Override
    public BackupFile updateStatus(Long id, FileStatus status) {
        BackupFile backupFile = get(id);
        if (backupFile != null){
            backupFile.status = status;
            backupFile.save();
            return backupFile;
        }
        return null;
    }

    @Override
    public void delete(Long id) {
        BackupFile backupFile = get(id);
        backupFile.delete();
    }
}
