package com.artur114.bytecodegrab.net;

public interface IMessageListener {
    void onMessage(AgentConnection agent, String message);
}
