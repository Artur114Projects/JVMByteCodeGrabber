package com.artur114.bytecodegrab.agent;

import java.io.*;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketImpl;

public class SocketA extends Socket {
    public final DataOutputStream out;
    public final DataInputStream in;

    public SocketA(String host, int port) throws IOException {
        super(host, port);
        this.in = new DataInputStream(this.getInputStream());
        this.out = new DataOutputStream(this.getOutputStream());
    }

    public void send(String string) throws IOException {
        this.out.writeUTF(string);
        this.out.flush();
    }

    public void send(byte[] bytes) throws IOException {
        this.out.writeInt(bytes.length);
        this.out.write(bytes);
        this.out.flush();
    }

    public String waitForMessage() throws IOException {
        return this.in.readUTF();
    }
}
