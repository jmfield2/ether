package org.tdod.ether.ta.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.tdod.ether.ta.engine.PlayerInputEventId;
import org.tdod.ether.ta.player.PlayerConnectedEventId;
import org.tdod.ether.ta.telnet.TaShell;
import org.tdod.ether.taimpl.websocket.WebSocketTaShell;
import org.tdod.ether.util.PlayerConnectedManager;
import org.tdod.ether.util.PlayerInputManager;

public class EtherWebSocketService implements WebSocketListener {

    private Log LOGGER = LogFactory.getLog(EtherWebSocketService.class);

    private Session session;

    @Override
    public void onWebSocketBinary(byte[] bytes, int i, int i1) {
        // only text for now
    }

    @Override
    public void onWebSocketText(String message) {

        LOGGER.debug("Received message: " + message);

        TaShell shell = EtherWebSocket.getInstance().getMatchingSessionOrNew(session);

        PlayerInputManager.postPlayerInputEvent(PlayerInputEventId.General, shell.getConnectionId(), message);
    }

    @Override
    public void onWebSocketClose(int i, String s) {
        LOGGER.info("Connection closed: " + i + " - Reason: " + s);

        this.session = null; // Clear the session

        PlayerConnectedManager.postPlayerConnectedEvent(PlayerConnectedEventId.Disconnected, null);
    }

    @Override
    public void onWebSocketConnect(Session userSession) {
        LOGGER.info("Connection opened: " + userSession.getRemoteAddress().toString());

        TaShell shell = EtherWebSocket.getInstance().getMatchingSessionOrNew(userSession);

        this.session = userSession;
        PlayerConnectedManager.postPlayerConnectedEvent(PlayerConnectedEventId.Connected, shell);
    }

    @Override
    public void onWebSocketError(Throwable t) {
        LOGGER.error("Error: ", t);
        if (session != null) {
            try {
                session.close(); // Close the session on error
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
