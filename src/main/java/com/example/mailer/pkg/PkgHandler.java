package com.example.mailer.pkg;

import com.example.mailer.Context;
import com.example.mailer.crypto.ElGamal;
import com.example.mailer.utils.Constants;
import it.unisa.dia.gas.jpbc.Element;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
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

    public static Element getPK(String id) {
        if(Context.isConnected()) {
            String params = "?client="+id;

            // récupération de PK:
            try {
                String pk_b64 = Objects.requireNonNull(requestPKG(Constants.PK_ENDPOINT+params, null, "GET")).get("pk").toString();
                byte[] pk_bytes = Base64.getDecoder().decode(pk_b64);

                return ElGamal.generator.getField().newElementFromBytes(pk_bytes);
            } catch (NullPointerException e) {
                System.out.println("Aucune clé PK récupérée");
            }
        } else {
            notConnectedError();
        }
        return null;
    }



    // ROUTE DE DEBUG (jamais utilisée)
    public static Element getSK(String id) {
        if(Context.isConnected()) {
            String params = "?client="+id;

            // récupération de SK:
            try {
                String sk_b64 = Objects.requireNonNull(requestPKG(Constants.SK_ENDPOINT+params, null, "GET")).get("sk").toString();
                System.out.println(sk_b64);
                byte[] sk_bytes = Base64.getDecoder().decode(sk_b64);

                return ElGamal.pairing.getZr().newElementFromBytes(sk_bytes);
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
            ElGamal.generateKeyPair();
            System.out.println("PK: "+Context.CHALLENGE_PK);
            JsonObject params = Json.createObjectBuilder()
                    .add("token", Context.CHALLENGE_TOKEN)
                    .add("pk", Base64.getEncoder().encodeToString(Context.CHALLENGE_PK.toBytes()))
                    .build();

            // récupération de SK:
            try {
                System.out.println(Constants.VALIDATE_ENDPOINT+"?client="+Context.CONNECTION_STATE.get("email"));
                JsonObject resp = Objects.requireNonNull(requestPKG(Constants.VALIDATE_ENDPOINT+"?client="+Context.CONNECTION_STATE.get("email"), params,"POST"));

                Element U = ElGamal.generator.getField().newElementFromBytes(Base64.getDecoder().decode(resp.get("U").toString()));
                Element V = ElGamal.generator.getField().newElementFromBytes(Base64.getDecoder().decode(resp.get("V").toString()));
                System.out.println("U for SK': "+U);
                System.out.println("V for SK': "+V);
                Element u_p = U.duplicate().mulZn(Context.CHALLENGE_SK);
                Element sk = V.duplicate().sub(u_p);

                return ElGamal.pairing.getZr().newElementFromBytes(sk.toBytes());

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
