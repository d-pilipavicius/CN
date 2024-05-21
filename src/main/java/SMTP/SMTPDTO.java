package SMTP;

import java.io.File;

public class SMTPDTO {
    public String sender;
    public String[] receivers;
    public String subject;
    public String data;
    public File[] attachments;
}
