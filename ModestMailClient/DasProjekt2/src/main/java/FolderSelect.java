import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;

import javax.mail.MessagingException;
import javax.mail.Store;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class FolderSelect {
    public ComboBox draft;
    public ComboBox deleted;
    public ComboBox gesendet;
    public ComboBox spam;
    public Button finish;
    public ComboBox archive;
    public ComboBox outbox;
    private MainHandler m;
    private Anmeldedaten a;
    private Store store;
    private final ArrayList<String> setupFolders = new ArrayList<>();

    public void setData(ArrayList<String> folders,Store store,MainHandler m,Anmeldedaten a){
        addFolders(draft, folders);
        addFolders(deleted, folders);
        addFolders(spam, folders);
        addFolders(gesendet, folders);
        addFolders(archive, folders);
        addFolders(outbox, folders);
        this.m = m;
        this.store = store;
        this.a = a;
    }
    private void addFolders(ComboBox b, ArrayList<String> folders){
        for (String s : folders){
            b.getItems().add(s);
        }
        b.getItems().remove("INBOX");
    }

    public void setUp(ActionEvent actionEvent) throws MessagingException, IOException {
        String draftS = (String) draft.getValue();
        String deletedS = (String) deleted.getValue();
        String spamS = (String) spam.getValue();
        String gesendetS = (String)  gesendet.getValue();
        String archiveS = (String) archive.getValue();
        String outboxS = (String) outbox.getValue();
        setupFolders.add("INBOX");
        System.out.println(archiveS);
        setupFolders.add(Objects.requireNonNullElse(draftS, "Draft"));
        setupFolders.add(Objects.requireNonNullElse(deletedS, "Geloescht"));
        setupFolders.add(Objects.requireNonNullElse(spamS, "Spam"));
        setupFolders.add(Objects.requireNonNullElse(gesendetS, "Gesendet"));
        setupFolders.add(Objects.requireNonNullElse(archiveS, "Archive"));
        setupFolders.add(Objects.requireNonNullElse(outboxS, "OUTBOX"));
        ArrayList<String> kopie = a.listFolders();
        kopie.removeAll(setupFolders);
        setupFolders.addAll(kopie);
        a.setFolders(setupFolders);
        a.getMailfirst(store);
        m.registerUser(a);
        m.refreshScene(0, null,m);
        m.setActiveData(a);
        m.saveData();
    }
}
