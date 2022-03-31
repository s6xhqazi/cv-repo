import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class RegisterScene {
    Scene register_scene;
    FXMLLoader register_load = new FXMLLoader(getClass().getResource("/Register.fxml"));
    public RegisterScene(MainHandler m) throws IOException {
        VBox vBox = register_load.load();
        register_scene = new Scene(vBox);
        Register register = register_load.getController();
        register.setMainHandler(m);
    }

    public void coustumeScene(String email_adresse, String posteingangsserver, String postausgangsserver,
                              int posteingangsserverPort, int postausgangsserverPort, String protokoll){
        Register register =register_load.getController();
        register.email_TextField.setText(email_adresse);
        register.Menu.setValue(protokoll);
        register.posteingangsserver_TextField.setText(posteingangsserver);
        register.posteingangsserverPort_TextField.setText(String.valueOf(posteingangsserverPort));
        register.postausgangsserver_textField.setText(postausgangsserver);
        register.postausgangsserverPort_TextField.setText(String.valueOf(postausgangsserverPort));
    }
}
