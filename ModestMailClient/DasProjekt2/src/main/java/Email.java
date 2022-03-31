import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javax.mail.MessagingException;
import java.io.IOException;

public class Email {
    /**
     * Hierbei handelt es sich um einen klassischen FXML Controller. Wenn es eine neue Mail ist, dann erscheint der Text in bold sonst normal.
     */
    public Label absender_Label;
    public Label betreff_Label;
    public Label eingangsdatum_Label;
    private MainHandler m;
    private MessageHalter msg;

    public void setMessage(MessageHalter msg) {
        this.msg = msg;
        if(msg.neu){
            absender_Label.setStyle("-fx-font-weight: bold");
            betreff_Label.setStyle("-fx-font-weight: bold");
        }
    }

    public void setMainHandler(MainHandler m) {
        this.m = m;
    }

    public void clicked(MouseEvent mouseEvent) throws IOException, MessagingException {
        m.scenes.set(3, new NachrichtLesenScene(msg, m).lesenScene);
        m.setSelected(3);
        msg.neu = false;
        if(m.getActiveUser().getProtokoll().equals("imap")){
            m.getActiveUser().getMailer().messageGelesen(msg);
        }
        m.refreshScene(m.getActiveUser().listFolders().indexOf(msg.getFolder())+4, null ,m);
    }
}
