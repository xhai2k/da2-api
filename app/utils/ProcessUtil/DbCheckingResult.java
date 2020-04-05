package utils.ProcessUtil;

import entities.enums.FileStatus;

import java.util.List;

public class DbCheckingResult {
    private FileStatus status;
    private List<String> results;

    public DbCheckingResult(FileStatus status, List<String> results) {
        this.status = status;
        this.results = results;
    }

    public FileStatus getStatus() {
        return status;
    }

    public List<String> getResults() {
        return results;
    }
}
