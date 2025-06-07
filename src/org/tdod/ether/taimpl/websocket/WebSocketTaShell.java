package org.tdod.ether.taimpl.websocket;

import com.meyling.telnet.shell.ShellIo;
import net.wimpi.telnetd.net.Connection;
import net.wimpi.telnetd.net.ConnectionEvent;
import org.eclipse.jetty.websocket.api.Session;
import org.tdod.ether.ta.telnet.TaShell;
import org.tdod.ether.taimpl.websocket.WebSocketShellIo; // Added import

import java.io.IOException; // Added import for IOException
import java.net.InetSocketAddress;

public class WebSocketTaShell implements TaShell {

    final private Session session;
    private WebSocketShellIo _shellIo; // Added field

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
        // Consider if port is unique enough, or if another ID mechanism is needed.
        // For now, using port as per existing code.
        return this.getConnectionPort();
    }

    @Override
    public ShellIo getShellIo() {
        if (_shellIo == null) {
            _shellIo = new WebSocketShellIo(this.session);
        }
        return _shellIo;
    }

    // Added close() method
    public void close() {
        try {
            if (_shellIo != null) {
                // Assuming WebSocketShellIo.close() handles session.close()
                _shellIo.close();
            } else if (session != null && session.isOpen()) {
                session.close();
            }
        } catch (IOException e) {
            // In a real application, use a proper logger (e.g., SLF4J)
            System.err.println("Error closing websocket session or shell: " + e.getMessage());
        }
    }

    @Override
    public Connection getConnection() {
        // This method's implementation depends on whether TaShell is used
        // in a context that strictly requires a Telnet Connection object.
        // For pure WebSocket usage, returning null might be acceptable.
        return null;
    }

    @Override
    public void cleanup(String info) {
        // Implement any specific cleanup logic for WebSocketTaShell if necessary.
        // This might include releasing resources or nullifying objects.
        if (_shellIo != null) {
            try {
                _shellIo.close();
            } catch (IOException e) {
                 System.err.println("Error during cleanup: " + e.getMessage());
            }
            _shellIo = null;
        }
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (IOException e) {
                 System.err.println("Error closing session during cleanup: " + e.getMessage());
            }
        }
    }

    @Override
    public void setHideInput(boolean hideInput) {
        if (session == null || !session.isOpen()) {
            System.err.println("WebSocket session is not open. Cannot set input hide status.");
            return;
        }
        try {
            String command;
            if (hideInput) {
                command = "{\"command\": \"hide_input\"}";
            } else {
                command = "{\"command\": \"show_input\"}";
            }
            session.getBasicRemote().sendText(command);
        } catch (IOException e) {
            // The TaShell interface method does not throw IOException, so handle it here.
            System.err.println("Error sending input hide/show command to websocket session: " + e.getMessage());
            // Depending on project error handling, could wrap in a RuntimeException
            // or use a dedicated logger with more context.
        }
    }

    @Override
    public void run(Connection con) {
        // This method is from the Runnable interface, often used with TelnetD's
        // shell management. Its direct applicability in a WebSocket server context
        // depends on the server's architecture. If the WebSocket events drive
        // the interaction, this method might not be called or used.
    }

    @Override
    public void connectionIdle(ConnectionEvent ce) {
        // Telnet-specific event. May not be directly applicable.
        // Could be mapped to WebSocket idle timeout if such a feature is used.
    }

    @Override
    public void connectionTimedOut(ConnectionEvent ce) {
        // Telnet-specific event.
        // Good practice to close the connection on timeout.
        System.err.println("Connection timed out. Closing session.");
        close();
    }

    @Override
    public void connectionLogoutRequest(ConnectionEvent ce) {
        // Telnet-specific event.
        // Close the connection upon a logout request.
        System.err.println("Logout request received. Closing session.");
        close();
    }

    @Override
    public void connectionSentBreak(ConnectionEvent ce) {
        // Telnet-specific event.
        // Handle as appropriate if a "break" signal has meaning in your application.
    }
}
