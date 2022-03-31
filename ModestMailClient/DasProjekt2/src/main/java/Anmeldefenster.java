import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.ArrayList;

public class Anmeldefenster {
    private final Scene scene;

    /**
     * Das ist eine GUI Komponente die von den Daten in MainHandler abhängt. Die gespeicherten User bekommen jeweils einen logIn Knopf. dazu kommt auch ein Register button
     * Der Konstruktor erzeugt die Scene die dann übergeben werden kann
     * @param users für jeden user wird ein button erstellt
     * @param m um auf die Daten zugreifen zu können
     * @throws IOException FXML Loader
     * @throws MessagingException nachricht lesen bei den Emails Szenen
     */
    public Anmeldefenster(ArrayList<Anmeldedaten> users, MainHandler m) throws IOException, MessagingException {
        FXMLLoader hintergrund = new FXMLLoader(getClass().getResource("/Anmeldefenster.fxml"));
        VBox vBox = hintergrund.load();
        FlowPane fp = new FlowPane();
        fp.setAlignment(Pos.CENTER);
        for (Anmeldedaten u : users) {
            FXMLLoader userloader = new FXMLLoader(getClass().getResource("/User.fxml"));
            VBox uservbox= userloader.load();
            User user = userloader.getController();
            user.setMainHandler(m);
            user.setAnmmeldeDaten(u);
            if(u.getPic()!=null){
                if(!u.getPic().isEmpty()){
                    user.setPic();
                }
            }
            user.username.setText(u.getUsername());
            fp.getChildren().add(uservbox);
        }
        FXMLLoader registerloader = new FXMLLoader(getClass().getResource("/RegisterButton.fxml"));
        HBox registerButton = registerloader.load();
        RegisterButton regButton = registerloader.getController();
        regButton.setMainHandler(m);
        fp.getChildren().add(registerButton);
        fp.setAlignment(Pos.CENTER);
        vBox.getChildren().add(fp);
        scene = new Scene(vBox);
    }

    /**
     * gibt die Szene zurück
     * @return Szene
     */
    public Scene getScene() {
        return this.scene;
    }

}
