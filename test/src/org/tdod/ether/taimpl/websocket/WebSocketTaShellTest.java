package org.tdod.ether.taimpl.websocket;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.Assert;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.tdod.ether.ta.telnet.TaShell; // For type casting if needed, though not directly tested for all methods

import java.io.IOException;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Future;
import org.eclipse.jetty.websocket.api.BatchMode;
import org.eclipse.jetty.websocket.api.CloseStatus;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.UpgradeResponse;
import org.eclipse.jetty.io.ByteBufferPool;


// --- Mock Classes ---

class MockTaShellRemoteEndpoint implements RemoteEndpoint {
    private String lastSentMessage;
    private boolean throwExceptionOnSend = false;

    public String getLastSentMessage() {
        return lastSentMessage;
    }
    public void clearLastSentMessage() { this.lastSentMessage = null; }

    public void setThrowExceptionOnSend(boolean throwExceptionOnSend) {
        this.throwExceptionOnSend = throwExceptionOnSend;
    }

    @Override
    public void sendString(String text) throws IOException {
        if (throwExceptionOnSend) {
            throw new IOException("Mocked sendString exception from TaShellRemoteEndpoint");
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

class MockTaShellSession implements Session {
    private final MockTaShellRemoteEndpoint remoteEndpoint;
    private boolean open = true;
    private boolean closeCalled = false;
    private InetSocketAddress remoteAddress;

    public MockTaShellSession(MockTaShellRemoteEndpoint remoteEndpoint, InetSocketAddress remoteAddress) {
        this.remoteEndpoint = remoteEndpoint;
        this.remoteAddress = remoteAddress;
    }

    public RemoteEndpoint getBasicRemote() {
        return remoteEndpoint;
    }
     @Override
    public RemoteEndpoint getRemote() {
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

    @Override
    public InetSocketAddress getRemoteAddress() {
        return remoteAddress;
    }
    public void setRemoteAddress(InetSocketAddress address) {
        this.remoteAddress = address;
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

// No need to mock WebSocketShellIo for these tests, as WebSocketTaShell creates a real one.
// We are testing WebSocketTaShell's interaction with its created WebSocketShellIo.

public class WebSocketTaShellTest {

    private MockTaShellRemoteEndpoint mockRemote;
    private MockTaShellSession mockSession;
    private WebSocketTaShell taShell;
    private InetSocketAddress mockAddress;

    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalErr = System.err;

    @BeforeMethod
    public void setUp() {
        mockRemote = new MockTaShellRemoteEndpoint();
        mockAddress = new InetSocketAddress("testhost", 12345);
        mockSession = new MockTaShellSession(mockRemote, mockAddress);
        taShell = new WebSocketTaShell(mockSession);
        System.setErr(new PrintStream(errContent)); // Capture System.err
    }

    @org.testng.annotations.AfterMethod
    public void tearDown() {
        System.setErr(originalErr); // Restore System.err
        errContent.reset();
    }

    @Test
    public void testSetHideInput_True_SendsHideCommand() {
        taShell.setHideInput(true);
        Assert.assertEquals(mockRemote.getLastSentMessage(), "{\"command\": \"hide_input\"}");
    }

    @Test
    public void testSetHideInput_False_SendsShowCommand() {
        taShell.setHideInput(false);
        Assert.assertEquals(mockRemote.getLastSentMessage(), "{\"command\": \"show_input\"}");
    }

    @Test
    public void testSetHideInput_SessionClosed_PrintsError() {
        mockSession.setOpen(false);
        taShell.setHideInput(true);
        Assert.assertTrue(errContent.toString().contains("WebSocket session is not open. Cannot set input hide status."), "Error message not found in System.err");
        Assert.assertNull(mockRemote.getLastSentMessage());
    }

    @Test
    public void testSetHideInput_IOExceptionOnSend_PrintsError() {
        mockRemote.setThrowExceptionOnSend(true);
        taShell.setHideInput(true);
        Assert.assertTrue(errContent.toString().contains("Error sending input hide/show command to websocket session: Mocked sendString exception from TaShellRemoteEndpoint"));
    }

    @Test
    public void testGetShellIo_LazyInitializationAndReturnsInstance() {
        // Initially, _shellIo is null (private field, so we test behaviorally)
        // First call creates and returns instance
        com.meyling.telnet.shell.ShellIo shellIo1 = taShell.getShellIo();
        Assert.assertNotNull(shellIo1);
        Assert.assertTrue(shellIo1 instanceof WebSocketShellIo);

        // Second call returns the same instance
        com.meyling.telnet.shell.ShellIo shellIo2 = taShell.getShellIo();
        Assert.assertSame(shellIo2, shellIo1);
    }

    @Test
    public void testGetConnectionHostname_ReturnsCorrectly() {
        Assert.assertEquals(taShell.getConnectionHostname(), "testhost");
    }

    @Test
    public void testGetConnectionPort_ReturnsCorrectly() {
        Assert.assertEquals(taShell.getConnectionPort(), 12345);
    }

    @Test
    public void testGetConnectionId_ReturnsPort() {
        // As per current WebSocketTaShell implementation
        Assert.assertEquals(taShell.getConnectionId(), 12345);
    }

    @Test
    public void testClose_ShellIoNotNull_CallsShellIoClose() throws IOException {
        // Ensure _shellIo is created
        WebSocketShellIo shellIo = (WebSocketShellIo) taShell.getShellIo();
        // Now mockSession is the session within this shellIo instance

        Assert.assertFalse(mockSession.isCloseCalled(), "Session should not be closed yet by shellIo");
        taShell.close(); // This should call shellIo.close(), which calls session.close()
        Assert.assertTrue(mockSession.isCloseCalled(), "Session.close() should have been called via shellIo.close()");
    }

    @Test
    public void testClose_ShellIoNull_ClosesSessionDirectly() throws IOException {
        // _shellIo is null by default if getShellIo() is not called
        Assert.assertFalse(mockSession.isCloseCalled());
        taShell.close(); // Should close the session directly
        Assert.assertTrue(mockSession.isCloseCalled());
    }

    @Test
    public void testClose_IOExceptionInShellIoClose_PrintsError() throws IOException {
        // To test this, we need shellIo to be created, and its session.close() to throw
        // This is a bit tricky as shellIo.close() wraps session.close()
        // Let's assume shellIo.close itself throws for simplicity of this test setup,
        // or that the session it holds throws.
        // If WebSocketShellIo.close() catches and prints, that's harder to test here.
        // The current TaShell.close() catches and prints.

        // Get the shellIo, then make its session throw on close
        taShell.getShellIo(); // Initializes _shellIo

        // Make the session throw an exception when close is called
        MockTaShellSession sessionThatThrows = new MockTaShellSession(mockRemote, mockAddress) {
            @Override
            public void close() throws IOException {
                super.close(); // Mark as called
                throw new IOException("Mocked session close exception");
            }
        };
        // Replace the original session inside WebSocketTaShell's _shellIo is hard without direct access or mocking WebSocketShellIo.
        // For this test, we'll rely on WebSocketTaShell's own catch block.
        // The WebSocketShellIo used by taShell has the original mockSession.
        // We need to make that original mockSession's close() throw.

        // Re-setup with a session that will throw on close, but only for the shellIo's close
        MockTaShellRemoteEndpoint specificRemote = new MockTaShellRemoteEndpoint();
        MockTaShellSession specificSession = new MockTaShellSession(specificRemote, mockAddress) {
             private boolean alreadyTriedToThrow = false;
             @Override
             public void close() throws IOException {
                 super.close(); // Marks closeCalled = true
                 if (!alreadyTriedToThrow) { // Throw only once to simulate the error
                     alreadyTriedToThrow = true;
                     throw new IOException("Mocked session close exception from specificSession");
                 }
             }
        };
        WebSocketTaShell specificTaShell = new WebSocketTaShell(specificSession);
        specificTaShell.getShellIo(); // Initialize its _shellIo with specificSession

        specificTaShell.close();
        Assert.assertTrue(errContent.toString().contains("Error closing websocket session or shell: Mocked session close exception from specificSession"));
    }


    @Test
    public void testCleanup_CallsShellIoCloseIfShellIoInitialized() throws IOException {
        // Initialize _shellIo
        taShell.getShellIo();
        Assert.assertFalse(mockSession.isCloseCalled());
        taShell.cleanup("test_cleanup");
        Assert.assertTrue(mockSession.isCloseCalled(), "Session.close() should be called via shellIo.close() during cleanup");
    }

    @Test
    public void testCleanup_ShellIoNotInitialized_SessionStillCloses() throws IOException {
        // _shellIo is not initialized
        Assert.assertFalse(mockSession.isCloseCalled());
        taShell.cleanup("test_cleanup");
        // WebSocketTaShell.cleanup calls _shellIo.close() and then session.close() if session is open
        // So, session.close() will be called regardless of _shellIo state if session is open.
        Assert.assertTrue(mockSession.isCloseCalled(), "Session.close() should be called directly during cleanup if _shellIo was null");
    }

    @Test
    public void testOtherNoOpMethods() {
        // These methods are no-ops or depend on TelnetConnection which is null
        Assert.assertNull(taShell.getConnection()); // Returns null
        taShell.run(null); // No-op
        taShell.connectionIdle(null); // No-op
        // connectionTimedOut and connectionLogoutRequest call close()
        mockRemote.clearLastSentMessage(); // clear any previous messages
        mockSession.setOpen(true); // ensure session is open
        mockSession.closeCalled = false; // reset closeCalled

        taShell.connectionTimedOut(null);
        Assert.assertTrue(mockSession.isCloseCalled(), "close should be called on connectionTimedOut");
        Assert.assertTrue(errContent.toString().contains("Connection timed out. Closing session."));
        errContent.reset(); // Clear buffer for next check

        mockSession.setOpen(true); // reopen for next test
        mockSession.closeCalled = false;
        taShell.connectionLogoutRequest(null);
        Assert.assertTrue(mockSession.isCloseCalled(), "close should be called on connectionLogoutRequest");
        Assert.assertTrue(errContent.toString().contains("Logout request received. Closing session."));

        taShell.connectionSentBreak(null); // No-op
    }
}
