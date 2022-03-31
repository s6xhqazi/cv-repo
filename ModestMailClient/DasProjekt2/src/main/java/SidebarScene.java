import com.jfoenix.controls.JFXButton;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.ArrayList;

public class SidebarScene {
    private final ArrayList<String> folders;
    private final ArrayList<JFXButton> buttons = new ArrayList<>();
    private final VBox sidebar;

    public SidebarScene(MainHandler m) throws MessagingException, IOException {

        this.folders = m.getActiveUser().listFolders();
        sidebar = new VBox();
        sidebar.setMinWidth(150.0);
        sidebar.setMinHeight(400.0);
        JFXButton vfs = new JFXButton("Verfassen");
        vfs.setOnAction(actionEvent -> {m.setSelected(2); vfs.autosize();});
        vfs.setButtonType(JFXButton.ButtonType.RAISED);
        vfs.setPrefSize(200.0,100.0);
        vfs.ripplerFillProperty().set(Paint.valueOf("#2596be"));
        sidebar.getChildren().add(vfs);
        for (int i=0; i< folders.size(); i++){
            JFXButton button = new JFXButton(folders.get(i));
            int finalI = i;
            button.setOnAction(actionEvent -> {m.setSelected(4 + finalI); button.autosize();});
            button.setButtonType(JFXButton.ButtonType.RAISED);
            button.setPrefSize(200.0,100.0);
            button.ripplerFillProperty().set(Paint.valueOf("#2596be"));
            sidebar.getChildren().add(button);
        }
        JFXButton button = new JFXButton("Abmelden");
        button.setOnAction(actionEvent -> {m.setSelected(0); button.autosize();
            try {
                m.getStore().close();
                m.abmelden();
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        });
        button.setPrefSize(200.0,100.0);
        button.ripplerFillProperty().set(Paint.valueOf("#2596be"));
        button.setButtonType(JFXButton.ButtonType.RAISED);
        sidebar.getChildren().add(button);
    }

    public VBox getVbox() throws MessagingException {
        return sidebar;
    }

}
