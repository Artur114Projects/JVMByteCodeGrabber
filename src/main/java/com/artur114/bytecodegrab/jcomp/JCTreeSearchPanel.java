package com.artur114.bytecodegrab.jcomp;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class JCTreeSearchPanel extends JPanel implements DocumentListener {
    private final JTextField search;
    private final JClassTree tree;

    public JCTreeSearchPanel(JClassTree tree) {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        JTextField search = new JTextField();
        search.setFont(search.getFont().deriveFont(11f));
        search.getDocument().addDocumentListener(this);
        search.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1), BorderFactory.createLineBorder(Color.LIGHT_GRAY)));
        this.add(Box.createHorizontalStrut(4));
        JLabel label = new JLabel("Search: ");
        label.setFont(label.getFont().deriveFont(11f));
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 1, 0));
        this.add(label);
        this.add(search);
        this.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
        this.search = search;
        this.tree = tree;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        this.performSearch();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        this.performSearch();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        this.performSearch();
    }

    private void performSearch() {
        String query = this.search.getText().trim();
        tree.searchBy(query);
    }
}
