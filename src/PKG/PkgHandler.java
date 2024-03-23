package PKG;

import app.Context;
import cypher.ElGamal;
import it.unisa.dia.gas.jpbc.Element;
import org.bouncycastle.util.encoders.Base64;
import utils.Constants;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
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

import javax.json.*;

public class PkgHandler {


    public static JsonObject register(String email) {
        JsonObject params = Json.createObjectBuilder()
                .add("identity", email)
                .build();

        return requestPKG(Constants.REGISTER_ENDPOINT, params, "POST");
    }

    public static void confirmIdentity() {
        if(Context.isConnected()) {
            String params = "?client="+Context.CONNECTION_STATE.get("email");
            requestPKG(Constants.CHALLENGE_ENDPOINT+params, null, "GET");
        } else {
            notConnectedError();
        }
    }

    public static Element getPK(String id) {
        if(Context.isConnected()) {
            String params = "?client="+id;

            // récupération de PK:
            try {
                String sk_b64 = Objects.requireNonNull(requestPKG(Constants.SK_ENDPOINT+params, null, "GET")).get("pk").toString();
                byte[] sk_bytes = Base64.decode(sk_b64);

                return ElGamal.generator.getField().newElementFromBytes(sk_bytes);
            } catch (NullPointerException e) {
                System.out.println("Aucune clé PK récupérée");
            }
        } else {
            notConnectedError();
        }
        return null;
    }

    public static Element getSK() {
        if(Context.isConnected()) {
            JsonObject params = Json.createObjectBuilder()
                    .add("token", Context.CHALLENGE_TOKEN)
                    .build();

            // récupération de SK:
            try {
                String sk_b64 = Objects.requireNonNull(requestPKG(Constants.SK_ENDPOINT, params,"POST")).get("sk").toString();
                byte[] sk_bytes = Base64.decode(sk_b64);

                return ElGamal.generator.getField().newElementFromBytes(sk_bytes);
            } catch (NullPointerException e) {
                System.out.println("Aucune clé SK récupérée");
            }
        } else {
            notConnectedError();
        }
        return null;
    }


    public static JsonObject requestPKG(String endpoint, JsonObject post_arguments, String method) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(Constants.PKG_HOST + endpoint).openConnection();

            conn.setRequestMethod(method);
            conn.setDoOutput(true);

            if(method.equals("POST")) {
                conn.setRequestProperty("Content-Type", "application/json");
                try (OutputStream os = conn.getOutputStream(); JsonWriter jw = Json.createWriter(os)) {
                    jw.writeObject(post_arguments);
                }
            }

            // Récupération de la réponse
            int responseCode = conn.getResponseCode();
            JsonObject jsonResponse = null;

            if (responseCode != 200) {
                try (JsonReader jsonReader = Json.createReader(conn.getErrorStream())) {
                    jsonResponse = jsonReader.readObject();
                    System.out.println("Erreur lors de la requete: " + jsonResponse.get("error"));
                }
            } else {
                // Lecture de la réponse JSON
                try (JsonReader jsonErrorReader = Json.createReader(conn.getInputStream())) {
                    System.out.println("Réponse JSON : " + jsonErrorReader.readObject());
                }
            }
            // Fermeture de la connexion
            conn.disconnect();

            return jsonResponse;
        } catch (IOException ex) {
            Logger.getLogger(PkgHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }


    public static void notConnectedError() {
        System.out.println("Veuillez vous connecter pour effectuer cette action");
    }
}
