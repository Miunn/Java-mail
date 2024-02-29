import org.bouncycastle.util.encoders.Base64;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.http.HttpClient;

public class PkgHandler {

    public static String requestSK() {
        try {
            Map<String,String> arguments = new HashMap<>();
            arguments.put(Constants.ID_param, "");
            arguments.put(Constants.PKc_param, "");
            StringJoiner sj = new StringJoiner("&");

            for(Map.Entry<String,String> entry : arguments.entrySet())
                sj.add(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));

            // TODO: est ce qu'on envoie un byte[] ou un json ?
            byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
            int length = out.length;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(Constants.PKG_HOST+":"+Constants.PKG_PORT+Constants.PKG_PATH))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(out))
                    .build();

            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray()); // TODO: est ce qu'on recoit un byte[] ou un json ?

            return new String(Base64.decode(response.body())); // TODO : à changer en fonction de ce qu'on recoit etc...

        } catch (IOException ex) {
            Logger.getLogger(PkgHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
