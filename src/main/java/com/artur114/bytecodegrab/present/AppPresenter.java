package com.artur114.bytecodegrab.present;

import com.artur114.bytecodegrab.jcomp.JCardContainer;
import com.artur114.bytecodegrab.jcomp.JPanelEmpty;
import com.artur114.bytecodegrab.main.Application;
import com.artur114.bytecodegrab.model.CodeGrabModel;
import com.artur114.bytecodegrab.model.ConnectModel;
import com.artur114.bytecodegrab.model.JvmListModel;
import com.artur114.bytecodegrab.net.ServerSocketThread;
import com.artur114.bytecodegrab.view.CodeGrabPanel;
import com.artur114.bytecodegrab.view.ConnectPanel;
import com.artur114.bytecodegrab.view.JvmListPanel;
import com.sun.tools.attach.VirtualMachineDescriptor;

import javax.swing.*;
import java.awt.*;

public class AppPresenter {
    private static AppPresenter presenter;

    public static void init() {
        presenter = new AppPresenter();
    }

    public static AppPresenter presenter() {
        return presenter;
    }

    public CodeGrabModel codeGrabModel;
    public CodeGrabPanel codeGrabView;
    public ConnectModel connectModel;
    public ConnectPanel connectView;
    public JvmListModel jvmListModel;
    public JvmListPanel jvmListView;

    public void init(Application appView) {
        this.codeGrabModel = new CodeGrabModel();
        this.codeGrabView = new CodeGrabPanel();
        this.connectModel = new ConnectModel();
        this.connectView = new ConnectPanel();
        this.jvmListView = new JvmListPanel();
        this.jvmListModel = new JvmListModel();

        JSplitPane appPane = appView.baseSplitPane();

        appPane.setLeftComponent(this.jvmListView);
        appPane.setDividerLocation(190);

        this.jvmListView.setProvider((callBack -> this.jvmListModel.scanJvmsAsync(callBack)));
        this.jvmListView.refreshJvmList();

        CardLayout card = new CardLayout();
        JPanel rightPanel = new JPanel(card);
        JCardContainer rightCard = new JCardContainer(rightPanel, card);
        appPane.setRightComponent(rightPanel);

        rightPanel.add(new JPanelEmpty(), "empty");
        rightPanel.add(this.connectView, "connect");
        rightPanel.add(this.codeGrabView, "tree");

        this.jvmListView.addSelectionListener((e) -> {
            VirtualMachineDescriptor vm = this.jvmListView.getSelectedJvm();
            this.connectView.setJvm(vm);

            if (vm != null) {
                rightCard.show("connect");
            } else {
                rightCard.show("empty");
            }
        });
        this.connectView.addConnectActionListener((e) -> {
            this.connectModel.connect(this.connectView.jvm().id());
        });
        this.connectView.addAbortActionListener((e) -> {
            this.connectModel.abort();
        });
        this.connectModel.setStateListener((state -> SwingUtilities.invokeLater(() -> {
            this.connectView.setConnectingState(state);
        })));
        this.connectModel.addConnectListener(() -> {
            codeGrabView.showProgressBar(true);
            codeGrabModel.requestClassPath();
            Timer timer = new Timer(100, (e -> {
                rightCard.show("tree");
                jvmListView.setDisable(true);
            }));
            timer.setRepeats(false);
            timer.start();
        });
        this.codeGrabView.addDisconnectListener(e -> {
            rightCard.show("connect");
            jvmListView.setDisable(false);
            connectView.disconnect();
            connectModel.disconnect();
        });
        this.codeGrabView.addRefreshListener(e -> {
            codeGrabView.showProgressBar(true);
            codeGrabModel.requestClassPath();
        });
        this.codeGrabModel.addCRequestProcessListener(chunks -> {
            codeGrabView.setProgress(chunks);
        });
        this.codeGrabModel.addCRequestDoneListener(ret -> {
            codeGrabView.loadClasses(ret);
        });
        this.codeGrabView.addGrabListener((data) -> {
            codeGrabModel.grabClasses(this.codeGrabView.getClassesToGrab(), data);
        });
        this.codeGrabModel.addBCRequestProcessListener(ret -> {
            codeGrabView.setGrabProgress(ret.percent());
            codeGrabView.setGrabState(ret.state());
        });
        this.codeGrabModel.addBCRequestDoneListener(ret -> {
            codeGrabView.grabDone();
        });
    }
}
