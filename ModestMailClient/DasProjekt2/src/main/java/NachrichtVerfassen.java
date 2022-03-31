import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;

public class NachrichtVerfassen {
    public Label empfaenger_Label;
    public TextField empfaenger_TextField;
    public Label betreff_Label;
    public TextField betreff_TextField;
    public TextArea nachricht_TextArea;
    public Button senden_Button;
    public Button entwurf_speichern_Button;
    private MainHandler m;
    
    public void setMainHandler(MainHandler m){
        this.m = m;
    }


    public void parseMail(String folder) throws MessagingException {
        Message msg = new MimeMessage(m.getActiveUser().getMailer().getSession());
        Address a = new InternetAddress(m.getActiveUser().getSMTP().email_adresse);
        Address b = new InternetAddress(empfaenger_TextField.getText());
        msg.setContent(nachricht_TextArea.getText(), "text/plain");
        msg.setFrom(a);
        msg.setRecipient(Message.RecipientType.TO, b);
        msg.setSubject(betreff_TextField.getText());
        Folder mailFolder = m.getStore().getFolder(folder);
        Message[] messages = {msg};
        if(m.getActiveUser().listFolders().get(1).equals(folder)){
            msg.setFlag(Flags.Flag.DRAFT,true);
        }
        Thread thread = new Thread(()-> {
            try {
                mailFolder.appendMessages(messages);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        });
        thread.start();
        betreff_TextField.setText("");
        empfaenger_TextField.setText("");
        nachricht_TextArea.setText("");
    }

    public void mail_als_entwurf_speichern(ActionEvent actionEvent) throws MessagingException, IOException {
        if(m.getActiveUser().getProtokoll().equals("imap")){
            parseMail(m.getActiveUser().listFolders().get(1));
        }
        else {
            MessageHalter msg = mailBinder(this.m.getActiveUser().listFolders().get(1));
            this.m.getActiveUser().getMail(1).add(0,msg);
            this.m.refreshScene(5,null,m);
            betreff_TextField.setText("");
            empfaenger_TextField.setText("");
            nachricht_TextArea.setText("");
        }
    }

    private MessageHalter mailBinder(String folder) throws AddressException {
        Address a = new InternetAddress(m.getActiveUser().getSMTP().email_adresse);
        Address[] from = {a};
        Address b = new InternetAddress(empfaenger_TextField.getText());
        MessageHalter msg = new MessageHalter(from,a.toString(),betreff_TextField.getText(),b.toString(),null, new Date(), "text",nachricht_TextArea.getText(),nachricht_TextArea.getText(),null,folder);
        return msg;
    }

    public void email_senden(ActionEvent actionEvent) throws MessagingException {
        if(m.getActiveUser().getProtokoll().equals("imap")){
            parseMail(m.getActiveUser().listFolders().get(6));
        }
        else {
            MessageHalter msg = mailBinder(this.m.getActiveUser().listFolders().get(4));
            m.getActiveUser().getMail(6).add(0,msg);
            betreff_TextField.setText("");
            empfaenger_TextField.setText("");
            nachricht_TextArea.setText("");
        }

    }
}
