import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.mail.MessagingException;
import javax.mail.Store;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class Register implements Initializable {
    public TextField email_TextField;
    public PasswordField password_TextField;
    public TextField posteingangsserverPort_TextField;
    public TextField posteingangsserver_TextField;
    public TextField postausgangsserverPort_TextField;
    public TextField postausgangsserver_textField;
    public Button register_Btn;
    public Button back_Btn;
    public String emailReg = "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
    public ComboBox Menu;
    public Label failure;
    public TextField username;
    public Label profilLabel;
    public Button profil;
    private MainHandler m;
    private FileChooser picChooser;
    private String picPath;

    public void setMainHandler(MainHandler m){
        this.m = m;
    }

    public void register(ActionEvent actionEvent) throws IOException, MessagingException {
        Anmeldedaten a = new Anmeldedaten(email_TextField.getText(),password_TextField.getText(),posteingangsserver_TextField.getText(),postausgangsserver_textField.getText(),Integer.parseInt(posteingangsserverPort_TextField.getText()),Integer.parseInt(postausgangsserverPort_TextField.getText()),(String) Menu.getValue(),username.getText());
        a.setPic(picPath,  m);
        Store store = a.getMailer().connectStore();
        if(store == null){
            failure.visibleProperty().setValue(true);
            return;
        }
        a.downFolders(store);
        FXMLLoader setup = new FXMLLoader(getClass().getResource("/FolderSelect.fxml"));
        VBox setupPane = setup.load();
        FolderSelect fs = setup.getController();
        fs.setData(a.listFolders(),store,m,a);
        Scene setupScene = new Scene(setupPane);
        m.getPrimary().setScene(setupScene);
        reset();
    }

    public void back(ActionEvent actionEvent) {
        m.setSelected(0);
        reset();
    }

    private void reset(){
        email_TextField.setText("");
        password_TextField.setText("");
        postausgangsserver_textField.setText("");
        postausgangsserver_textField.setText("");
        postausgangsserverPort_TextField.setText("");
        posteingangsserverPort_TextField.setText("");
        failure.visibleProperty().setValue(false);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Menu.getItems().addAll("pop3", "pop3s", "imap");
        Pattern p = Pattern.compile(emailReg, Pattern.CASE_INSENSITIVE);
        email_TextField.textProperty().addListener((observable, oldValue, newValue) -> register_Btn.setDisable(!p.matcher(newValue).matches() || password_TextField.getText().equals("")));
        password_TextField.textProperty().addListener((observable, oldValue, newValue) -> register_Btn.setDisable(!p.matcher(email_TextField.getText()).matches() || !email_TextField.getText().contains(".") || newValue.length()<5));

    }

    public void setpic(ActionEvent actionEvent) {
        this.picChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG Files (*.png)", "*.png");
        picChooser.getExtensionFilters().add(extFilter);
        File pic = picChooser.showOpenDialog(new Stage());
        if(pic != null){
            picPath = pic.getAbsolutePath();
            this.profilLabel.setText(picPath);
        }

    }
}
