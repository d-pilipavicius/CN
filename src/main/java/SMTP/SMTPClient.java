package SMTP;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;

public class SMTPClient {
    private SSLSocket sslSocket;
    private final String ip;
    private final int port;
    private String lastSentMessage;
    private String lastReceivedMessage;
    private BufferedWriter writer;
    private BufferedReader reader;
    public SMTPClient(String ip, int port) throws IOException {
        this.ip = ip;
        this.port = port;
        connect();
    }
    public void connect() throws IOException {
        SSLSocketFactory sslfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        sslSocket = (SSLSocket) sslfactory.createSocket(ip, port);
        reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(sslSocket.getOutputStream()));
    }

    public void close() throws IOException {
        if(sslSocket != null)
            sslSocket.close();
    }
    public void sendMessage(String message) throws IOException {
        writer.write(message);
        writer.flush();
        lastSentMessage = message;
    }
    public String receiveMessage() throws IOException {
        lastReceivedMessage = reader.readLine();
        return lastReceivedMessage;
    }
    public boolean isConnected() {
        return sslSocket.isConnected();
    }
    public void write(String text) throws IOException {
        writer.write(text);
    }
    public void flush() throws IOException {
        writer.flush();
    }
    public String getLastSentMessage() {
        return lastSentMessage;
    }
    public String getLastReceivedMessage() {
        return lastReceivedMessage;
    }
}
