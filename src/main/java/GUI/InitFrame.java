package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class InitFrame extends JFrame {
    private JTextField subject = new JTextField();
    private JTextField recipients = new JTextField();
    private JTextArea text = new JTextArea();
    private JButton send = new JButton("Send");
    private JButton addAttachment = new JButton("\nAdd attachment\n");
    private JPanel attachm = new JPanel();
    private List<File> addedAttachments = new ArrayList<>();
    private JCheckBox encrypt = new JCheckBox("Encrypt text");
    private String encryptionKey = null;
    public InitFrame() {
        JPanel panel = new JPanel();
        JPanel recip = new JPanel();
        JPanel sub = new JPanel();
        JPanel attach = new JPanel();

        JScrollPane pane = new JScrollPane();
        JScrollPane attachPane = new JScrollPane();

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        recip.setLayout(new BoxLayout(recip, BoxLayout.X_AXIS));
        sub.setLayout(new BoxLayout(sub, BoxLayout.X_AXIS));
        attach.setLayout(new BoxLayout(attach, BoxLayout.X_AXIS));

        recip.setMaximumSize(new Dimension(99999, 30));
        sub.setMaximumSize(new Dimension(99999, 30));
        attach.setMaximumSize(new Dimension(99999, 70));

        recip.add(new JLabel("To: "));
        recip.add(recipients);
        recip.add(encrypt);
        recip.add(send);
        sub.add(new JLabel("Subject: "));
        sub.add(subject);
        attach.add(addAttachment);
        attach.add(attachPane);
        attachPane.setViewportView(attachm);
        pane.setViewportView(text);

        panel.add(recip);
        panel.add(sub);
        panel.add(attach);
        panel.add(pane);
        add(panel);

        setName("SMTP Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);
        setVisible(true);
        setEditableFields(false);
    }

    public String getSubject() {
        return subject.getText();
    }

    public String getRecipients() {
        return recipients.getText();
    }

    public String getText() {
        return text.getText();
    }
    public void addUponPress(ActionListener l) {
        send.addActionListener(l);
    }
    public void addUponAttachmentPress(ActionListener l) {
        addAttachment.addActionListener(l);
    }
    public void addUponCheckBox(boolean isTicked, ActionListener l) {
        encrypt.addActionListener(e -> {
            if(isTicked == encrypt.isSelected())
                l.actionPerformed(e);
        });
    }
    public void setEditableFields(boolean editable) {
        subject.setEditable(editable);
        recipients.setEditable(editable);
        text.setEditable(editable);
        send.setEnabled(editable);
        addAttachment.setEnabled(editable);
    }
    public void clearBoxes() {
        subject.setText("");
        recipients.setText("");
        text.setText("");
        addedAttachments.clear();
        attachm.removeAll();
        attachm.revalidate();
        attachm.repaint();
    }
    public void addAttachment(File file) {
        for(File oneFile : addedAttachments) {
            if(oneFile.equals(file))
                return;
        }
        addedAttachments.add(file);
        attachm.add(new InputChip(file.getName(), e->{
            removeAttachment(file);
        }));
        attachm.revalidate();
    }
    public void removeAttachment(File file) {
        addedAttachments.removeIf(x->x.equals(file));
    }
    public File[] getFiles() {
        return addedAttachments.toArray(new File[0]);
    }
    public void setKey(String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }
    public String getKey() {
        return encryptionKey;
    }
    public void setCheck(boolean isSet) {
        encrypt.setSelected(isSet);
    }
}