import javax.mail.*;
import javax.mail.Message.RecipientType;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

public class EMailGetter implements Serializable {


    private final Properties props = new Properties();
    private final String protokoll;
    private final String email;
    private final String passwort;

    /**Parameter für POP3 oder Imap
     *
     * @param protokoll das gewünschte Protokoll
     * @param emaileingangserver Der Server
     * @param port Der Port
     * @param email Die Adresse
     * @param password Das Passwort
     */
    public EMailGetter(String protokoll, String emaileingangserver, int port, String email, String password) {
        this.email = email;
        this.passwort = password;
        this.protokoll = protokoll;
        props.put(String.format("mail.%s.host", protokoll), emaileingangserver);
        props.put(String.format("mail.%s.port", protokoll), port);
        props.setProperty(String.format("mail.%s.socketFactory.class", protokoll), "javax.net.ssl.SSLSocketFactory");
        props.setProperty(String.format("mail.%s.socketFactory.fallback", protokoll), "false");
        props.setProperty(String.format("mail.%s.socketFactory.port", protokoll), String.valueOf(port));

    }

    /**
     * Daten werden überprüft und ein Session wird erstellt
     * @return gibt den Session zurück
     */
    public Session getSession(){
        return Session.getDefaultInstance(props);
    }

    /**
     * Versucht eine Verbindungs-Store zu erstellen
     * @return gibt diese Verbindung zurück
     * @throws MessagingException falls die Verbindung fehlschlägt
     */
    public Store connectStore() throws MessagingException {
        try {
            Store store = getSession().getStore(protokoll);
            store.connect(email, passwort);
            return store;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Lädt alle Emails bei einem Foldern runter
     * @param folder Der Folder-Name
     * @param store Die Verbindung
     * @return gibt eine arraylist mit den Nachrichten zurück
     */
    public ArrayList<MessageHalter> downloadEmails(String folder, Store store) {
        try {
            Folder folderInbox = store.getFolder(folder);
            if(!folderInbox.exists()){
                folderInbox.create(0);
            }

            UIDFolder ufFolder = (UIDFolder) folderInbox;
            folderInbox.open(Folder.READ_ONLY);
            int countBegin = folderInbox.getMessageCount();
            int countEnd = countBegin-10;
            Message[] messages;
            if(countEnd < 1){
                countEnd = 1;
            }
            if(countBegin > 1){
                messages = folderInbox.getMessages(countEnd,countBegin);
            }
            else {
                messages = folderInbox.getMessages();
            }
            ArrayList<String> ids = new ArrayList<>();
            for(Message m : messages){
                ids.add(String.valueOf(ufFolder.getUID(m)));
            }
            ArrayList<MessageHalter> storableMessages = readMessage(messages,ids,folderInbox.getName(),false);
            folderInbox.close();
            return storableMessages;
        } catch (NoSuchProviderException ex) {
            System.out.println("Server nicht gefunden für " + protokoll);
            ex.printStackTrace();
        } catch (MessagingException ex) {
            System.out.println("Verbindung nicht möglich");
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<MessageHalter>();
    }

    /**
     * Die Nachrichten werden zu Seriaizable Datenform MessageHalter verwandelt
     * @param messages Die Nachrichten
     * @param ids Die Ids
     * @param folder Der Foldername
     * @param isPop Ob es sich um POP handlet
     * @return gibt die zu speichernde Datenstrukturen zurück
     * @throws MessagingException beim Lesen der Mails
     * @throws IOException Beim lesen/schreiben der Datein
     */
    public ArrayList<MessageHalter> readMessage(Message[] messages, ArrayList<String> ids, String folder, boolean isPop) throws MessagingException, IOException {
        ArrayList<MessageHalter> storableMessages = new ArrayList<>();
        for (int i = messages.length-1; i > messages.length - 11 && i >= 0; i--) {
            Message msg = messages[i];
            Address[] fromAddress = msg.getFrom();
            String sender = fromAddress[0].toString();
            String subjekt = msg.getSubject();
            String empfaengerListe = parseAddresses(msg.getRecipients(RecipientType.TO));
            String ccListe = parseAddresses(msg.getRecipients(RecipientType.CC));
            Date datum = msg.getSentDate();
            String art = msg.getContentType();
            String messageContent;
            messageContent = getText(msg);
            String id;
            if(!isPop){
                id = ids.get(ids.size()-1);
            }
            else {
                id = "";
            }
            boolean neu = !msg.getFlags().contains(Flags.Flag.SEEN);
            try {
                    id = ids.get(i);
            }
            catch (Exception e){
                System.out.println("Jabaaaaaaaaa");
            }
            MessageHalter messageHalter = new MessageHalter(fromAddress, sender, subjekt, empfaengerListe, ccListe, datum, art, messageContent, messageContent,id,folder);
            messageHalter.neu = neu;
            storableMessages.add(messageHalter);
        }
        return storableMessages;
    }

    /**
     * Mail wird als gelesen markier, auch Online
     * @param m die zu markierende Mail
     */
    public void messageGelesen(MessageHalter m){
        Thread thread = new Thread(()-> {
            try {
                Folder folder = connectStore().getFolder(m.getFolder());
                folder.open(Folder.READ_WRITE);
                UIDFolder uidfolder = (UIDFolder) folder;
                uidfolder.getMessageByUID(Long.parseLong(m.getMessageID())).setFlag(Flags.Flag.SEEN, true);
                folder.close();
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }


    /**
     * Aus Mime zu text, sei es HTML oder Plain
     * @param p MimePart der übersetzt werden muss
     * @return String mit dem Body
     * @throws MessagingException Nachricht lesen
     * @throws IOException Datei Lesen
     */
    private String getText(Part p) throws
            MessagingException, IOException {
        if (p.isMimeType("text/*")) {
            return (String) p.getContent();
        }

        if (p.isMimeType("multipart/alternative")) {
            Multipart mp = (Multipart) p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getText(bp);
                } else if (bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    if (s != null)
                        return s;
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }

        return null;
    }

    /**
     * Vom INternet adresse zu String
     * @param address Adresse aus dem Mail
     * @return String zu speichern, anzeigen
     */
    private String parseAddresses(Address[] address) {
        StringBuilder listAddress = new StringBuilder();

        if (address != null) {
            for (Address value : address) {
                listAddress.append(value.toString()).append(", ");
            }
        }
        if (listAddress.length() > 1) {
            listAddress = new StringBuilder(listAddress.substring(0, listAddress.length() - 2));
        }

        return listAddress.toString();
    }

    /**
     * Verwendet bei POP, ladet den Inbox-Folder runter
     * @param store
     * @return
     * @throws MessagingException
     * @throws IOException
     */
    public ArrayList<MessageHalter> downloadInbox(Store store) throws MessagingException, IOException {
        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);
        int begin = inbox.getMessageCount()-9;
        int end = inbox.getMessageCount();
        if(begin<1){
            begin = 1;
        }
        if(end<1){
            end = 1;
        }
        Message[] messages = inbox.getMessages(begin, end);
        ArrayList<MessageHalter> storableMessages = readMessage(messages,null,"INBOX",true);
        inbox.close();
        return storableMessages;
    }
}