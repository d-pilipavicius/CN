package SMTP;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;

public class SMTPFormatter {
    private final SMTPClient client;
    private static final boolean printMessages = false;
    private static final String newLine = "\r\n";
    public SMTPFormatter(SMTPClient client) {
        this.client = client;
    }
    public void identify(String domainName) throws IOException {
        client.sendMessage("EHLO "+domainName+newLine);
        acceptMessages(printMessages);
    }
    public void setSenderUser(String senderEmail) throws IOException {
        client.sendMessage("MAIL FROM:<"+senderEmail+">"+newLine);
        acceptMessages(printMessages);
    }
    public void setReceiverUsers(String[] users) throws IOException {
        for(String user : users) {
            client.sendMessage("RCPT TO:<"+user+">"+newLine);
            acceptMessages(printMessages);
        }
    }
    public void sendDataWithAttachment(SMTPDTO dto) throws IOException {
        client.sendMessage("DATA"+newLine);
        acceptMessages(printMessages);
        client.write("Subject: "+dto.subject+newLine+
                "From: "+dto.sender+newLine);
        client.write("To: ");
        for(int i = 0; i < dto.receivers.length; ++i) {
            client.write(dto.receivers[i]);
            if(i != dto.receivers.length-1)
                client.write(", ");
            else
                client.write(newLine);
        }
        client.write(
                "MIME-Version: 1.0"+newLine+
                "Content-Type: multipart/mixed; boundary = \"BOUNDARY\""+newLine);
        client.write(
                "--BOUNDARY"+newLine+
                "Content-Type: text/plain; charset=\"UTF-8\""+newLine);
        client.write(formatBadMessage(dto.data)+newLine);
        HashMap<String, Integer> fileNames = new HashMap<>();
        for(File file : dto.attachments) {
            String name = file.getName();
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            if(fileBytes.length > 1024*1024*40) {
                System.out.println("File too big, not uploaded.");
            }

            if(!fileNames.containsKey(name))
                fileNames.put(name, 1);
            else {
                name = name.substring(0, name.lastIndexOf('.'))+" ("+fileNames.get(name)+")"+name.substring(name.lastIndexOf('.'));
                fileNames.replace(file.getName(), fileNames.get(file.getName())+1);
            }
            client.write(
                    "--BOUNDARY"+newLine+
                            "Content-Type: application/octet-stream"+newLine+
                            "Content-Transfer-Encoding: base64"+newLine+
                            "Content-Disposition: attachment; filename=\""+name+"\""+newLine);
            client.write(Base64.getEncoder().encodeToString(fileBytes)+newLine);
        }
        client.write("--BOUNDARY--"+newLine);
        client.flush();
        client.sendMessage(newLine+"."+newLine);
        acceptMessages(printMessages);
    }
    public void sendDataAsEncrypted(SMTPDTO dto) throws IOException {
        client.sendMessage("DATA"+newLine);
        acceptMessages(printMessages);
        client.write("Subject: "+dto.subject+newLine+
                "From: "+dto.sender+newLine);
        client.write("To: ");
        for(int i = 0; i < dto.receivers.length; ++i) {
            client.write(dto.receivers[i]);
            if(i != dto.receivers.length-1)
                client.write(", ");
            else
                client.write(newLine);
        }
        client.write(
                "MIME-Version: 1.0"+newLine+
                     "Content-Type: multipart/mixed; boundary = \"BOUNDARY\""+newLine);
        HashMap<String, Integer> fileNames = new HashMap<>();
        client.write(
                "--BOUNDARY"+newLine+
                        "Content-Type: application/octet-stream"+newLine+
                        "Content-Transfer-Encoding: base64"+newLine+
                        "Content-Disposition: attachment; filename=\"encrypted.txt\""+newLine);
        client.write(Base64.getEncoder().encodeToString(dto.data.getBytes())+newLine);
        fileNames.put("encrypted.txt",1);
        for(File file : dto.attachments) {
            String name = file.getName();
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            if(fileBytes.length > 1024*1024*40) {
                System.out.println("File too big, not uploaded.");
            }

            if(!fileNames.containsKey(name))
                fileNames.put(name, 1);
            else {
                name = name.substring(0, name.lastIndexOf('.'))+" ("+fileNames.get(name)+")"+name.substring(name.lastIndexOf('.'));
                fileNames.replace(file.getName(), fileNames.get(file.getName())+1);
            }
            client.write(
                    "--BOUNDARY"+newLine+
                            "Content-Type: application/octet-stream"+newLine+
                            "Content-Transfer-Encoding: base64"+newLine+
                            "Content-Disposition: attachment; filename=\""+name+"\""+newLine);
            client.write(Base64.getEncoder().encodeToString(fileBytes)+newLine);
        }
        client.write("--BOUNDARY--"+newLine);
        client.flush();
        client.sendMessage(newLine+"."+newLine);
        acceptMessages(printMessages);
    }
    public static String[] formatUsers(String usersUnformatted) {
        String[] users = usersUnformatted.split("[,]+");
        for(int i = 0; i < users.length; ++i)
            users[i] = users[i].strip();
        return users;
    }
    public static String formatBadMessage(String data) {
        data = data.replace("\n",newLine);
        data = data.replace(newLine+"."+newLine,newLine+".."+newLine);
        return data;
    }
    public void reconnectAndSend(SMTPDTO message, String username, String password) throws IOException {
        client.connect();
        acceptMessages(printMessages);
        identify("localhost");
        loginSSL(username,password);
        setSenderUser(message.sender);
        setReceiverUsers(message.receivers);
        sendDataWithAttachment(message);
    }

    public void loginSSL(String username, String password) throws IOException {
        client.sendMessage("AUTH LOGIN"+newLine);
        acceptMessages(printMessages);
        client.sendMessage(Base64.getEncoder().encodeToString(username.getBytes())+newLine);
        acceptMessages(printMessages);
        client.sendMessage(Base64.getEncoder().encodeToString(password.getBytes())+newLine);
        acceptMessages(printMessages);
    }
    public static String keyCoder(String key, String text) {
        char[] result = null;
        try {
            char[] keyByte = key.toCharArray();
            char[] textByte = text.toCharArray();
            result = new char[textByte.length];

            for(int i = 0; i < textByte.length; ++i) {
                result[i] = (char) (textByte[i] ^ keyByte[i%keyByte.length]);
            }
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
        return String.copyValueOf(result);
    }

    public static String encode(String key, String input) {
        return Base64.getEncoder().encodeToString(keyCoder(key,input).getBytes());
    }
    public static String decode(String key, String input) {
        return keyCoder(key,new String(Base64.getDecoder().decode(input)));
    }
    public void acceptMessages(boolean printMessage) throws IOException {
        String temp = null;
        try {
            while((temp = client.receiveMessage()).charAt(3) == '-') {
                if(printMessage)
                    System.out.println(temp);
            }
        } catch(IndexOutOfBoundsException ex) {
            if(temp == null)
                throw new IOException("SOCKET DISCONNECTED");
        }
        if(printMessage)
            System.out.println(temp);
    }

}
