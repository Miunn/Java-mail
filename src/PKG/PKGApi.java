package PKG;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import it.unisa.dia.gas.jpbc.Element;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;

import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class PKGApi {

    private HashMap<String, Client> clients = new HashMap<>();
    private PKGIdentity pkg;

    public PKGApi() {
        this.pkg = new PKGIdentity();
    }

    public void startServer() {
        InetSocketAddress s = new InetSocketAddress("localhost", 8080);

        try {
            HttpServer server = HttpServer.create(s, 1000);

            server.createContext("/", new HttpHandler() {
                public void handle(HttpExchange he) throws IOException {
                    he.getResponseHeaders().set("Content-Type", "application/json");
                    byte[] payload = ("{\"message\": \"hello\"}").getBytes();
                    he.sendResponseHeaders(200, payload.length);
                    OutputStream os = he.getResponseBody();
                    os.write(payload);
                    os.close();
                }
            });
            server.createContext("/register", handleRegisterRequest());
            server.createContext("/get", handleGetClientRequest());
            server.createContext("/challenge", handleChallengeRequest());
            server.createContext("/validate", handleValidateRequest());
            server.createContext("/sk", new HttpHandler() {
                public void handle(HttpExchange he) throws IOException {
                    if (!he.getRequestMethod().equals("GET")) {
                        writeMethodNotAllowed(he);
                        return;
                    }
    
                    Map<String, String> args = parseUriArgs(he.getRequestURI().getQuery());

                    he.getResponseHeaders().set("Content-Type", "application/json");
                    if (!args.containsKey("client")) {
                        he.sendResponseHeaders(400, "{\"error\": \"Missing client arguement\"}".getBytes().length);
                        OutputStream os = he.getResponseBody();
                        os.write("{\"error\": \"Missing client arguement\"}".getBytes());
                        os.close();
                        return;
                    }
    
                    Client client = clients.get(args.get("client"));
                    
                    if (client == null) {
                        writeClientNotFound(he);
                        return;
                    }

                    byte[] payload = ("{\"Qid\": \""+Base64.getEncoder().encodeToString(client.getPrivateKey().toBytes())+"\"}").getBytes();
                    he.sendResponseHeaders(200, payload.length);
                    OutputStream os = he.getResponseBody();
                    os.write(payload);
                    os.close();
                }
            });

            server.createContext("/token", new HttpHandler() {
                public void handle(HttpExchange he) throws IOException {
                    if (!he.getRequestMethod().equals("GET")) {
                        writeMethodNotAllowed(he);
                        return;
                    }
    
                    Map<String, String> args = parseUriArgs(he.getRequestURI().getQuery());
    
                    he.getResponseHeaders().set("Content-Type", "application/json");
                    if (!args.containsKey("client")) {
                        he.sendResponseHeaders(400, "{\"error\": \"Missing client arguement\"}".getBytes().length);
                        OutputStream os = he.getResponseBody();
                        os.write("{\"error\": \"Missing client arguement\"}".getBytes());
                        os.close();
                        return;
                    }
    
                    Client client = clients.get(args.get("client"));
                    
                    if (client == null) {
                        writeClientNotFound(he);
                        return;
                    }

                    byte[] payload = ("{\"token\": \""+client.getToken()+"\"}").getBytes();
                    he.sendResponseHeaders(200, payload.length);
                    OutputStream os = he.getResponseBody();
                    os.write(payload);
                    os.close();
                }
            });

            server.start();
            System.out.println("Server listening");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HttpHandler handleRegisterRequest() {
        return new HttpHandler() {
            public void handle(HttpExchange he) throws IOException {
                if (!he.getRequestMethod().equals("POST")) {
                    writeMethodNotAllowed(he);
                    return;
                }

                he.getResponseHeaders().set("Content-Type", "application/json");
                try {
                    JsonObject requestBody = Json.createReader(he.getRequestBody()).readObject();

                    Client newClient = registerNewClient(requestBody.getString("identity"));
                    System.out.println("Register: "+newClient.getIdentity());

                    he.sendResponseHeaders(200, pkg.getClientPublicJSON(newClient).toString().getBytes().length);

                    OutputStream os = he.getResponseBody();
                    os.write(pkg.getClientPublicJSON(newClient).toString().getBytes());
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
        };
    }

    public HttpHandler handleGetClientRequest() {
        return new HttpHandler() {
            public void handle(HttpExchange he) throws IOException {
                if (!he.getRequestMethod().equals("GET")) {
                    writeMethodNotAllowed(he);
                    return;
                }

                Map<String, String> args = parseUriArgs(he.getRequestURI().getQuery());

                he.getResponseHeaders().set("Content-Type", "application/json");
                if (!args.containsKey("client")) {
                    he.sendResponseHeaders(400, "{\"error\": \"Missing client argument\"}".getBytes().length);
                    OutputStream os = he.getResponseBody();
                    os.write("{\"error\": \"Missing client arguement\"}".getBytes());
                    os.close();
                    return;
                }

                Client client = clients.get(args.get("client"));
                if (client == null) {
                    writeClientNotFound(he);
                    return;
                }

                byte[] payload = pkg.getClientPublicJSON(client).toString().getBytes();
                he.sendResponseHeaders(200, payload.length);
                OutputStream os = he.getResponseBody();
                os.write(pkg.getClientPublicJSON(client).toString().getBytes());
                os.close();
            }
        };
    }

    public HttpHandler handleChallengeRequest() {
        return new HttpHandler() {
            public void handle(HttpExchange he) throws IOException {
                if (!he.getRequestMethod().equals("GET")) {
                    writeMethodNotAllowed(he);
                    return;
                }

                Map<String, String> args = parseUriArgs(he.getRequestURI().getQuery());

                he.getResponseHeaders().set("Content-Type", "application/json");
                if (!args.containsKey("client")) {
                    he.sendResponseHeaders(400, "{\"error\": \"Missing client arguement\"}".getBytes().length);
                    OutputStream os = he.getResponseBody();
                    os.write("{\"error\": \"Missing client arguement\"}".getBytes());
                    os.close();
                    return;
                }

                Client client = clients.get(args.get("client"));
                if (client == null) {
                    writeClientNotFound(he);
                    return;
                }
                
                client.generateChallengeCode();

                ///////////////////
                // SEND THE MAIL //
                ///////////////////
                sendmessage("serveurpkg@gmail.com", "ndca fknw bmcq xvej", args.get("client"), "PKG-Challenge", client.getToken());

                he.sendResponseHeaders(204, -1);
            }
        };
    }

    public HttpHandler handleValidateRequest() {
        return new HttpHandler() {
            public void handle(HttpExchange he) throws IOException {
                if (!he.getRequestMethod().equals("POST")) {
                    writeMethodNotAllowed(he);
                    return;
                }

                Map<String, String> args = parseUriArgs(he.getRequestURI().getQuery());

                he.getResponseHeaders().set("Content-Type", "application/json");
                if (!args.containsKey("client")) {
                    he.sendResponseHeaders(400, "{\"error\": \"Missing client arguement\"}".getBytes().length);
                    OutputStream os = he.getResponseBody();
                    os.write("{\"error\": \"Missing client arguement\"}".getBytes());
                    os.close();
                    return;
                }

                Client client = clients.get(args.get("client"));
                
                if (client == null) {
                    writeClientNotFound(he);
                    return;
                }

                try {
                    JsonObject requestBody = Json.createReader(he.getRequestBody()).readObject();

                    String token = requestBody.getString("token");

                    if (!client.validateChallenge(token)) {
                        byte[] payload = "{\"error\": \"Wrong token\"}".getBytes();
                        he.getResponseHeaders().set("Content-Type", "application/json");
                        he.sendResponseHeaders(400, payload.length);    
                        OutputStream os = he.getResponseBody();
                        os.write(payload);
                        os.close();
                        return;
                    }

                    //String pk_b64 = requestBody.getString("pk");
                    //System.out.println(pk_b64);
                    //Element[] UV = client.encodeSkFromPk(pk_b64);
                    //System.out.println("Got encoded");

                    //byte[] payload = ("{\"U\": \""+Base64.getEncoder().encodeToString(UV[0].toBytes())+"\", \"V\": \""+Base64.getEncoder().encodeToString(UV[1].toBytes())+"\"}").getBytes();
                    byte[] payload = ("{\"P\": \""+Base64.getEncoder().encodeToString(pkg.getP().toBytes())+"\",\"Qid\": \""+Base64.getEncoder().encodeToString(client.getPrivateKey().toBytes())+"\"}").getBytes();
                    he.getResponseHeaders().set("Content-Type", "application/json");
                    he.sendResponseHeaders(200, payload.length);
                    OutputStream os = he.getResponseBody();
                    os.write(payload);
                    os.close();
                } catch (JsonException e) {
                    he.sendResponseHeaders(400, 41);
                    OutputStream os = he.getResponseBody();
                    os.write("{\"error\": \"Unable to parse request body\"}".getBytes());
                    os.close();
                } catch (NullPointerException e) {
                    he.sendResponseHeaders(400, 47);
                    OutputStream os = he.getResponseBody();
                    os.write("{\"error\": \"Body token or pkc attribute is missing or challenge already validated\"}".getBytes());
                    os.close();
                }
            }
        };
    }

    private void writeMethodNotAllowed(HttpExchange he) throws IOException {
        he.getResponseHeaders().set("Content-Type", "application/json");
        he.sendResponseHeaders(405, 31);
        OutputStream os = he.getResponseBody();
        os.write("{\"error\": \"Method not allowed\"}".getBytes());
        os.close();
    }

    private void writeClientNotFound(HttpExchange he) throws IOException {
        he.getResponseHeaders().set("Content-Type", "application/json");
        he.sendResponseHeaders(404, "{\"error\": \"Client not found\"}".getBytes().length);
        OutputStream os = he.getResponseBody();
        os.write("{\"error\": \"Client not found\"}".getBytes());
        os.close();
    }

    private Map<String, String> parseUriArgs(String query) {
        Map<String, String> map = new HashMap<>();

        if (query == null) {
            return map;
        }

        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                map.put(entry[0], entry[1]);
            } else {
                map.put(entry[0], "");
            }
        }
        
        return map;
    }

    private Client registerNewClient(String identity) throws ClientAlreadyExistsException {
        if (clients.get(identity) != null) {
            throw new ClientAlreadyExistsException();
        }

        Client newClient = new Client(identity);

        this.pkg.generateKeyPairForClient(newClient);

        clients.put(identity, newClient);
        return newClient;
    }

    private void sendmessage(String user, String password, String destination, String subject, String text) {
        Properties properties = new Properties();

        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(user,password);
			}
		});
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(user);
            message.setSubject(subject);
            message.setText(text);
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destination));
            Transport.send(message);
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }
}
