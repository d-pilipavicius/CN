import GUI.InitFrame;
import SMTP.SMTPClient;
import SMTP.SMTPDTO;
import SMTP.SMTPFormatter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class Main {
    private static final String emailRegex = "[a-zA-Z0-9.]+@[a-zA-Z0-9]+.[a-zA-Z0-9]+";

    public static void main(String[] args) throws IOException {
        //Window
        InitFrame frame = new InitFrame();

        //server used for server declaration
        String ip = "smtp.gmail.com";
        int port = 465;

        final String username = "your gmail"; //JOptionPane.showInputDialog(frame, "Enter your email", "Login", JOptionPane.PLAIN_MESSAGE);
        final String password = "your password"; //JOptionPane.showInputDialog(frame, "Enter your app password", "Login", JOptionPane.PLAIN_MESSAGE);

        //Opening port and declaring formatter
        SMTPClient client = new SMTPClient(ip, port);
        SMTPFormatter formatter = new SMTPFormatter(client);

        formatter.identify("localhost");

        if(!ip.isBlank() && port != 0) {
            frame.setEditableFields(true);
        }

        frame.addUponPress(e -> {
            if(!frame.getRecipients().isBlank() && !frame.getText().isBlank()) {
                if(client.isConnected()) {
                    SMTPDTO dto = new SMTPDTO();
                    dto.sender = username;
                    dto.subject = frame.getSubject();
                    dto.receivers = SMTPFormatter.formatUsers(frame.getRecipients());
                    if(frame.getKey() == null || frame.getKey().isBlank())
                        dto.data = SMTPFormatter.formatBadMessage(frame.getText());
                    else
                        dto.data = SMTPFormatter.encode(frame.getKey(), SMTPFormatter.formatBadMessage(frame.getText()));
                    dto.attachments = frame.getFiles();

                    for(String receiver : dto.receivers) {
                        if(!Pattern.matches(emailRegex, receiver))
                            return;
                    }

                    try {
                        try {
                            formatter.setSenderUser(dto.sender);
                            formatter.setReceiverUsers(dto.receivers);
                            if(frame.getKey() == null || frame.getKey().isBlank())
                                formatter.sendDataWithAttachment(dto);
                            else
                                formatter.sendDataAsEncrypted(dto);
                        } catch(IOException exc) {
                            formatter.reconnectAndSend(dto,username,password);
                        }
                    } catch(IOException ex) {
                        ex.printStackTrace();
                        System.exit(1);
                    }
                    frame.clearBoxes();
                }
            }
        });
        frame.addUponAttachmentPress(e -> {
            FileDialog dialog = new FileDialog(frame, "Select files");
            dialog.setVisible(true);

            for(File file : dialog.getFiles())
                frame.addAttachment(file);
        });
        frame.addUponCheckBox(true, e ->{
            String key = JOptionPane.showInputDialog(frame, "Please enter the message encryption key", "Set key", JOptionPane.PLAIN_MESSAGE);
            if(key == null || key.isBlank())
                frame.setCheck(false);
            else
                frame.setKey(key);
        });
        frame.addUponCheckBox(false, e-> {
            frame.setKey(null);
        });
    }
}
