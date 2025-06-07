package org.tdod.ether.taimpl.websocket;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.Assert;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Future;
import org.eclipse.jetty.websocket.api.BatchMode;
import org.eclipse.jetty.websocket.api.CloseStatus;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.UpgradeResponse;
import org.eclipse.jetty.io.ByteBufferPool;


// --- Mock Classes ---
// (Similar to WebGameOutputTest, but self-contained for clarity and potential minor differences)

class MockIoRemoteEndpoint implements RemoteEndpoint {
    private String lastSentMessage;
    private boolean throwExceptionOnSend = false;

    public String getLastSentMessage() {
        return lastSentMessage;
    }

    public void setThrowExceptionOnSend(boolean throwExceptionOnSend) {
        this.throwExceptionOnSend = throwExceptionOnSend;
    }

    @Override
    public void sendString(String text) throws IOException {
        if (throwExceptionOnSend) {
            throw new IOException("Mocked sendString exception");
        }
        this.lastSentMessage = text;
    }

    // Other RemoteEndpoint methods
    @Override public void sendBytes(java.nio.ByteBuffer data) throws IOException {}
    @Override public void sendPartialString(String fragment, boolean isLast) throws IOException {}
    @Override public void sendPartialBytes(java.nio.ByteBuffer fragment, boolean isLast) throws IOException {}
    @Override public BatchMode getBatchMode() { return BatchMode.OFF; }
    @Override public void setBatchMode(BatchMode mode) {}
    @Override public int getMaxOutgoingFrames() { return 0; }
    @Override public void setMaxOutgoingFrames(int maxOutgoingFrames) {}
    @Override public void flush() throws IOException {}
    @Override public void sendObject(Object data) throws IOException {}
    @Override public void sendPing(java.nio.ByteBuffer applicationData) throws IOException {}
    @Override public void sendPong(java.nio.ByteBuffer applicationData) throws IOException {}
}

class MockIoSession implements Session {
    private final MockIoRemoteEndpoint remoteEndpoint;
    private boolean open = true;
    private boolean closeCalled = false;

    public MockIoSession(MockIoRemoteEndpoint remoteEndpoint) {
        this.remoteEndpoint = remoteEndpoint;
    }

    @Override
    public RemoteEndpoint getRemote() { // Used by WebGameOutput
        return remoteEndpoint;
    }

    public RemoteEndpoint getBasicRemote() { // Used by WebSocketShellIo for sendText
        return remoteEndpoint;
    }


    @Override
    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public boolean isCloseCalled() {
        return closeCalled;
    }

    @Override
    public void close() throws IOException {
        this.closeCalled = true;
        this.open = false;
    }

    @Override
    public void close(int statusCode, String reason) throws IOException {
        this.closeCalled = true;
        this.open = false;
    }

    // Other Session methods
    @Override public void close(CloseStatus closeStatus) throws IOException {}
    @Override public void disconnect() throws IOException {}
    @Override public long getIdleTimeout() { return 0; }
    @Override public InetSocketAddress getLocalAddress() { return null; }
    @Override public int getMaxBinaryMessageBufferSize() { return 0; }
    @Override public int getMaxTextMessageBufferSize() { return 0; }
    @Override public String getNegotiatedSubprotocol() { return null; }
    @Override public org.eclipse.jetty.websocket.api.ポリシー getPolicy() { return null; }
    @Override public String getProtocolVersion() { return null; }
    @Override public InetSocketAddress getRemoteAddress() { return null; }
    @Override public UpgradeRequest getUpgradeRequest() { return null; }
    @Override public UpgradeResponse getUpgradeResponse() { return null; }
    @Override public boolean isSecure() { return false; }
    @Override public void setIdleTimeout(long ms) {}
    @Override public void setMaxBinaryMessageBufferSize(int length) {}
    @Override public void setMaxTextMessageBufferSize(int length) {}
    @Override public ByteBufferPool getByteBufferPool() { return null; }
    @Override public boolean isOutgoingFramesFlushed() { return false; }
    @Override public void suspend() {}
    @Override public Future<Void> resume() { return null; }
    @Override public void addMessageHandler(Class handler, org.eclipse.jetty.websocket.api.MessageHandler.Partial callback) {}
    @Override public void addMessageHandler(Class handler, org.eclipse.jetty.websocket.api.MessageHandler.Whole callback) {}
    @Override public <T> void addMessageHandler(org.eclipse.jetty.websocket.api.MessageHandlerFactory factory, Class<T> handlerType, Class<? extends org.eclipse.jetty.websocket.api.MessageHandler> messageHandlerType) {}
    @Override public java.util.Set<org.eclipse.jetty.websocket.api.extensions.ExtensionConfig> getNegotiatedExtensions() { return java.util.Collections.emptySet(); }
    @Override public boolean isBadEof() { return false; }
    @Override public long getMaxFrameSize() { return 0; }
    @Override public void setMaxFrameSize(long maxFrameSize) {}
    @Override public org.eclipse.jetty.websocket.api.SuspendToken suspendInput() { return null; }

}

public class WebSocketShellIoTest {

    private MockIoRemoteEndpoint mockRemote;
    private MockIoSession mockSession;
    private WebSocketShellIo shellIo;

    @BeforeMethod
    public void setUp() {
        mockRemote = new MockIoRemoteEndpoint();
        mockSession = new MockIoSession(mockRemote);
        shellIo = new WebSocketShellIo(mockSession);
    }

    @Test
    public void testEraseScreen_SendsCorrectJson() throws IOException {
        shellIo.eraseScreen();
        Assert.assertEquals(mockRemote.getLastSentMessage(), "{\"command\": \"clear_screen\"}");
    }

    @Test(expectedExceptions = IOException.class, expectedExceptionsMessageRegExp = "Session is closed. Cannot erase screen.")
    public void testEraseScreen_ThrowsIOExceptionWhenSessionClosed() throws IOException {
        mockSession.setOpen(false);
        shellIo.eraseScreen();
    }

    @Test(expectedExceptions = IOException.class, expectedExceptionsMessageRegExp = "Error sending eraseScreen command to websocket session")
    public void testEraseScreen_ThrowsIOExceptionOnSendError() throws IOException {
        mockRemote.setThrowExceptionOnSend(true);
        shellIo.eraseScreen();
    }

    @Test
    public void testHomeCursor_IsNoOp() throws IOException {
        shellIo.homeCursor();
        Assert.assertNull(mockRemote.getLastSentMessage(), "homeCursor should not send any message");
    }

    @Test
    public void testWrite_SendsChar() throws IOException {
        shellIo.write('A');
        Assert.assertEquals(mockRemote.getLastSentMessage(), "A");
        shellIo.write('b');
        Assert.assertEquals(mockRemote.getLastSentMessage(), "b");
    }

    @Test(expectedExceptions = IOException.class, expectedExceptionsMessageRegExp = "Session is closed")
    public void testWrite_ThrowsIOExceptionWhenSessionClosed() throws IOException {
        mockSession.setOpen(false);
        shellIo.write('A');
    }

    @Test(expectedExceptions = IOException.class, expectedExceptionsMessageRegExp = "Error writing to websocket session")
    public void testWrite_ThrowsIOExceptionOnSendError() throws IOException {
        mockRemote.setThrowExceptionOnSend(true);
        shellIo.write('A');
    }

    @Test
    public void testFlush_IsNoOp() throws IOException {
        shellIo.flush(); // Should not throw or do anything observable here
        Assert.assertNull(mockRemote.getLastSentMessage());
    }


    @Test
    public void testClose_CallsSessionCloseAndClosesSession() throws IOException {
        Assert.assertTrue(mockSession.isOpen());
        Assert.assertFalse(mockSession.isCloseCalled());
        shellIo.close();
        Assert.assertTrue(mockSession.isCloseCalled());
        Assert.assertFalse(mockSession.isOpen()); // MockSession.close() sets open to false
    }

    @Test
    public void testClose_WhenSessionAlreadyClosed_DoesNotThrow() throws IOException {
        mockSession.setOpen(false); // Simulate already closed
        mockSession.close(); // Call it once to set closeCalled
        Assert.assertTrue(mockSession.isCloseCalled());

        shellIo.close(); // Should be idempotent or at least not error
        Assert.assertTrue(mockSession.isCloseCalled()); // Still true
    }

    @Test
    public void testNoOpMethodsDontThrow() {
        shellIo.defineKey("Ctrl+C", "xyz");
        shellIo.negotiateTelnetParameters();
        Assert.assertEquals(shellIo.getColumns(), 80);
        Assert.assertEquals(shellIo.getRows(), 24);
        shellIo.setColumns(100); // No-op, so no change to check beyond no error
        shellIo.setRows(40);   // No-op
        shellIo.resetTerminal();
        shellIo.resetKeyPressed();
        Assert.assertFalse(shellIo.isKeyPressed());
        Assert.assertEquals(shellIo.getTerminal(), "WebSocket");
        shellIo.setTerminal("vt100"); // No-op
        Assert.assertFalse(shellIo.getCRNL());
        shellIo.setCRNL(true); // No-op
    }
}
