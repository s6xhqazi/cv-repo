import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ResourceBundle;

public class PasswordScene implements Initializable {
    public PasswordField pass;
    public Button login;
    private MainHandler m;

    public void setMain(MainHandler m){
        this.m = m;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        login.disableProperty().set(true);
        pass.textProperty().addListener(((observable, oldValue, newValue) -> {newValue = newValue.trim(); if (newValue.length()<6){
        login.disableProperty().setValue(true);} else { login.disableProperty().set(false);
        }
        }));
    }

    public void checkit(ActionEvent actionEvent) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {
        m.ciphers(pass.getText());
        m.loaddata(m.getPrimary());
    }
}
