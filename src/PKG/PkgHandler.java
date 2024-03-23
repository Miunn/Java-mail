package PKG;

import app.Context;
import cypher.ElGamal;
import it.unisa.dia.gas.jpbc.Element;
import org.bouncycastle.util.encoders.Base64;
import utils.Constants;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.http.HttpClient;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;

public class PkgHandler {


    public static String register(String email) {
        HashMap<String,String> params = new HashMap<>();
        params.put("identity", email);

        return requestPKG(Constants.REGISTER_ENDPOINT, params);
    }

    public static String confirmIdentity() {
        if(Context.isConnected()) {
            HashMap<String,String> params = new HashMap<>();
            params.put("identity", Context.CONNECTION_STATE.get("email"));

            return requestPKG(Constants.CHALLENGE_ENDPOINT, params);
        } else {
            notConnectedError();
            return null;
        }
    }

    public static String getPK(String id) {
        if(Context.isConnected()) {
            HashMap<String,String> params = new HashMap<>();
            params.put("identity", id);

            return requestPKG(Constants.PK_ENDPOINT, params);
        } else {
            notConnectedError();
            return null;
        }
    }

    public static Element getSK() {
        if(Context.isConnected()) {
            HashMap<String,String> params = new HashMap<>();
            params.put("token", Context.CHALLENGE_TOKEN);

            // récupération de SK:
            try {
                String sk_b64 = requestPKG(Constants.SK_ENDPOINT, params);// TODO: recup sk dans la reponse
                byte[] sk_bytes = Base64.decode(Objects.requireNonNull(sk_b64));

                return ElGamal.generator.getField().newElementFromBytes(sk_bytes);
            } catch (NullPointerException e) {
                System.out.println("Aucune clé SK récupérée");
            }
        } else {
            notConnectedError();
        }
        return null;
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
                    .uri(URI.create(Constants.PKG_HOST+endpoint))
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
