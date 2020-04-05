package entities.enums;

import java.util.stream.Stream;

public enum FileStatus {
    UNCHECK(0,"未確認"), CHECKING(1, "確認中"), OK(2, "成功"), NG(3, "失敗");

    private int value;
    private String text;

    private FileStatus(int value, String text) {
        this.value = value;
this.text = text;
    }
    public int getFileStatus() {
        return value;
    }
    public String getTextJp() {
        return text;
    }
    public static FileStatus of(int value) {
        return Stream.of(FileStatus.values())
                .filter(p -> p.getFileStatus() == value)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public static FileStatus byName(String name) {
        return Stream.of(FileStatus.values())
                .filter(p -> p.name().equals(name))
                .findFirst().orElse(null);
    }
}
