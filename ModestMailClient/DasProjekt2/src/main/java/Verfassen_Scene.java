import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.mail.MessagingException;
import java.io.IOException;

public class Verfassen_Scene {
    public Scene scene;
    FXMLLoader verfassen = new FXMLLoader(getClass().getResource("/NachrichtVerfassen.fxml"));


    public Verfassen_Scene(MainHandler m) throws IOException, MessagingException {
        //wenn man durch Sidebar kommt
        SidebarScene sbs = new SidebarScene(m);
        VBox vBox = sbs.getVbox();
        VBox vbox1 = verfassen.load();
        NachrichtVerfassen controller = verfassen.getController();
        controller.setMainHandler(m);
        HBox hBox = new HBox();
        hBox.getChildren().addAll(vBox,vbox1);
        scene = new Scene(hBox);
    }
    //reply implementieren
}
