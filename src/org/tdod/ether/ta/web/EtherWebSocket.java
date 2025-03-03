package org.tdod.ether.ta.web;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer;
import org.tdod.ether.ta.telnet.TaShell;
import org.tdod.ether.taimpl.websocket.WebSocketTaShell;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class EtherWebSocket {

    private static EtherWebSocket _instance;

    private Map<Session, TaShell> sockets;
    private Server server;

    private EtherWebSocket() {
        this._instance = this;
        this.sockets = new HashMap<>();
    }

    public Server getServer() {
        return this.server;
    }

    public TaShell getMatchingSessionOrNew(Session s) {

        this.sockets.computeIfAbsent(s, WebSocketTaShell::new);

        return this.sockets.get(s);

    }

    public static EtherWebSocket getInstance() {
        if (EtherWebSocket._instance == null) {
            EtherWebSocket._instance = new EtherWebSocket();

            try {
                EtherWebSocket._instance.server = EtherWebSocket.createService();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return EtherWebSocket._instance;
    }

    public static Server createService() throws Exception {

        Server server = new Server(8080);

        ServletContextHandler context = new ServletContextHandler(server, "/");

        // Server WS client html
        context.setBaseResource(Resource.newResource(new File(".")));

        // Add websocket servlet
        context.addServlet(EtherWebSocketServlet.class, "/ether");

        context.addServlet(new ServletHolder("default", DefaultServlet.class), "/");

        server.setHandler(context);

        JettyWebSocketServletContainerInitializer.configure(context, null);

        return server;
    }

}
