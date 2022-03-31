import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javax.crypto.NoSuchPaddingException;
import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ResourceBundle;

public class Settings implements Initializable {
    public Button folderButton;
    public Label folderLabel;
    public PasswordField passwort1;
    public PasswordField passwort2;
    public Button speichern;
    public Label meldung;
    public Label foldermeldung;
    private DirectoryChooser dc;
    private MainHandler m;
    private String directory ="";

    public void setMain(MainHandler m){
        this.m = m;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dc = new DirectoryChooser();
        foldermeldung.visibleProperty().set(false);
        speichern.disableProperty().setValue(true);
        meldung.visibleProperty().setValue(false);
        checker(passwort1, passwort2);
        checker(passwort2, passwort1);
    }

    private void checker(PasswordField passwort1, PasswordField passwort2) {
        passwort1.textProperty().addListener(((observable, oldValue, newValue) -> {
            speichern.disableProperty().setValue(newValue.length() < 6 || !newValue.equals(passwort2.getText()) || directory.isEmpty());
            meldung.visibleProperty().setValue(newValue.length() < 6 || !newValue.equals(passwort2.getText()));
        }));
    }

    public void chooseDir(ActionEvent actionEvent) {
        try {
            String folder = dc.showDialog(new Stage()).getAbsolutePath();
            this.directory =folder;
            folderLabel.setText(folder);
            speichern.disableProperty().set(passwort1.getText().length()<6 || !passwort1.getText().equals(passwort2.getText()));
            foldermeldung.visibleProperty().set(false);
        } catch (NullPointerException e){
            this.directory = "";
            folderLabel.setText("Folder");
            foldermeldung.visibleProperty().set(true);
            speichern.disableProperty().set(true);
        }

    }

    public void speichern(ActionEvent actionEvent) throws MessagingException, IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        m.ciphers(passwort1.getText());
        m.setPfad(this.directory + "\\MailClient");
        File pfad = new File(m.getPfad());
        if (!pfad.exists()) {
            pfad.mkdirs();
        }
        m.load1();
        m.setSelected(100);
        m.setSelected(0);
    }
}
