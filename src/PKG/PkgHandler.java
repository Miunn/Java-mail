package PKG;

import app.Context;
import utils.Constants;

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

    public static String getSK() {
        if(Context.CONNECTED) {
            HashMap<String,String> params = new HashMap<>();
            params.put("ID", Context.ID);
            return requestPKG(Constants.SK_ENDPOINT, params);
        } else {
            // TODO: page de connexion
            System.out.println("Vous n'êtes pas connecté");
            return null;
        }
    }


    public static String requestPKG(String endpoint, HashMap<String,String> arguments) {
        try {
            StringJoiner sj = new StringJoiner("&");
            for(Map.Entry<String,String> entry : arguments.entrySet())
                sj.add(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));

            byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
            int length = out.length;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(Constants.PKG_HOST+":"+Constants.PKG_PORT+endpoint))
                    .POST(HttpRequest.BodyPublishers.ofByteArray(out))
                    .build();

            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            return new String(response.body());

        } catch (IOException ex) {
            Logger.getLogger(PkgHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
