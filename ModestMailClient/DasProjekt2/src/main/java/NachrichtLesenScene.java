import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.mail.MessagingException;
import javax.mail.Part;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NachrichtLesenScene {
    public Scene lesenScene;
    private final FXMLLoader readScene = new FXMLLoader(getClass().getResource("/NachrichtLesen.fxml"));
    private SidebarScene sbs;
    private final List<Part> parts = new ArrayList<>();

    public NachrichtLesenScene(MessageHalter m, MainHandler main) throws IOException, MessagingException {
        VBox vbox1 = readScene.load();
        SidebarScene sbs = new SidebarScene(main);
        VBox sidebar = sbs.getVbox();
        NachrichtLesen controller1 = readScene.getController();
        controller1.betreff_text.setText(m.getSubjekt());
        controller1.sender_text.setText(Arrays.toString(m.getFrom()));
        controller1.webView.getEngine().loadContent(m.getContent(), "text/html");
        controller1.setMain(main);
        controller1.setMessage(m);
        HBox hbox = new HBox();
        hbox.getChildren().addAll(sidebar, vbox1);
        this.lesenScene = new Scene(hbox);
    }
}
