import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class EmailsScene {
    public Scene emailsScene;

    public EmailsScene(MainHandler m, ArrayList<MessageHalter> messages) throws IOException, MessagingException {
        HBox hbox = new HBox();
        SidebarScene sbs = new SidebarScene(m);
        VBox vbox = sbs.getVbox();
        hbox.getChildren().add(vbox);
        VBox emails = new VBox();
        FXMLLoader legende = new FXMLLoader(getClass().getResource("/Legende.fxml"));
        HBox legendeBox = legende.load();
        emails.getChildren().add(legendeBox);
        for (int i = 0; i < messages.size() && i < 10; i++) {
            FXMLLoader fxmlLoader2 = new FXMLLoader(getClass().getResource("/Email.fxml"));
            HBox button = fxmlLoader2.load();
            Email controller = fxmlLoader2.getController();
            controller.eingangsdatum_Label.setText(messages.get(i).getSentDate().toString());
            controller.betreff_Label.setText(messages.get(i).getSubjekt());
            controller.absender_Label.setText(Arrays.toString(messages.get(i).getFrom()));
            controller.setMainHandler(m);
            controller.setMessage(messages.get(i));
            emails.getChildren().add(button);
        }
        hbox.getChildren().add(emails);
        this.emailsScene = new Scene(hbox);
    }
}
