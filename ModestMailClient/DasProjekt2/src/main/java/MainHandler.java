import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.MessagingException;
import javax.mail.Store;
import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Diese Klasse funktioniert wie als Loader und linker.
 * Es handelt sic hier um die Main-Klasse und das Herz des Programms.
 * Hier werden die Nutzer - Daten geladen und gespeichert und von hier wird die fx Application kontrolliert.
 * Diese Klasse verbindet die restliche Teile des Programms mit einander
 */

public class MainHandler extends Application {

    /**
     * In der Klasse werden bereit erzeugte FX-Scenen gespeichert und nach anfrage aus anderen Komponenten geändert. Das macht das Programm flüßiger
     * Das IntegerProperty steuert den Stage und ändert die Scenen
     * Mit Hilfe von Anmeldedaten activeData wird der User-Objekt mit den restlichen Komponenten gebunden
     * Hier existiert auch eine Liste von allen bereits angelegten usern
     * Pfad dient als Ziel für das lesen und speichern der Dateien
     * Zwei Symetrische Cipher werden für das sichere speichern und lesen gebraucht
     * Das Passwort dient ebenfalls der Sicherheit
     */
    public ArrayList<Scene> scenes = new ArrayList<>();
    public IntegerProperty selected;
    private Anmeldedaten activeData;
    private ArrayList<Anmeldedaten> users;
    private String pfad;
    private Cipher cipher;
    private Cipher decrypt;
    public Media pop;
    public MediaPlayer popPlayer;
    private Stage primary;
    private Store activeStore;
    Checker checker;

    /**
     * Die main() Methode startet nur das Program
     * @param args args
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * Die Methode start() startet bekannterweise die Applikation. Sie nimmt einen Parameter "primaryStage" und das ist der vom JavaFX erzeugte Stage.
     * In dieser Methode wird dem Fenster der Name und das Icon übergeben.
     * Dazu wird die Datenstruktur users, ein Arraylist, erzeugt. Da werden die gespeicherten Daten geladen, bzw die neueDaten geadded.
     * Das IntegerProperty dient dem Scene-Wechsel.
     * Dazu wird auch das verhalten beim schließen des Programs bestimmt.
     * @param primaryStage von FX erzeugter Stage
     * @throws IOException bei datachecker() wird versucht Daten zu laden, das kann zu diesem Exception führen
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        String file = new File("").getAbsolutePath();
        System.out.println(file);
        primary = primaryStage;                                                                                         // Referenz auf PrimaryStage
        primaryStage.setTitle("Modest Mail Client");                                                                    //Titel des Programs
        Image icon = new Image(String.valueOf(getClass().getResource("/Bilder/icon.png")));                       // Icon des Programs
        primaryStage.getIcons().add(icon);
        users = new ArrayList<>();                                                                                      // users wird initialisiert
        datachecker();                                                                                                  //Laden der Daten, falls vorhanden
        selected = new SimpleIntegerProperty(this, "selected", 0);                                 //Initialisierung, IntegerProperty
        selected.addListener((observable, oldValue, newValue) ->{ loadScene(primaryStage); selected.set(100);});        //Listener auf IntegerProperty
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override                                                                                                   //Methode wird beim schließen des Programs aufgerufen
            public void handle(WindowEvent t) {
                if(pfad != null && cipher != null && decrypt != null){
                    saveData();                                                                                         // falls Daten geladen wurdenn so werden die 'neuen' gespeichert
                }
                try {
                    checker.interrupt();                                                                                //Thread im Hintergrund wird gestoppt, Fehlervereidung
                } catch (Exception e){
                    System.out.println("Program geschlossen");
                }
                try{
                    if(activeStore != null){
                        activeStore.close();                                                                            //Verbindung, falls offen, wird geschlossen
                    }
                } catch (MessagingException e) {
                    System.out.println("Store ist schon geschlossen");
                    Platform.exit();                                                                                    //Applikation wird geschloßen
                    System.exit(0);                                                                               //JVM wird geschloßen
                }
                Platform.exit();
                System.exit(0);
            }
        });
    }


    /**
     * Nachdem die Scenen gespeichert ist, wird zwischen denen durch diese Methode gewechselt.
     * Ist selected bei 100, bedeuted das kein Scene-Wechsel, sonst wird das entsprechende Scene geladen
     * @param primaryStage das zu wechselnde Stage
     */
    private void loadScene(Stage primaryStage) {                                                                        //Scene Wechsel mithilfe von selected INtegerPropeerty
        if(selected.intValue() == 100){                                                                                 // 100 für kein wechsel
            return;
        }
        primaryStage.setScene(scenes.get(selected.intValue()));
    }

    /**
     *
     * @throws IOException FXMLLoader standard Exception
     * @throws MessagingException Bei Register, wird gegebenfalls eine Verbindung zu dem Mail-Server hergestellt, das kann ein
     */
    public  void load1() throws IOException, MessagingException {                                                       //Die erste Scenen(vor dem Login) werden geladen
        Anmeldefenster anmelde = new Anmeldefenster(users, this);
        Scene anmelde_scene = anmelde.getScene();
        scenes.add(anmelde_scene); // szene 0
        RegisterScene register_scene = new RegisterScene(this);
        scenes.add(register_scene.register_scene); //Szene 1
        setSelected(0);
        primary.setMinHeight(600);                                                                                      //Minimale größe des Fensters wird festgelegt
        primary.setMinWidth(1000);
    }

    /**
     * Diese Methode ladet die restlichen Szenen(user abhängig) und speicher sie.
     * Dafür wird überprüft wie viele Folder auf dem Email - Server sind
     * Jeder Folder braucht ein Scene
     * Es wird ebenfalls eine Szene für das Verfassen von Mails gebraucht
     * @throws IOException Wird geworfen, wenn das Laden einer Datei fehlschlägt
     * @throws MessagingException Wird geworfen, wenn bei einer Scene eine Nachricht nicht gelesen werden kann.
     */
    public void load() throws IOException, MessagingException {
        pop = new Media(Objects.requireNonNull(getClass().getResource("/pop.mp3")).toString());                   //Der Sound bei PopUp für neue Mails wird vorbereitet
        popPlayer = new MediaPlayer(pop);                                                                               //Das Ziel ist so wenig wie möglich im letzten Moment zu laden "Storage-Performance Trade-Off"
        Verfassen_Scene verfassen_scene = new Verfassen_Scene(this);                                                // Die Szenen werden auch bereitgestellt
        scenes.add(verfassen_scene.scene);                                                                              //Szene 2
        scenes.add(new Scene(new HBox()));                                                                              // Szene 3 dummy für Email Lesen. Ein Dummy ist wichtig, damit der Szene-Wechsel mit loadscene() funktioiert
        int i = 0;
        for (String s : activeData.listFolders()){                                                                      //Alle Folder bekommen eine Szene
            scenes.add(new EmailsScene(this, activeData.getMail(i)).emailsScene);
            i++;
        }
        checker = new Checker(users,this);                                                                           //Der Thread Checker wird gestartet und ist für das Bekommen von neuen Mails verntwortlich.
        checker.start();
    }

    /**
     * Die Methode überprüft ob eins der gespeicherten usern eine neue Mail hat.
     * Wird von dem thread Checker gerufen und unterscheidet zwischen einem Pop-User und Imap-User
     * @throws MessagingException Wenn die neue Mails nicht geladen werden können
     * @throws IOException  Gegebenfalls muss eine FXML-Datei gelesen werden.
     */
    public void checknew() throws MessagingException, IOException {                                                     //Läuft für jeden User
        for (Anmeldedaten a : users) {
            if(a.getProtokoll().equals("imap")){
                a.checkForNew(this, a);                                                                              // Für IMAP-Usern
            }
            else {
                a.checkNewPop(this,a);                                                                               //Für Pop-Usern
            }
        }
    }

    /**
     * Die folgende Methode ist verantwortlich für das Erstellen der Ciphers für das Verschlüsseln und Entschlüsseln
     * @param readPass Das vom User gewählter Passwort
     * @throws NoSuchAlgorithmException Wenn der gewählte Algorithm nicht unterstützt wird
     * @throws NoSuchPaddingException   Wenn bei Padding ein Fehler passiert
     * @throws InvalidKeySpecException  Wenn die Schlüsseleigenschaften nicht passen ist
     * @throws InvalidKeyException      Wenn der Schlüssel selbst nicht passend ist
     */
    public void ciphers(String readPass) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeySpecException, InvalidKeyException {
        byte[] salt = new byte[8];                                                                                      //Salt wird auf 8 bytes gestellt
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");                                //Schlüsselerzeugung Algo
        KeySpec spec = new PBEKeySpec(readPass.toCharArray(), salt, 1000, 128);                    //Schlüsselspecs werden erzeugt
        SecretKey tmp = factory.generateSecret(spec);                                                                   //Schlüssel wird erzeugt
        SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
        cipher = Cipher.getInstance("AES");                                                                             //Verschlüsseln
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        decrypt = Cipher.getInstance("AES");                                                                            //Entschlüsseln
        decrypt.init(Cipher.DECRYPT_MODE, secretKey);
    }

    /**
     * Es kann während des Programsablauf nötig sein, bestimmte Scenes zu ändern, upzudaten oder reseten.
     * Diese Methode ist dafür vorgesehen. Der refresh passiert so schnell wie möglich, und nicht erst wenn gebraucht.
     * @param sceneNR Ident für die gespeicherten Szenen
     * @param sc      Scene falls nötig.
     * @throws IOException  Wie bei den restlichen Methoden für das Laden der Dateien
     * @throws MessagingException  Problemen beim Lesen der Nachrichten
     */
    public void refreshScene(int sceneNR, Scene sc, MainHandler m) throws IOException, MessagingException {
        Platform.runLater(new Runnable() {                                                                              // Platform.runlater ermögilicht, dass die Methode in Hintergrund arbeitet
            @Override
            public void run() {
                switch (sceneNR) {
                    case 0 :                                                                                         // Das Anmeldefenster
                        try {
                            scenes.set(0, new Anmeldefenster(users, m).getScene());
                            setSelected(0);
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        } catch (MessagingException e) {
                            e.printStackTrace();
                        }
                        break;

                    case 1 :                                                                                         // RegisterScene
                        scenes.remove(1);
                        scenes.add(1, sc);
                        break;

                    default :                                                                                       //Alle andere Scenen, die die Emails darstellen
                        int i = selected.get();
                        selected.set(100);
                        EmailsScene es = null;
                        try {
                            es = new EmailsScene(m, activeData.getMail(sceneNR - 4));
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        } catch (MessagingException e) {
                            e.printStackTrace();
                        }
                        assert es != null;                                                                              //Fehlervermeidung falls die erzeugte Scene nicht funktioniert
                        scenes.set(sceneNR, es.emailsScene);
                        selected.set(i);
                }
            }
        });
    }


    /**
     * Hier werden die gespeicherten Dateien geladen.
     */
    protected void loaddata(Stage primary) {
        try {
            FileInputStream fis = new FileInputStream(pfad  + "\\data.bin");                                      //Der InputStream nimmt die Datei aus dem gewählten Pfaden
            BufferedInputStream bis = new BufferedInputStream(fis);
            CipherInputStream cis = new CipherInputStream(bis, decrypt);                                                //Die Datei wird entschlüsselt
            ObjectInputStream ois = new ObjectInputStream(cis);
            users = (ArrayList<Anmeldedaten>) ois.readObject();                                                         //Falls die Entschlüsselung geklappt hat werden die Usern Daten gerufen
            fis.close();                                                                                                //Alle Streams werden geschloßen
            bis.close();
            cis.close();
            ois.close();
            load1();
            setSelected(100);
            setSelected(0);                                                                                             //User wird zu der Anmeldefenster weitergeleitet
        } catch (Exception e) {                                                                                         //Wenn die Datei gelöscht ist oder das Passwort falsch ist
            System.out.println("Falsches Passwort oder Folder");
            System.out.println(pfad  + "\\data.bin");
        }
    }

    /**
     * Überprüft ob der User bereits Daten gespeichert hat. Fragt danach nach Passwort oder ruft das Einstellungen Fenster auf
     * @throws IOException Beim Laden der Datei
     */
    protected void datachecker() throws IOException {
        try {
            Path path = pathfinder();                                                                                   //Es wird überprüft ob die Daten in dem Pfaden existieren
            pfad = Files.readString(path);
            FXMLLoader password = new FXMLLoader(getClass().getResource("/Password.fxml"));                       //Falls ja, dann wird nach Passwort gefragt
            primary.setScene(new Scene(password.load()));
            PasswordScene controller = password.getController();
            controller.setMain(this);
            primary.show();

        }
        catch (IOException exception){
            FXMLLoader settings = new FXMLLoader(getClass().getResource("/Settings.fxml"));                       // Falls die Datei nicht existiert wird das Setup fenster gerufen
            Scene setting = new Scene(settings.load());
            Settings setup = settings.getController();
            setup.setMain(this);
            primary.setScene(setting);
            primary.show();

        }
    }

    /**
     * Hier werden die Dateien in dem gegebenen Pfad gespeichert
     */
    public void saveData() {
        try {
            File pfad = new File(this.pfad);                                                                            //Zur Fehlervermeidung wird überprüft ob der Folder existiert, falls nicht wird er erzeugt
            if (!pfad.exists()) {
                pfad.mkdirs();
            }
            Path path = pathfinder();
            File file = path.toFile();
            if(!file.exists()){                                                                                         // Dann wird auch die Datei erzeugt, falls sie nicht bereits existiert
                System.out.println(file.getAbsolutePath());
                file.createNewFile();
            }
            Files.writeString(path, this.pfad, StandardOpenOption.CREATE);                                              // die Datei wird verschlüsselt geschrieben
            FileOutputStream fos = new FileOutputStream(pfad  + "\\data.bin");
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            CipherOutputStream cos = new CipherOutputStream(bos, cipher);
            ObjectOutputStream oos = new ObjectOutputStream(cos);
            oos.writeObject(users);
            oos.flush();                                                                                                //Streams werden geschloßen
            cos.flush();
            bos.flush();
            fos.flush();
            cos.close();
            fos.close();
            bos.close();
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Hier wird der gespeicherte Pfaden zu einem path umgeschrieben. Die Struktur des Daten-Folders wird geformt
     * @return gibt einen Pfaden zurück
     */
    private Path pathfinder() {
        URL folder = getClass().getProtectionDomain().getCodeSource().getLocation();                                    //Da der Pfaden außerhalb der Jar-Datei liegt
        String jarPath = URLDecoder.decode(folder.getFile(), StandardCharsets.UTF_8);
        String parentPath = new File(jarPath).getParentFile().getPath();
        Path folderpath = Paths.get(parentPath + "/Data");
        if(!folderpath.toFile().exists()){
            folderpath.toFile().mkdirs();
        }
        Path path = Paths.get(folderpath.toAbsolutePath() + "/settings.sgn");
        return path;
    }


    /**
     * Diese Methode bestimmt den activen User
     * @param a ein nutzer wird als aktiver Nutzer bestimmt
     */
    public void setActiveData(Anmeldedaten a) {
        activeData = a;
    }

    /**
     * Diese Methode gibt den activen User zurück
     * @return gibt den Aktiven user zurück
     */
    public Anmeldedaten getActiveUser() {
        return this.activeData;
    }

    /**
     * Ermöglicht das löschen der Daten eines Users
     * @param d der zu löschende User
     */
    public void removeUser(Anmeldedaten d) {
        users.remove(d);
    }

    public void registerUser(Anmeldedaten a) throws IOException {
        users.add(a);
    }

    public void setSelected(int i) {
        selected.set(i);
    }

    public String getPfad() {
        return pfad;
    }

    public void setPfad(String pfad){
        this.pfad = pfad;
    }

    public Stage getPrimary(){
        return primary;
    }

    public void setStore(Store store){
        activeStore = store;
    }

    public Store getStore(){
        return activeStore;
    }

    public void abmelden(){
        this.activeData = null;
    }

}
