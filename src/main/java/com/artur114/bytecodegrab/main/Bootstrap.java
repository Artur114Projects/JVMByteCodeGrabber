package com.artur114.bytecodegrab.main;

import com.artur114.bytecodegrab.jcomp.JLoadingFrame;
import com.artur114.bytecodegrab.present.AppPresenter;

import javax.swing.*;

public class Bootstrap {
    protected static void init() {
        JLoadingFrame loading = new JLoadingFrame();
        loading.setVisible(true);

        SwingUtilities.invokeLater(() -> {

            Application.init();
            AppPresenter.init();

            AppPresenter.presenter().init(Application.application());

            loading.dispose();
            Application.application().setVisible(true);
        });
    }
}
