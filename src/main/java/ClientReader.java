import java.io.IOException;

public class ClientReader implements Runnable {
    WebSocketServer webSocketServer;
    Client client;

    public ClientReader(WebSocketServer webSocketServer, Client client) {
        this.webSocketServer = webSocketServer;
        this.client = client;
    }

    @Override
    public void run() {
        int len = 0;
        byte[] b = new byte[1024];
        try {
            while (true) {
                len = client.getInputStream().read(b);
                if (isClientClosing(b)) {
                    this.webSocketServer.onClose(client);
                    this.client.close();
                    return;
                }
                if (len != -1) {
                    byte[] message = WSDecoder.decode(len, b);
                    this.webSocketServer.onMessage(this.client, new String(message));
                    b = new byte[1024];
                }
            }
        } catch (IOException e) {
            System.out.println("IO Exception" + e.getMessage());
        }
    }

    private boolean isClientClosing(byte[] b) {
        int firstFrame = b[0] & 8;
        return firstFrame == 8;
    }
}
