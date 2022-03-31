import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;

import javax.mail.*;
import java.io.IOException;

public class NachrichtLesen {
    public Label sender_Label;
    public Label sender_text;
    public Label betreff_Label;
    public Label betreff_text;
    public Button senden_Button;
    public Button nachricht_loeschen_Button;
    public WebView webView;
    private MessageHalter message;
    private MainHandler main;

    public void delete(ActionEvent actionEvent) throws MessagingException, IOException {
        int index = main.getActiveUser().listFolders().indexOf(message.getFolder());
        if(main.getActiveUser().getProtokoll().equals("imap")){
            main.setSelected(100);
            main.getActiveUser().deleteMail(message.getMessageID(),message.getFolder(), main.getActiveUser(), main);
        }
        else {
            if(message.getFolder().equals("INBOX")){
                Folder folder = main.getStore().getFolder("INBOX");
                folder.open(Folder.READ_WRITE);
                main.getActiveUser().deletePop(main,message,folder);
                folder.close();
            }
            main.getActiveUser().getMail(2).add(message);
            main.getActiveUser().getMail(index).remove(message);
        }
        main.refreshScene(6,null,main);
        main.refreshScene(4+index,null,main);
        main.setSelected(4+index);
    }

    public void setMessage(MessageHalter m){
        this.message = m;
    }

    public void setMain(MainHandler m){
        main = m;
    }

    public void archive(ActionEvent actionEvent) throws MessagingException, IOException {
        if(main.getActiveUser().getProtokoll().equals("imap")){
            Folder firstFolder = main.getStore().getFolder(message.getFolder());
            if(firstFolder.getName().equals(main.getActiveUser().listFolders().get(1))||firstFolder.getName().equals(main.getActiveUser().listFolders().get(2)) ){
                return;
            }
            firstFolder.open(Folder.READ_WRITE);
            UIDFolder uidFolder = (UIDFolder) firstFolder;
            Folder archive = main.getStore().getFolder(main.getActiveUser().listFolders().get(5));
            archive.open(Folder.READ_WRITE);
            Message msg = uidFolder.getMessageByUID(Long.parseLong(message.getMessageID()));
            Message[] msgs ={msg};
            archive.appendMessages(msgs);
            firstFolder.close();
            archive.close();
        }
        else {
            main.getActiveUser().getMail(5).add(message);
            main.refreshScene(9,null,main);
        }


    }

}
