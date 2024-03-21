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

    public static String confirmIdentity() {
        if(Context.CONNECTED) {
            HashMap<String,String> params = new HashMap<>();
            params.put("client", Context.ID);

            return requestPKG(Constants.CHALLENGE_ENDPOINT, params);
        } else {
            notConnectedError();
            return null;
        }
    }

    public static String getPK(String id) {
        if(Context.CONNECTED) {
            HashMap<String,String> params = new HashMap<>();
            params.put("client", id);

            return requestPKG(Constants.PK_ENDPOINT, params);
        } else {
            notConnectedError();
            return null;
        }
    }

    public static String getSK() {
        if(Context.CONNECTED) {
            HashMap<String,String> params = new HashMap<>();
            params.put("client", Context.ID);
            params.put("token", Context.CHALLENGE_TOKEN);

            return requestPKG(Constants.SK_ENDPOINT, params);// TODO: parser le json pour récuperer sk
        } else {
            notConnectedError();
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

    public static void notConnectedError() {
        // TODO: afficher la page de connexion
        System.out.println("Veuillez vous connecter pour effectuer cette action");
    }
}
