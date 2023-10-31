package com.holo.util;

import java.io.IOException;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.Socket;

public class Talker {
    private DataOutputStream writeStream;
    private BufferedReader readStream;

    public Talker(Socket client) throws IOException {
        writeStream = new DataOutputStream(client.getOutputStream());
        readStream = new BufferedReader(new InputStreamReader(client.getInputStream()));
    }

    public void send(String arg) throws IOException {
        System.out.println("SEND:" + arg); // TODO: Remove after debug
        writeStream.writeUTF(arg+"\n");
        writeStream.flush();
    }

    public String recieve() throws IOException {
        clear();
        clear();
        String tmp = readStream.readLine();
        System.out.println("RECIEVE: " + tmp);
        return new String(tmp); // TODO: Set back to readStream.readAllBytes after debug
    }

    private void clear() throws IOException {
        readStream.read();
    }
}
