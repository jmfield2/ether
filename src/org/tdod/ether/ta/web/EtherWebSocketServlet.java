package org.tdod.ether.ta.web;

import org.eclipse.jetty.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.websocket.server.JettyWebSocketServletFactory;

public class EtherWebSocketServlet extends JettyWebSocketServlet
{

    @Override
    public void configure(JettyWebSocketServletFactory factory)
    {
        factory.register(EtherWebSocketService.class);
    }
}