import javafx.scene.control.Label;

public class GUI_PopUp {
    public Label absender_Label;
    public Label betreff_Label;
    public Label user;

    public void setAbsender_Label(String absender_Label) {
        this.absender_Label.setText(absender_Label);
    }

    public void setBetreff_Label(String betreff_Label) {
        this.betreff_Label.setText(betreff_Label);
    }

    public void setUser(String username){
        user.setText(username);
    }
}