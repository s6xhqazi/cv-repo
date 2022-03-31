import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
// Postausgangsserver -> SMTP wird immer verwendet

public class SMTP implements Serializable {

    String email_adresse;
    Properties props;
    String passwort;

    /**
     * Erzeugung der Zugangsdaten
     * @param email_adresse Adresse
     * @param passwort Passwort
     * @param postausgangsserver Server
     * @param postausgangsserverport Port
     */
    public SMTP(String email_adresse, String passwort,String postausgangsserver, int postausgangsserverport)
    {
        this.passwort = passwort;
        props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", postausgangsserver);
        props.put("mail.smtp.port", postausgangsserverport);
        this.email_adresse = email_adresse;
    }

    /**
     * Session wird erstellt
     * @return Session wird zurückgegeben
     */
    protected Session getSession(){
        Session session;
        session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(email_adresse, passwort);
                    }
                });
        return session;
    }

    /**
     * Email wird versendet
     * @param m Nachricht zu versenden
     */
    public void sendmail(MessageHalter m)
    {
        try {
            Message msg = new MimeMessage(getSession());
            Address a = new InternetAddress(this.email_adresse);
            Address b = new InternetAddress(m.getEmpfaengerListe());
            msg.setContent(m.getContent(), "text/plain");
            msg.setFrom(a);
            msg.setRecipient(Message.RecipientType.TO, b);
            msg.setSubject(m.getSubjekt());
            System.out.println("Send message");
            Transport.send(msg);
        } catch (MessagingException e) {
            System.out.println("Nachricht nicht versendet");
        }
    }
}