import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import javax.mail.*;
import javax.mail.search.SearchTerm;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Anmeldedaten implements Serializable {
    private final SMTP smpt;
    private final EMailGetter mailGetter;
    private final String protokoll;
    private String pic = "";
    private UUID uuid;
    private final ArrayList<ArrayList<MessageHalter>> messageHalter = new ArrayList<>();
    private ArrayList<String> folders = new ArrayList<>(); // 0 Inbox, 1 Entwürfe, 2 gelöscht, 3 SPAM, 4 GESENDET , 5 archive, 6 outbox, ab 7 rest
    public ArrayList<Integer> messageCount = new ArrayList<>();
    private final ArrayList<Date> lastmail = new ArrayList<>();
    private SearchTerm searchTerm;
    private final String email;
    private final String eingangserver;
    private final int eingangport;
    private final String ausgangserver;
    private final int ausgangport;
    private final String username;

    /**
     * Der Konstruktor nimmt die Dateien aus den Angaben des Users und speichert sie in einem neuen Objekt die Namen sind selbsterklärend
     * @param email_adresse Emailadresse des Users
     * @param passwort      Das passwort
     * @param posteingangsserver  Der Imap/Pop server
     * @param postausgangsserver  Der SMTP Server
     * @param posteingangsserverPort Der Imap/Pop Port
     * @param postausgangsserverPort Der Smtp Port
     * @param protokoll Imap oder Pop
     * @param username Optional, Username für den gespeicherten User
     */
    Anmeldedaten(String email_adresse, String passwort, String posteingangsserver, String postausgangsserver,
                 int posteingangsserverPort, int postausgangsserverPort, String protokoll, String username) {
        this.protokoll = protokoll;
        this.username = username;
        this.email = email_adresse;
        this.eingangport = posteingangsserverPort;
        this.eingangserver = posteingangsserver;
        this.ausgangport = postausgangsserverPort;
        this.ausgangserver = postausgangsserver;
        smpt = new SMTP(email_adresse, passwort, postausgangsserver, postausgangsserverPort);
        mailGetter = new EMailGetter(protokoll, posteingangsserver, posteingangsserverPort, email_adresse, passwort);
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }

    /**
     * Hier werden die Foldernamen gespeichert als Parameter des Objektes
     * @throws MessagingException bei einem Fehler in der Verbidung mit dem Server
     */
    public void downFolders(Store store) throws MessagingException {
        Folder[] flds = store.getDefaultFolder().list();                                                                //Eine Liste von allen Folders wird erzeugt
        for (Folder fld : flds) {                                                                                       //Die Foldeers und die Anzahl der Mails werden gespeichert
            byte[] s = fld.getName().getBytes(StandardCharsets.UTF_16);
            String name = new String(s, StandardCharsets.UTF_16);
            folders.add(name);
            fld.open(Folder.READ_ONLY);
            messageCount.add(fld.getMessageCount());
            lastmail.add(new Date());                                                                                   //Gespeichert wird auch wann das letzte Mal nach neuen Mails überprüft wurde, hier werden Dummies erstellt
        }
    }

    /**
     * Hier werden die Emails nach dem Registrieren runtergeladen. Diese Methode wird nur kurz nach dem Registrieren gerufen.
     * Unterscheidung zwischen IMap und Pop
     * @throws MessagingException Fehler beim Herunterladen
     */
    public void getMailfirst(Store store) throws MessagingException, IOException {
        if(protokoll.equals("imap")){
            for (String s : folders){
                ArrayList<MessageHalter> msgs = mailGetter.downloadEmails(s,store);
                messageHalter.add(msgs);
            }
        } else{
            ArrayList<MessageHalter> msgs = mailGetter.downloadInbox(store);
            messageHalter.add(msgs);
            for(int i = 0; i<6; i++){
                messageHalter.add(new ArrayList<>());
            }
        }
    }

    /**
     * es kann der Fall eintretten, dass die Folders sich ändern, dafür ist dieser setter vorgesehen
     * @param folders die zu speichernden Foldernamen
     */
    public void setFolders(ArrayList<String> folders){
        this.folders = folders;
    }

    /**
     * Pop hat nur einen Folder daher muss es auch nur diesen überprüfen, für die eingehende Mails. Dazu wird überprüft ob etwas in dem Outbox liegt.
     * Wenn ja dann wird das von handleOutbox(...) gehandelt
     * @param m Zugriff auf refreshScene(...)
     * @param a Anmeldedaten für Fehlervermeidung, da die methode auf einem Thread läuft
     * @throws MessagingException Fehler beim Herunterladen der Mails
     * @throws IOException Bei dem PopUp aufruf
     */
    public void checkNewPop(MainHandler m,Anmeldedaten a) throws MessagingException, IOException {
        handleOutbox(m, a);                                                                                             //Outbox Handler
        Store store;                                                                                                    //Die Verbindung
        if(a == m.getActiveUser()){
            store = m.getStore();                                                                                       //Der aktiver user hat Priorität daher ist seine Verbindung immer offen
        }
        else {
            store = a.getMailer().connectStore();                                                                       //Andere Usern müssen erst verbinden
        }
        searchTerm = new SearchTerm() {
            @Override
            public boolean match(Message msg) {                                                                             //es wird nach enuen Mails gesucht mit dem term
                try {
                    return msg.getSentDate().after(a.lastmail.get(0));
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
                return false;
            }
        };
        if(store==null){
            return;
        }
        Folder inbox = store.getFolder("INBOX");                                                                  //Folder Inbox wird geöffnet und es wird nach neuen Mails gesucht
        inbox.open(Folder.READ_ONLY);
        Message[] newMSg = inbox.search(searchTerm);
        ArrayList<MessageHalter> newStorable = getMailer().readMessage(newMSg,null,"INBOX",true);
        if(newMSg.length>0){                                                                                            //Wenn es neue Mails gibt, dann werden diese gespeichert und ein PopUp erscheint
            messageHalter.get(0).addAll(0,newStorable);
            a.lastmail.set(0, new Date());                                                                              //Das Datum des letzten Mail wird geupdatet
            onMailReceived(newMSg[0],m.popPlayer,0, m.getPrimary());
        }
        while(messageHalter.get(0).size()>10){
            messageHalter.get(0).remove(messageHalter.get(0).size()-1);
        }
        int lastonline = inbox.getMessageCount()-9;
        if(lastonline<1){
            lastonline =1;
        }
        if(messageHalter.get(0).get(messageHalter.get(0).size()-1).getSentDate().after(inbox.getMessage(lastonline).getSentDate()) || (inbox.getMessageCount()!=messageHalter.get(0).size() && inbox.getMessageCount()!=10 && messageHalter.get(0).size()!=10)){
            messageHalter.set(0,mailGetter.downloadInbox(store));
        }
        m.refreshScene(4,null,m);
        inbox.close();                                                                                                  //Szene wird geupdatet und folder geschloßen
    }

    /**
     *
     * @param m MainHandler erlaubt zugriff auf refreshScene(...)
     * @param a Fehlervermeidung
     * @throws MessagingException Schicken des Emails fehlgeschlagen
     * @throws IOException refreshScene(...) hat ein Exception geworfen
     */
    private void handleOutbox(MainHandler m, Anmeldedaten a) throws MessagingException, IOException {
        ArrayList<MessageHalter> outbox = new ArrayList<>();
        if(!a.messageHalter.get(6).isEmpty()){                                                                          //wenn es emails in dem Outbox folder gibt, so werden diese Alle verschickt
            outbox.addAll(a.messageHalter.get(6));
            for(MessageHalter message : outbox){
                smpt.sendmail(message);
                if(protokoll.equals("imap")){
                    deleteMail(message.getMessageID(),a.listFolders().get(6),a,m);
                }
                else {
                    a.messageHalter.get(4).add(0,message);
                    m.refreshScene(8,null,m);
                    a.messageHalter.get(6).remove(message);                                                             //Szene wird dann ungedatet und die Mails werden aus Outbox gelöscht
                }

            }
        }
    }

    /**
     * Diese Methode updated die lokal gespeicherten Emails. Da dieser Check auf einem Thread läuft(Siehe Mainhndler checker) ist es geeignet den Mainhandler und
     * den User Obbjekt als parameter zu geben. Die Methode läuft analog wie für Pop3 nur auf allen Folders
     * @param m Zugriff auf activeStore und refreshScene(...)
     * @param a Fehlervermeidung
     * @throws MessagingException Herunterladen der Mails ist fehlgeschlagen
     * @throws IOException refreshScene(...) hat ein Exception geworfen
     */
    public void checkForNew(MainHandler m, Anmeldedaten a) throws MessagingException, IOException {
        handleOutbox(m, a);
        Boolean hasNew = false;
        Store store;
        if(a == m.getActiveUser()){
            store = m.getStore();
            if(!store.isConnected()){
                store.connect();
            }
        }
        else {
            store = a.getMailer().connectStore();
        }
        for(String s : a.folders){
            int index = a.folders.indexOf(s);
            if(index > a.folders.size()-1){
                return;
            }

            searchTerm = new SearchTerm() {
                @Override
                public boolean match(Message msg) {
                    try {
                        return msg.getSentDate().after(a.lastmail.get(index));
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
                    return false;
                }
            };
            if(store==null|| !store.isConnected()){
                return;
            }
            Folder f = store.getFolder(s);
            UIDFolder uidFolder = (UIDFolder) f;
            f.open(Folder.READ_ONLY);

            Message[] newMsg = f.search(searchTerm);
            if(newMsg.length != 0){
                a.lastmail.set(index, newMsg[newMsg.length-1].getReceivedDate());
                hasNew = true;
                Message show = newMsg[newMsg.length-1];
                onMailReceived(show, m.popPlayer, index, m.getPrimary());
            }
            ArrayList<String> ids = new ArrayList<>();
            for (Message ms : newMsg){
                ids.add(String.valueOf(uidFolder.getUID(ms)));
            }
            ArrayList<MessageHalter> oldList = new ArrayList<>(a.getMail(index));
            ArrayList<MessageHalter> newStorable = a.mailGetter.readMessage(newMsg,ids, folders.get(index), false);
            for (MessageHalter message: newStorable){
                if(oldList.size()+newStorable.size()>10){
                    oldList.remove(oldList.size()-1);
                }
            }
            oldList.addAll(0,newStorable);
            int last = oldList.size()-1;
            if(last<0){
                last =0;
            }
            if(9<last){
                last = 9;
            }
            int lastonline =f.getMessageCount()-9;
            if(lastonline<1){
                lastonline=1;
            }
            if(oldList.size() != 0){
                if(((!hasNew && f.getMessageCount()!=0) && !oldList.get(last).getSentDate().equals(f.getMessage(lastonline).getSentDate())) || f.getMessageCount()<oldList.size()){
                    ids = new ArrayList<>();
                    for(int i=10; i>=1;i--){
                        if(f.getMessageCount()-i<1){
                            continue;
                        }
                        ids.add(String.valueOf(uidFolder.getUID(f.getMessage(f.getMessageCount()-i))));
                    }
                    if(f.getMessageCount()<oldList.size()){
                        oldList = new ArrayList<>(a.mailGetter.readMessage(f.getMessages(), ids, f.getName(),false));
                        a.messageHalter.set(index,oldList);
                        m.refreshScene(index+4,null,m);
                        continue;
                    }
                    int start = f.getMessageCount()-9;
                    int end = f.getMessageCount();
                    if (start < 1){ start = 1;}
                    if(end < 1){ end = 1;}
                    oldList = getMailer().readMessage(f.getMessages(start,end),ids,folders.get(index),false);
                    a.messageHalter.set(index,oldList);
                    m.refreshScene(index+4,null,m);
                }
            }
            a.messageHalter.set(index,oldList);
            if(hasNew){
               m.refreshScene(index+4,null,m);
            }
            f.close();
        }

    }

    /**
     * Wenn eine neue Mail kommt wird ein PopUp erscheinen
     * @param message die neue Mail
     * @param pop Der Sound
     * @param index Der Folder Index
     * @param primary Der Stage auf dem der popup erscheinen soll
     */
    public void onMailReceived(Message message, MediaPlayer pop, int index, Stage primary) {

        if (message != null && index==0) {
            try {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Stage secondStage = new Stage();
                        try {
                            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/new_message_popup.fxml"));
                            VBox popUp = fxmlLoader.load();
                            GUI_PopUp controller = fxmlLoader.getController();
                            controller.setUser("Der User " + username + " hat eine neue Nachricht");
                            controller.setAbsender_Label(Arrays.toString(message.getFrom()));
                            controller.setBetreff_Label(message.getSubject());
                            Scene scene = new Scene(popUp);
                            secondStage.setScene(scene);
                            pop.play();
                            secondStage.setX(primary.getX() + primary.getWidth()/2-178.5 );
                            secondStage.setY(primary.getY());
                            secondStage.show();
                            PauseTransition delay = new PauseTransition(Duration.seconds(5));
                            delay.setOnFinished( event -> secondStage.close() );
                            delay.play();
                        } catch (IOException | MessagingException e) {
                            e.printStackTrace();
                        }
                    }
                });


            }catch (Exception e){
                System.out.println("Notification error");
            }
        }

    }

    /**
     * Der User kann nachrichten löschen, diese Nachrichten werden dann lokal und online gelöscht. Nur für IMap geeignet.
     * @param id Mail id
     * @param folder Folder
     * @param a User
     */
    public void deleteMail(String id, String folder, Anmeldedaten a, MainHandler main){
        try {
            for(MessageHalter m : a.messageHalter.get(folders.indexOf(folder))){
                if(m.getMessageID().equals(id)){
                    ArrayList<MessageHalter> old = a.messageHalter.get((folders.indexOf(folder)));
                    old.remove(m);
                    a.messageHalter.set(folders.indexOf(folder), old );
                    a.messageHalter.get(folders.indexOf(folder)).remove(m);
                    main.refreshScene(folders.indexOf(folder) + 4,null,main );
                    break;
                }
            }
            Store store = main.getStore();
            Folder f = store.getFolder(folder);
            UIDFolder uf = (UIDFolder) f;
            f.open(Folder.READ_WRITE);
            for (Message m : f.getMessages()) {
                if (String.valueOf(uf.getUID(m)).equals(id)) {
                    Message[] msgs = {m};
                    if(folder.equals(listFolders().get(6))){
                        f.copyMessages(msgs, main.getStore().getFolder(listFolders().get(4)));
                    }
                    else {
                        f.copyMessages(msgs, main.getStore().getFolder(listFolders().get(2)));
                    }
                    m.setFlag(Flags.Flag.DELETED, true);
                }
            }
            f.close();
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }

    /** Hier werden Mails für das Pop Protokoll gelöscht
     *
     * @param m zugriff auf refreshScene(...) und activeStore
     * @param smsg Die zu löschende Nachricht
     * @param folder Der Folder
     * @throws MessagingException Verbindungsfehler
     * @throws IOException aus refreshScene
     */
    public void deletePop(MainHandler m, MessageHalter smsg, Folder folder) throws MessagingException, IOException {
        searchTerm = new SearchTerm() {
            @Override
            public boolean match(Message msg) {
                try {
                    boolean subjekt = msg.getSubject().equals(smsg.getSubjekt());
                    boolean sentdate = msg.getSentDate().equals(smsg.getSentDate());
                    return subjekt && sentdate;
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
                return false;
            }
        };
        Message[] messages = folder.search(searchTerm);
        if(messages.length==0){
            return;
        }
        else {
            messages[0].setFlag(Flags.Flag.DELETED, true);
        }
    }

    public ArrayList<MessageHalter> getMail( int i) {
        return messageHalter.get(i);
    }

    public SMTP getSMTP() {
        return smpt;
    }

    public ArrayList<String> listFolders(){
        return folders;
    }

    public String getPic() {
        return pic;
    }

    /**
     * Das Speichern des Profilsbild unter einem User spezifischen Folder. Unterstützt nur .png Dateien
     * @param pic der Pfaden des Bildes
     * @param m um auf den Pfaden zugreifen zu können
     * @throws IOException
     */
    public void setPic(String pic, MainHandler m) throws IOException {
        this.pic = pic;
        if(pic==null){
            return;
        }
        Path path = Paths.get(pic);
        File userFolder = new File(m.getPfad() + "\\" + uuid);
        if(!userFolder.exists()){
            userFolder.mkdirs();
        }
        Path destination = Paths.get(m.getPfad() + "\\"  + uuid + "\\" + "profile.png");
        File dest = new File(destination.toString());
        if(dest.exists()){
            dest.delete();
        }
        Files.copy(path,destination);
    }

    // Getters und setters folgen


    public EMailGetter getMailer() {
        return mailGetter;
    }

    public String getEmail() {
        return email;
    }

    public String getEingangserver() {
        return eingangserver;
    }

    public int getEingangport() {
        return eingangport;
    }

    public String getAusgangserver() {
        return ausgangserver;
    }

    public int getAusgangport() {
        return ausgangport;
    }

    public UUID getUuid(){
        return this.uuid;
    }

    public String getUsername(){
        return username;
    }

    public String getProtokoll() {
        return protokoll;
    }
}