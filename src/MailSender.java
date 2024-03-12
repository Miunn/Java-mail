import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MailSender {

    public void sendMail(Mail mail) {
        try {
            URL url = new URL("http://" + Constants.PKG_HOST + Constants.PKG_PORT + "/service");

            URLConnection urlConn = url.openConnection();
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            OutputStream out = urlConn.getOutputStream();
            //out.write(user_name.getBytes());
            out.write("salut...".getBytes());

            InputStream dis = urlConn.getInputStream();
            byte[] b = new byte[Integer.parseInt(urlConn.getHeaderField("Content-length"))];
            dis.read(b);

            String response = new String(b);
            System.out.println("message reçu du serveur:" + response);

        } catch (
                IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
