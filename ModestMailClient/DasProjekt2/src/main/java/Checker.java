import javax.mail.MessagingException;
import java.io.IOException;
import java.util.ArrayList;

public class Checker extends Thread {
    ArrayList<Anmeldedaten> user = new ArrayList<>();
    MainHandler m;

    /**
     * Laüft für alle nutzer ein nach dem anderen im Hintergrund
     * @param users Die zu überprüfenden Usern
     * @param m Zugriff auf Store
     */
    public Checker(ArrayList<Anmeldedaten> users, MainHandler m) {
        this.user = users;
        this.m = m;
    }

    /**
     * Der im Hintergrund laufende Thread überprüft ob es bei einem User neue Mails gibt
     * Bei einem Interrupt werden die Verbindungen geschloßen und der lauf unterbrochen
     */
    @Override
    public void run() {
        boolean running = true;
        while (running) {
            try {
                sleep(3000);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
            try {
                if(Thread.interrupted()){
                    running = false;
                    m.getStore().close();
                } else {
                    m.checknew();
                }
            } catch (MessagingException | IOException e) {
                e.printStackTrace();
            }
        }
    }
}
