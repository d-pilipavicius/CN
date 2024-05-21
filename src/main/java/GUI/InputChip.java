package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class InputChip extends JButton {
    public InputChip(String text, ActionListener uponExit) {
        super(text);
        setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        setBackground(Color.lightGray);
        addActionListener(e -> {
            Container parent = getParent();
            parent.remove(InputChip.this);
            parent.revalidate();
            parent.repaint();
            uponExit.actionPerformed(e);
        });
    }
}