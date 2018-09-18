package vn.drjoy.server;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Created by QuangNV on 23/02/2018.
 */
@SpringBootApplication
public class Server extends WebSocketServer {
    private final String CREATE = "GETROOM";
    private final String JOIN = "ENTERROOM";
    private final String ERROR = "WRONGROOM";
    private static Map<Integer,Set<WebSocket>> Rooms = new HashMap<Integer, Set<WebSocket>>();
    private int roomId;

    public Server() {
        super(new InetSocketAddress(8080));
    }

    /**
     *  When connection be opened from client
     * @param webSocket
     * @param clientHandshake
     */
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        System.out.println("New client connected: " +
                webSocket.getRemoteSocketAddress() + " hash " +
                webSocket.getRemoteSocketAddress().hashCode());
    }

    /**
     *  When connection be closed from client
     * @param webSocket  : connection
     * @param code  : code of connection
     * @param reason  : reason of close
     * @param remote :
     */
    public void onClose(WebSocket webSocket, int code, String reason, boolean remote) {
        System.out.println("Client disconnected  : " + reason);
    }

    /**
     *  While client sent message to server
     * @param webSocket  : connection
     * @param message  : sent message from client
     */
    public void onMessage(WebSocket webSocket, String message) {
        Set<WebSocket> session ;
        try{
            JSONObject object = new JSONObject(message);
            String msgType = object.getString("type");
            if (msgType.equals(CREATE)) {
                roomId = generateRoomNumber();
                session = new HashSet<WebSocket>();
                session.add(webSocket);
                Rooms.put(roomId,session);
                System.out.println("Created room : " + roomId);
                // Sending room id to client
                webSocket.send("{\"type\":\""+ CREATE + "\",\"value\":" + roomId +"}");

            } else if (msgType.equals(JOIN)) {
                roomId = object.getInt("value");
                System.out.println("New client joined room " + roomId);
                session = Rooms.get(roomId);
                if (session != null) {
                    session.add(webSocket);
                    Rooms.put(roomId, session);
                }else {
                    webSocket.send("{\"type\":\""+ ERROR + "\",\"value\":" + roomId +"}");
                }
            } else {
                sendToAll(webSocket, message);
            }

        }catch (JSONException e){
            sendToAll(webSocket,message);
        }
    }

    public void onError(WebSocket webSocket, Exception e) {
        System.err.println("Error happened: " + e);
    }

    private int generateRoomNumber() {
        return new Random(System.currentTimeMillis()).nextInt();
    }

    private void sendToAll(WebSocket conn, String message) {
        for (WebSocket c : Rooms.get(roomId)) {
            if (c != conn) c.send(message);
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }
}
