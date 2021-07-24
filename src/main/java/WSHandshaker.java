import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WSHandshaker {
    public static void doHandShakeToInitializeWebSocketConnection(InputStream inputStream, OutputStream outputStream) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        String data = new Scanner(inputStream, "UTF-8").useDelimiter("\\r\\n\\r\\n").next();
        String webSocketAcceptanceProof = prepareWebSocketAcceptanceProof(data);
        try {
            String responseHeader = "HTTP/1.1 101 Switching Protocols\r\n"
                    + "Connection: Upgrade\r\n"
                    + "Upgrade: websocket\r\n"
                    + "Sec-WebSocket-Accept: " + webSocketAcceptanceProof
                    + "\r\n\r\n";
            byte[] response = responseHeader.getBytes(StandardCharsets.UTF_8);
            outputStream.write(response, 0, response.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * The server has to take the value of Sec-WebSocket-Key present in the header
     * and concatenate this with the Globally Unique Identifier (GUID) "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
     * Then, a SHA-1 hash of the concatenated string is digested
     * and the digested string is base64-encoded.
     * This base64-encoded string is then returned in the server's handshake.
     */
    private static String prepareWebSocketAcceptanceProof(String data) throws NoSuchAlgorithmException {
        Pattern pattern = Pattern.compile("Sec-WebSocket-Key: (.*)");
        Matcher matcher = pattern.matcher(data);
        // Take the value of Sec-WebSocket-Key present in the header.
        if (matcher.find()) {
            // Concatenate the guid to the Sec-WebSocket-Key.
            String secWebSocketKey = matcher.group(1);
            String guid = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            String secWebSocketProof = secWebSocketKey + guid;

            // Take SHA-1 hash of the concatenated string
            MessageDigest digester = MessageDigest.getInstance("SHA-1");
            byte[] proofBytes = digester.digest(secWebSocketProof.getBytes(StandardCharsets.UTF_8));

            // Base64 encode the digest
            byte[] bytes = Base64.getEncoder().encode(proofBytes);
            return new String(bytes);
        } else {
            return null;
        }
    }
}
