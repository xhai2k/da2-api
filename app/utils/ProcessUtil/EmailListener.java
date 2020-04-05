package utils.ProcessUtil;

import java.util.EventListener;

public interface EmailListener extends EventListener {
    void emailFinished(EmailResult emailResult);
}