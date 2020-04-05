package utils.ProcessUtil;

import org.apache.commons.mail.MultiPartEmail;
import play.Logger;
import play.libs.Mail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class EmailExitDetector extends Thread  {

    /** The Email for which we have to detect the end. */
    private  MultiPartEmail email;
    /** The associated listeners to be invoked at the end of the email. */
    private List<EmailListener> listeners = new ArrayList<>();

    /**
     * Starts the detection for the given process
     * @param email the process for which we have to detect when it is finished
     */
    public EmailExitDetector(MultiPartEmail email) {
        this.email = email;
    }

    public void run() {
        try {
            Future<Boolean> future = Mail.send(email);
            while(!future.isDone()){
                Thread.sleep(500);
            }
            // invokes the listeners
            for (EmailListener listener : listeners) {
                listener.emailFinished(new EmailResult());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Logger.error(e, e.getMessage());
        }
    }

    /** Adds a process listener.
     * @param listener the listener to be added
     */
    public void addProcessListener(EmailListener listener) {
        listeners.add(listener);
    }

    /** Removes a process listener.
     * @param listener the listener to be removed
     */
    public void removeProcessListener(EmailListener listener) {
        listeners.remove(listener);
    }
}
