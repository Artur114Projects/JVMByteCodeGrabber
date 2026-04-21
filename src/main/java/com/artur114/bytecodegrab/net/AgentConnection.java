package com.artur114.bytecodegrab.net;

import java.io.*;
import java.net.Socket;

public class AgentConnection {
    public final DataInputStream in;
    public final DataOutputStream out;
    public final Socket connection;

    public AgentConnection(Socket socket) throws IOException {
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        this.connection = socket;
    }

    public void disconnect() throws IOException {
        this.connection.close();
        this.out.close();
        this.in.close();
    }

    public void send(String string) throws IOException {
        this.out.writeUTF(string);
        this.out.flush();
    }

    public byte[] receiveBytes() throws IOException {
        int length = this.in.readInt();
        byte[] data = new byte[length];
        this.in.readFully(data);
        return data;
    }

    public String waitForMessage() throws IOException {
        return this.in.readUTF();
    }

    @Override
    public String toString() {
        return this.connection.toString();
    }
}
