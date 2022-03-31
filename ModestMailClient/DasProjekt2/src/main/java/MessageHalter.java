import javax.mail.Address;
import java.io.Serializable;
import java.util.Date;

public class MessageHalter implements Serializable {
    private final String content;
    private final Address[] fromAddress;
    private final String sender;
    private final String subjekt;
    private final String empfaengerListe;
    private final String ccListe;
    private Date datum;
    private final String art;
    private String messageContent = "";
    private final String messageID;
    private final String folder;
    public boolean neu;


    /**
     * Ist ein Serializable datenstruktur die die Daten aus einer Mail speichert
     * @param adr
     * @param sender
     * @param subjekt
     * @param empfaengerListe
     * @param ccListe
     * @param datum
     * @param art
     * @param messageContent
     * @param content
     * @param messageID
     * @param folder
     */
    public MessageHalter(Address[] adr, String sender, String subjekt, String empfaengerListe, String ccListe, Date datum, String art, String messageContent, String content, String messageID, String folder) {
        this.fromAddress = adr;
        this.sender = sender;
        this.subjekt = subjekt;
        this.empfaengerListe = empfaengerListe;
        this.ccListe = ccListe;
        this.datum = datum;
        this.art = art;
        this.messageContent = messageContent;
        this.content = content;
        this.messageID = messageID;
        this.folder = folder;
    }


    //Getters
    public String getFolder(){
        return folder;
    }

    public String getEmpfaengerListe(){ return empfaengerListe;}

    public  String getContent(){
        return content;
    }

    public String getMessageID(){
        return messageID;
    }

    public Date getSentDate() {
        return datum;
    }


    public String getSubjekt() {
        return subjekt;
    }

    public Address[] getFrom() {
        return fromAddress;
    }
}
