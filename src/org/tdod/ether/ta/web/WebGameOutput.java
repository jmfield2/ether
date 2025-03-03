package org.tdod.ether.ta.web;

import org.eclipse.jetty.websocket.api.Session;
import org.tdod.ether.ta.output.GameOutput;
import org.tdod.ether.taimpl.websocket.WebSocketTaShell;

import java.io.IOException;

public class WebGameOutput implements GameOutput {

    private final Session session;

    public WebGameOutput(WebSocketTaShell shell) {
        this.session = shell.getSession();
    }

    @Override
    public void println(String s) {
        try {
            session.getRemote().sendString(s + "\r\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void print(String s) {
        try {
            session.getRemote().sendString(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void printlnWithoutColor(String s) {
        this.println(s);
    }

    @Override
    public void printWithoutColor(String s) {
        this.print(s);
    }
}
