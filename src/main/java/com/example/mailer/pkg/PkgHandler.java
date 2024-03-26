package com.example.mailer.pkg;

import com.example.mailer.Context;
import com.example.mailer.crypto.Cipher;
import com.example.mailer.utils.Constants;
import it.unisa.dia.gas.jpbc.Element;
import org.bouncycastle.util.encoders.Base64;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.*;


public class PkgHandler {


    public static JsonObject register(String email) {
        JsonObject params = Json.createObjectBuilder()
                .add("identity", email)
                .build();

        return requestPKG(Constants.REGISTER_ENDPOINT, params, "POST");
    }

    public static String confirmIdentity() {
        if(Context.isConnected()) {
            try {
                String params = "?client="+Context.CONNECTION_STATE.get("email");
                return Objects.requireNonNull(requestPKG(Constants.CHALLENGE_ENDPOINT+params, null, "GET")).get("message").toString();
            } catch (NullPointerException e) {
                System.out.println("Erreur: le mail d'activation n'a pas été envoyé");
            }
        } else {
            notConnectedError();
        }
        return null;
    }

    public static List<Element> getClient(String id) {
        if(Context.isConnected()) {
            String params = "?client="+id;

            // récupération de PK:
            try {
                JsonObject json = Objects.requireNonNull(requestPKG(Constants.PK_ENDPOINT+params, null, "GET"));
                String Qid_b64 = json.get("Qid").toString();
                String Kpub_b64 = json.get("Kpub").toString();
                byte[] Qid_bytes = Base64.decode(Qid_b64);
                byte[] Kpub_bytes = Base64.decode(Kpub_b64);

                return List.of(Cipher.PkgGenerator.getField().newElementFromBytes(Qid_bytes), Cipher.pairing.getG1().newElementFromBytes(Kpub_bytes));
            } catch (NullPointerException e) {
                System.out.println("Aucune clé PK récupérée");
            }
        } else {
            notConnectedError();
        }
        return null;
    }

    // Pour le debug
    public static Element getSK(String id) {
        if(Context.isConnected()) {
            String params = "?client="+id;

            // récupération de SK:
            try {
                String sk_b64 = Objects.requireNonNull(requestPKG(Constants.SK_ENDPOINT+params, null, "GET")).get("sk").toString();
                System.out.println(sk_b64);
                byte[] sk_bytes = Base64.decode(sk_b64);

                return Cipher.pairing.getZr().newElementFromBytes(sk_bytes);
            } catch (NullPointerException e) {
                System.out.println("Aucune clé SK récupérée");
            }
        } else {
            notConnectedError();
        }
        return null;
    }

    public static Element getSkByValidation() {
        if(Context.isConnected()) {
            JsonObject params = Json.createObjectBuilder()
                    .add("token", Context.CHALLENGE_TOKEN)
                    .build();

            System.out.println(params.toString());
            // récupération de SK:
            try {
                System.out.println(Constants.VALIDATE_ENDPOINT+"?client="+Context.CONNECTION_STATE.get("email"));
                JsonObject json = Objects.requireNonNull(requestPKG(Constants.VALIDATE_ENDPOINT+"?client="+Context.CONNECTION_STATE.get("email"), params,"POST"));

                String sk_b64 = json.get("sk").toString();
                String P_b64 = json.get("P").toString();
                byte[] sk_bytes = Base64.decode(sk_b64);
                byte[] P_bytes = Base64.decode(P_b64);

                Cipher.initPkgGenerator(Cipher.pairing.getG1().newElementFromBytes(P_bytes));

                return Cipher.pairing.getZr().newElementFromBytes(sk_bytes);

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
            JsonObject jsonResponse;

            if(responseCode == 200) {
                // Lecture de la réponse JSON
                try (JsonReader jsonReader = Json.createReader(conn.getInputStream())) {
                    jsonResponse = jsonReader.readObject();
                }
            } else if(responseCode == 204) {
                jsonResponse = Json.createObjectBuilder()
                        .add("message", "Un mail de confirmation à été envoyé")
                        .build();
            } else {
                try (JsonReader jsonReader = Json.createReader(conn.getErrorStream())) {
                    jsonResponse = jsonReader.readObject();
                    System.out.println("Erreur lors de la requete: " + jsonResponse.get("error"));
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
