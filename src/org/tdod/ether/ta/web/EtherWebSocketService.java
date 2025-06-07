package org.tdod.ether.ta.web;

// import org.apache.commons.logging.Log; // Removed
// import org.apache.commons.logging.LogFactory; // Removed
import org.slf4j.Logger; // Added
import org.slf4j.LoggerFactory; // Added
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.tdod.ether.ta.engine.PlayerInputEventId;
import org.tdod.ether.ta.player.PlayerConnectedEventId;
import org.tdod.ether.ta.telnet.TaShell;
import org.tdod.ether.taimpl.websocket.WebSocketTaShell;
import org.tdod.ether.util.PlayerConnectedManager;
import org.tdod.ether.util.PlayerInputManager;

public class EtherWebSocketService implements WebSocketListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(EtherWebSocketService.class); // Changed to SLF4J

    private Session session;

    @Override
    public void onWebSocketBinary(byte[] bytes, int i, int i1) {
        // only text for now
        LOGGER.debug("Received binary data of length: {}", i1);
    }

    @Override
    public void onWebSocketText(String message) {
        LOGGER.debug("Received message: {}", message);

        TaShell shell = EtherWebSocket.getInstance().getMatchingSessionOrNew(session);
        if (shell == null) {
            // This case should ideally not happen if session management in EtherWebSocket is robust.
            // If it does, it means 'session' (the instance field) was not properly associated
            // or was cleared prematurely. The 'userSession' from onWebSocketConnect should be used
            // to uniquely identify the connection.
            // For now, log an error. A more robust solution might involve re-associating or rejecting.
            LOGGER.error("Could not find or create a TaShell for the current session. Message dropped: {}", message);
            // Attempt to close the problematic session if it's still open,
            // as it might be in an inconsistent state.
            if (this.session != null && this.session.isOpen()) {
                try {
                    this.session.close(1011, "Internal server error: shell association failed.");
                } catch (Exception e) {
                    LOGGER.warn("Exception while trying to close problematic session.", e);
                }
            }
            return;
        }
        PlayerInputManager.postPlayerInputEvent(PlayerInputEventId.General, shell.getConnectionId(), message);
    }

    @Override
    public void onWebSocketClose(int i, String s) {
        LOGGER.info("Connection closed: {} - Reason: {}", i, s);
        // The 'session' instance field in WebSocketListener is typically associated with ONE connection.
        // If this EtherWebSocketService instance is reused for multiple client connections by Jetty,
        // then 'this.session' needs careful handling. Assuming one listener instance per connection,
        // or that 'this.session' is correctly managed by onWebSocketConnect/Close.

        TaShell shell = EtherWebSocket.getInstance().getMatchingShell(this.session);
        if (shell != null) {
            PlayerConnectedManager.postPlayerConnectedEvent(PlayerConnectedEventId.Disconnected, shell);
            EtherWebSocket.getInstance().removeSession(this.session); // Explicitly remove on close
        } else {
            // This might happen if close is called after an error or if session was never fully registered.
            LOGGER.warn("No TaShell found for closing session. Session remote address: {}",
                        (this.session != null && this.session.getRemoteAddress() != null) ? this.session.getRemoteAddress().toString() : "unknown");
        }
        this.session = null; // Clear the session for this listener instance
    }

    @Override
    public void onWebSocketConnect(Session userSession) {
        // userSession is the definitive session object for this specific connection.
        this.session = userSession; // Associate this listener instance with this session.
        LOGGER.info("Connection opened: {}", userSession.getRemoteAddress().toString());

        TaShell shell = EtherWebSocket.getInstance().getMatchingSessionOrNew(userSession);
        // This shell instance should be the one associated with userSession.
        PlayerConnectedManager.postPlayerConnectedEvent(PlayerConnectedEventId.Connected, shell);
    }

    @Override
    public void onWebSocketError(Throwable t) {
        LOGGER.error("WebSocket Error: ", t);
        TaShell shell = null;
        if (session != null) {
            shell = EtherWebSocket.getInstance().getMatchingShell(this.session);
            // Attempt to gracefully inform the game about the disconnection due to error.
            if (shell != null) {
                 PlayerConnectedManager.postPlayerConnectedEvent(PlayerConnectedEventId.Disconnected, shell);
                 EtherWebSocket.getInstance().removeSession(this.session);
            }
            try {
                if (session.isOpen()) {
                    session.close(1011, "WebSocket Error"); // 1011 indicates an unexpected condition
                }
            } catch (Exception e) {
                LOGGER.warn("Exception while trying to close session on error: {}", e.getMessage());
            }
        }
        this.session = null; // Nullify to prevent reuse of a potentially bad session state.
    }
}
