import it.unisa.dia.gas.jpbc.Element;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Mail {
    private String from;
    private String to;
    private String subject;
    private String message;
    private String attachment;
    private ElGamalCipherText enc;
    private Boolean isEncrypted;


    public Mail(String from, String to, String subject, String message, String attachment, ElGamalCipherText encryptedAttachment, Boolean isEncrypted) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.message = message;
        this.attachment = attachment;
        this.enc = encryptedAttachment;
        this.isEncrypted = isEncrypted;
    }


    public void encryptMail() {
        if (!isEncrypted) {
            ElGamalCipherText enc = ElGamal.encryptMail(attachment.getBytes());
            if (enc == null) {
                Logger.getLogger(Mail.class.getName()).log(Level.SEVERE, "Mail encryption failed");
            } else {
                this.isEncrypted = true;
                this.enc = enc;
            }
        }
    }

    public void decryptMail() {
        if (isEncrypted) {
            Element sk = ElGamal.get
            String plain = ElGamal.decryptMail(enc, sk);
            if (plain == null) {
                Logger.getLogger(Mail.class.getName()).log(Level.SEVERE, "Mail decryption failed");
            } else {
                this.isEncrypted = false;
                this.attachment = plain;
            }
        }
    }
}
