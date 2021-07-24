import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class WebSocketServer extends Thread {
    private final int port;
    private final Executor executor = Executors.newCachedThreadPool();

    public WebSocketServer(int port) {
        this.port = port;
    }

    public abstract void onOpen(Client client);

    public abstract void onClose(Client client);

    public abstract void onMessage(Client client, String message);

    public abstract void onError(Throwable e);

    @Override
    public void run() {
        ServerSocket server;
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            this.onError(e);
            return;
        }

        while (true) {
            try {
                Socket clientSocket = server.accept(); //waits until a client connects

                InputStream inputStream = clientSocket.getInputStream();
                OutputStream outputStream = clientSocket.getOutputStream();
                WSHandshaker.doHandShakeToInitializeWebSocketConnection(inputStream, outputStream);

                String clientId = createClientId(clientSocket);
                Client client = new Client(clientId, outputStream, inputStream);
                this.onOpen(client);

                ClientReader clientReader = new ClientReader(this, client);
                executor.execute(clientReader);
            } catch (IOException | NoSuchAlgorithmException e) {
                // Could not wait for client connection
                e.printStackTrace();
                this.onError(e);
            }
        }
    }

    private String createClientId(Socket clientSocket) throws NoSuchAlgorithmException {
        InetAddress clientIP = clientSocket.getInetAddress();
        String clientId = clientIP.getHostAddress() + clientSocket.getPort();
        byte[] clientIdDigest = Base64.getEncoder().encode(clientId.getBytes());
        return new String(clientIdDigest);
    }
}
