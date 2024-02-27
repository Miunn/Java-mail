package PKG;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class PKGApi {

    private HashMap<String, Client> clients = new HashMap<>();

    public PKGApi() {

    }

    public void startServer() {
        InetSocketAddress s = new InetSocketAddress("localhost", 8080);

        try {
            HttpServer server = HttpServer.create(s, 1000);

            server.createContext("/register", new HttpHandler() {
                public void handle(HttpExchange he) throws IOException {
                    try {
                        JsonObject requestBody = Json.createReader(he.getRequestBody()).readObject();                        

                        Client newClient = registerNewClient(requestBody.getString("identity"));

                        he.getResponseHeaders().set("Content-Type", "application/json");
                        he.sendResponseHeaders(200, newClient.getClientPublicJSON().toString().getBytes().length);
                        
                        OutputStream os = he.getResponseBody();
                        os.write(newClient.getClientPublicJSON().toString().getBytes());
                        os.close();
                    } catch (JsonException e) {
                        he.sendResponseHeaders(400, 41);
                        OutputStream os = he.getResponseBody();
                        os.write("{\"error\": \"Unable to parse request body\"}".getBytes());
                        os.close();
                    } catch (ClientAlreadyExistsException e) {
                        he.sendResponseHeaders(403, 32);
                        OutputStream os = he.getResponseBody();
                        os.write("{\"error\": \"User already exists\"}".getBytes());
                        os.close();
                    } catch (NullPointerException e) {
                        he.sendResponseHeaders(400, 47);
                        OutputStream os = he.getResponseBody();
                        os.write("{\"error\": \"Body identity attribute is missing\"}".getBytes());
                        os.close();
                    }
                }
            });

            server.start();
            System.out.println("Server listening");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Client registerNewClient(String identity) throws ClientAlreadyExistsException {
        if (clients.get(identity) != null) {
            throw new ClientAlreadyExistsException();
        }

        Client newClient = new Client(identity);
        clients.put(identity, newClient);
        return newClient;
    }
}
