package org.tdod.ether.taimpl.websocket;

import com.meyling.telnet.shell.ShellIo;
import javax.websocket.Session;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WebSocketShellIo implements ShellIo {

    private final Session session;
    private final Queue<Character> buffer = new ConcurrentLinkedQueue<>();

    public WebSocketShellIo(Session session) {
        this.session = session;
        // Register a message handler to populate the buffer
        session.addMessageHandler(String.class, message -> {
            for (char c : message.toCharArray()) {
                buffer.add(c);
            }
        });
    }

    @Override
    public int read() throws IOException {
        if (!session.isOpen()) {
            throw new IOException("Session is closed");
        }
        // Busy-wait until data is available
        while (buffer.isEmpty()) {
            if (!session.isOpen()) {
                throw new IOException("Session is closed during read");
            }
            try {
                // Sleep for a short duration to avoid excessive CPU usage
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Read interrupted", e);
            }
        }
        Character c = buffer.poll();
        return (c != null) ? c : -1; // Should not be -1 if buffer.isEmpty() check passes
    }

    @Override
    public void write(int i) throws IOException {
        if (!session.isOpen()) {
            throw new IOException("Session is closed");
        }
        try {
            session.getBasicRemote().sendText(String.valueOf((char) i));
        } catch (IOException e) {
            throw new IOException("Error writing to websocket session", e);
        }
    }

    @Override
    public void flush() throws IOException {
        // No-op as writes are expected to be immediate
    }

    @Override
    public void close() throws IOException {
        if (session.isOpen()) {
            session.close();
        }
    }

    @Override
    public void defineKey(String s, String s1) {
        // No-op
    }

    @Override
    public void negotiateTelnetParameters() {
        // No-op
    }

    @Override
    public int getColumns() {
        return 80; // Default value
    }

    @Override
    public int getRows() {
        return 24; // Default value
    }

    @Override
    public void setColumns(int i) {
        // No-op
    }

    @Override
    public void setRows(int i) {
        // No-op
    }

    @Override
    public void resetTerminal() {
        // No-op
    }

    @Override
    public void resetKeyPressed() {
        // No-op
    }

    @Override
    public boolean isKeyPressed() {
        return false; // Default value
    }

    @Override
    public String getTerminal() {
        return "WebSocket"; // Default value
    }

    @Override
    public void setTerminal(String s) {
        // No-op
    }

    @Override
    public boolean getCRNL() {
        return false; // Default value
    }

    @Override
    public void setCRNL(boolean b) {
        // No-op
    }

    // --- New methods as per subtask ---

    /**
     * Sends a command to the client to erase the screen.
     * @throws IOException if the send fails.
     */
    @Override
    public void eraseScreen() throws IOException {
        if (!session.isOpen()) {
            throw new IOException("Session is closed. Cannot erase screen.");
        }
        try {
            // In a real application, using a JSON library (like Jackson or Gson) would be more robust.
            // For this specific simple case, direct string construction is acceptable.
            String clearScreenCommand = "{\"command\": \"clear_screen\"}";
            session.getBasicRemote().sendText(clearScreenCommand);
        } catch (IOException e) {
            // Log or handle the exception appropriately
            // System.err.println("Error sending eraseScreen command: " + e.getMessage());
            throw new IOException("Error sending eraseScreen command to websocket session", e);
        }
    }

    /**
     * Moves the cursor to the home position (typically top-left).
     * Currently a no-op for WebSocket implementation.
     * @throws IOException if an I/O error occurs (not thrown in this no-op implementation).
     */
    @Override
    public void homeCursor() throws IOException {
        // No-op for now.
        // If specific client-side cursor manipulation is needed later,
        // a similar JSON command could be sent.
    }
}
