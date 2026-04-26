package com.artur114.bytecodegrab.view;

import com.artur114.bytecodegrab.model.ConnectModel;
import com.artur114.bytecodegrab.util.Theme;
import com.sun.tools.attach.VirtualMachineDescriptor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ConnectPanel extends JPanel {
    private VirtualMachineDescriptor jvm;
    private JProgressBar connectingBar;
    private JLabel connectingTitle;
    private JButton buttonConnect;
    private JButton buttonAbort;
    private JLabel jvmTitle;

    public ConnectPanel() {
        CardLayout cardLayout = new CardLayout();
        this.setLayout(cardLayout);

        this.add(this.createConnectPanel(), "connect");
        this.add(this.createConnectingPanel(), "connecting");
        cardLayout.show(this, "connect");

        ConnectModel.IConnectState wait = new ConnectModel.IConnectState() {
            @Override
            public String stage() {
                return "wait";
            }

            @Override
            public int percent() {
                return -1;
            }
        };

        this.addConnectActionListener((e) -> cardLayout.show(this, "connecting"));
        this.addAbortActionListener((e) -> {
            cardLayout.show(this, "connect");
            this.setConnectingState(wait);
        });

        this.setConnectingState(wait);
    }

    public VirtualMachineDescriptor jvm() {
        return this.jvm;
    }

    public void disconnect() {
        this.connectingBar.setIndeterminate(true);
        this.connectingBar.setValue(0);
        this.connectingTitle.setText("");
        ((CardLayout) this.getLayout()).show(this, "connect");
    }

    public void addConnectActionListener(ActionListener listener) {
        this.buttonConnect.addActionListener(listener);
    }

    public void addAbortActionListener(ActionListener listener) {
        this.buttonAbort.addActionListener(listener);
    }

    public void setConnectingState(ConnectModel.IConnectState state) {
        this.connectingTitle.setText("Connecting: " + state.stage());

        if (state.percent() == -1) {
            this.connectingBar.setIndeterminate(true);
        } else {
            this.connectingBar.setIndeterminate(false);
            this.connectingBar.setValue(state.percent());
        }
    }

    public void setJvm(VirtualMachineDescriptor jvm) {
        this.jvm = jvm;

        if (jvm != null) {
            this.jvmTitle.setText(String.format("JVM: %s (pid %s)", jvm.displayName(), jvm.id()));
        } else {
            this.jvmTitle.setText(String.format("JVM: %s (pid %s)", "noop", "0000"));
        }
    }

    private JPanel createConnectPanel() {
        JPanel panelConnect = new JPanel();
        panelConnect.setLayout(new BoxLayout(panelConnect, BoxLayout.Y_AXIS));

        panelConnect.add(Box.createVerticalGlue());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(Box.createHorizontalGlue());
        JLabel label = new JLabel();
        Theme.jvmLabelColor().ifPresent(label::setForeground);
        label.setFont(label.getFont().deriveFont(18f));
        panel.add(label);
        panel.add(Box.createHorizontalGlue());

        JPanel panel1 = new JPanel();
        panel1.setLayout(new BoxLayout(panel1, BoxLayout.X_AXIS));
        panel1.add(Box.createHorizontalGlue());
        JButton button = new JButton("connect");
        button.setFocusable(false);
        panel1.add(button);
        panel1.add(Box.createHorizontalGlue());

        panelConnect.add(panel);
        panelConnect.add(Box.createVerticalStrut(8));
        panelConnect.add(panel1);

        panelConnect.add(Box.createVerticalStrut(20));

        panelConnect.add(Box.createVerticalGlue());


        this.buttonConnect = button;
        this.jvmTitle = label;

        return panelConnect;
    }

    private JPanel createConnectingPanel() {
        JPanel panelConnecting = new JPanel();
        panelConnecting.setLayout(new BoxLayout(panelConnecting, BoxLayout.Y_AXIS));

        panelConnecting.add(Box.createVerticalGlue());

        JPanel panelLabel = new JPanel();
        panelLabel.setLayout(new BoxLayout(panelLabel, BoxLayout.X_AXIS));
        panelLabel.add(Box.createHorizontalGlue());
        JLabel label = new JLabel();
        Theme.jvmLabelColor().ifPresent(label::setForeground);
        label.setFont(label.getFont().deriveFont(18f));
        panelLabel.add(label);
        panelLabel.add(Box.createHorizontalGlue());

        JPanel panelProgress = new JPanel();
        panelProgress.setLayout(new BoxLayout(panelProgress, BoxLayout.X_AXIS));
        panelProgress.add(Box.createHorizontalGlue());
        JProgressBar bar = new JProgressBar(0, 4);
        panelProgress.add(bar);
        panelProgress.add(Box.createHorizontalGlue());

        JPanel panelButton = new JPanel();
        panelButton.setLayout(new BoxLayout(panelButton, BoxLayout.X_AXIS));
        panelButton.add(Box.createHorizontalGlue());
        JButton button = new JButton("abort");
        button.setFocusable(false);
        panelButton.add(button);
        panelButton.add(Box.createHorizontalGlue());

        panelConnecting.add(panelLabel);
        panelConnecting.add(Box.createVerticalStrut(8));
        panelConnecting.add(panelProgress);
        panelConnecting.add(Box.createVerticalStrut(8));
        panelConnecting.add(panelButton);

        panelConnecting.add(Box.createVerticalStrut(20));

        panelConnecting.add(Box.createVerticalGlue());

        this.connectingTitle = label;
        this.buttonAbort = button;
        this.connectingBar = bar;

        return panelConnecting;
    }
}
