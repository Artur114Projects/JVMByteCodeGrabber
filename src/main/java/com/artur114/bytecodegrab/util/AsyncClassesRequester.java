package com.artur114.bytecodegrab.util;

import com.artur114.bytecodegrab.net.AgentConnection;
import com.artur114.bytecodegrab.net.ServerSocketThread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AsyncClassesRequester extends SwingWorkerListened<List<String>, Percent> {
    @Override
    protected List<String> doInBackground() throws Exception {
        ServerSocketThread server = ServerSocketThread.server();
        Percent percent = new Percent();

        this.publish(percent.setIndeterminate(true));

        AgentConnection agent = server.agent();

        agent.send("REQUEST_LOADED_CLASSES");
        String reply = agent.waitForMessage();

        List<String> classList;
        int classesCount;

        if (reply.contains("CLASS_LIST_START")) {
            classesCount = StringUtils.intPropFromMessage(reply, "SIZE");
            classList = new ArrayList<>(classesCount);
        } else {
            System.out.println("reply is invalid! reply: " + reply);
            return Collections.emptyList();
        }

        while (true) {
            String message = agent.waitForMessage();

            if (message.equals("CLASS_LIST_END")) {
                return classList;
            }

            classList.add(message);
            this.publish(percent.setPercent(classList.size(), classesCount));
        }
    }
}
