package org.tdod.ether.taimpl.websocket;

import com.meyling.telnet.shell.ShellIo;
import net.wimpi.telnetd.net.Connection;
import net.wimpi.telnetd.net.ConnectionEvent;
import org.eclipse.jetty.websocket.api.Session;
import org.tdod.ether.ta.telnet.TaShell;

import java.net.InetSocketAddress;

public class WebSocketTaShell implements TaShell {

    final private Session session;

    public WebSocketTaShell(Session s) {
        this.session = s;
    }

    public Session getSession() {
        return this.session;
    }

    @Override
    public String getConnectionHostname() {
        return ((InetSocketAddress) session.getRemoteAddress()).getHostName();
    }

    @Override
    public int getConnectionPort() {
        return ((InetSocketAddress) session.getRemoteAddress()).getPort();
    }

    @Override
    public long getConnectionId() {
        return this.getConnectionPort();
    }

    @Override
    public ShellIo getShellIo() {
        return null;
    }

    @Override
    public Connection getConnection() {
        return null;
    }

    @Override
    public void cleanup(String info) {

    }

    @Override
    public void setHideInput(boolean hideInput) {

    }

    @Override
    public void run(Connection con) {

    }

    @Override
    public void connectionIdle(ConnectionEvent ce) {

    }

    @Override
    public void connectionTimedOut(ConnectionEvent ce) {

    }

    @Override
    public void connectionLogoutRequest(ConnectionEvent ce) {

    }

    @Override
    public void connectionSentBreak(ConnectionEvent ce) {

    }
}
