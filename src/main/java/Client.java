import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Client {
    private final String id;
    private final OutputStream outputStream;
    private final InputStream inputStream;

    public Client(String id, OutputStream outputStream, InputStream inputStream) {
        this.id = id;
        this.outputStream = outputStream;
        this.inputStream = inputStream;
    }

    public String getId() {
        return this.id;
    }

    protected InputStream getInputStream() {
        return this.inputStream;
    }

    public void close() throws IOException {
        this.inputStream.close();
        this.outputStream.close();
    }

    public void send(String message) throws IOException {
        this.outputStream.write(WSEncoder.encode(message));
    }
}
