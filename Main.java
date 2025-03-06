import java.util.HashMap;
import java.util.Map;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import spark.Spark;
import spark.utils.StringUtils;

public class Main {
    private static Map<String, Session> sessions = new HashMap<>();
    private static final String adminUsername = "TalP4";
    private static final String adminPassword = "Project Anonymous";

    public static void main(String[] args) throws Exception {
        // Set up Jetty server
        Server server = new Server(8080);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);
        
        // Add a simple servlet at "/"
        context.addServlet(new ServletHolder(new HelloServlet()), "/*");

        server.start();
        
        Spark.webSocket("/chat", ChatWebSocket.class);
        Spark.init();

        Spark.post("/login", (req, res) -> {
            String username = req.queryParams("username");
            String password = req.queryParams("password");

            if (username.equals(adminUsername) && password.equals(adminPassword)) {
                return "admin";
            } else {
                return "invalid";
            }
        });

        Spark.post("/message", (req, res) -> {
            String message = req.queryParams("message");
            ChatWebSocket.broadcast("Server", message);
            return "Message sent";
        });

        Spark.post("/impersonate", (req, res) -> {
            String userToImpersonate = req.queryParams("user");
            String message = req.queryParams("message");
            ChatWebSocket.broadcast(userToImpersonate, message);
            return "Message sent";
        });

        Spark.post("/shutdown", (req, res) -> {
            System.exit(0);
            return "Shutting down";
        });

        Spark.post("/ban", (req, res) -> {
            String userToBan = req.queryParams("user");
            banUser(userToBan);
            return "User banned";
        });

        Spark.post("/restrict", (req, res) -> {
            String userToRestrict = req.queryParams("user");
            restrictUser(userToRestrict);
            return "User restricted";
        });
    }

    private static void banUser(String username) {
        // Implement user banning logic
        System.out.println("User " + username + " banned");
    }

    private static void restrictUser(String username) {
        // Implement user restriction logic
        System.out.println("User " + username + " restricted");
    }

    public static Map<String, Session> getSessions() {
        return sessions;
    }

    @WebSocket
    public static class ChatWebSocket {

        @OnWebSocketConnect
        public void onConnect(Session session) throws Exception {
            System.out.println("Connect: " + session.getRemoteAddress().getAddress());
        }

        @OnWebSocketClose
        public void onClose(Session session, int statusCode, String reason) {
            System.out.println("Close: " + session.getRemoteAddress().getAddress());
        }

        @OnWebSocketMessage
        public void onMessage(Session session, String message) {
            System.out.println("Message: " + message);
            String[] parts = message.split(" ");
            String username = parts[0];
            String password = parts[1];
            String action = parts[2];
            String target = parts[3];

            if ("admin".equals(username) && "admin".equals(password)) {
                if ("message".equals(action)) {
                    getSessions().values().stream()
                            .filter(s -> StringUtils.isNotBlank(target))
                            .forEach(s -> sendMessage(s, username + ": " + target));
                } else if ("impersonate".equals(action)) {
                    getSessions().values().stream()
                            .filter(s -> StringUtils.isNotBlank(target))
                            .forEach(s -> sendMessage(s, username + " (impersonating " + target + "): " + parts[4]));
                } else if ("shutdown".equals(action)) {
                    System.exit(0);
                } else if ("ban".equals(action)) {
                    banUser(target);
                } else if ("restrict".equals(action)) {
                    restrictUser(target);
                }
            }
        }

        private void sendMessage(Session session, String message) {
            try {
                session.getRemote().sendString(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void broadcast(String sender, String message) {
            getSessions().values().forEach(s -> sendMessage(s, sender + ": " + message));
        }
    }
}
