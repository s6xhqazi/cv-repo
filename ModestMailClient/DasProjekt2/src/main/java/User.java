import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

import javax.mail.MessagingException;
import java.io.IOException;

public class User {
    public Button edit;
    public Button delete;
    public Button logIn;
    public Label username;
    MainHandler m;
    Anmeldedaten d;

    public void setMainHandler(MainHandler m) {
        this.m = m;
    }

    public void setAnmmeldeDaten(Anmeldedaten d) {
        this.d = d;
    }

    public void edit(ActionEvent actionEvent) throws MessagingException, IOException {
        RegisterScene rs = new RegisterScene(m);
        rs.coustumeScene(d.getEmail(), d.getEingangserver(), d.getAusgangserver(), d.getEingangport(), d.getAusgangport(), d.getProtokoll());
        m.refreshScene(1, rs.register_scene,m);
        m.setSelected(1);
    }

    public void setPic() {
        String picLoc = "file:///" + m.getPfad().replace("\\", "/").replace(" ","") + "/" + d.getUuid() + "/" + "profile.png";
        logIn.setStyle(
                "-fx-background-image:  url(" + picLoc + ");" +
                        "-fx-border-radius: 50%;" +
                        "-fx-background-radius: 50%;" +
                        "-fx-background-size: stretch;" +
                        "-fx-background-repeat: no-repeat;"+
                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 10, 0.5, 3, 3);");
    }



    public void delete(ActionEvent actionEvent) throws IOException, MessagingException {
        m.removeUser(d);
        m.saveData();
        m.refreshScene(0, null,m);
        m.setSelected(100);
        m.setSelected(0);
    }

    public void getData(ActionEvent actionEvent) throws IOException, MessagingException {
        m.setActiveData(d);
        m.setStore(d.getMailer().connectStore());
        m.load();
        m.setSelected(2);
    }
}
