import javafx.event.ActionEvent;
import javafx.scene.control.Button;

public class RegisterButton {
    public Button register;
    private MainHandler m;

    public void register(ActionEvent actionEvent){
        m.setSelected(1);
    }

    public void setMainHandler(MainHandler m){
        this.m = m;
    }
}
