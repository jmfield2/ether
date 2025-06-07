package org.tdod.ether.ta.web;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.Assert;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.tdod.ether.taimpl.websocket.WebSocketTaShell;

import java.io.IOException;
import java.net.InetSocketAddress; // Required for WebSocketTaShell constructor if not mocking it fully
import org.eclipse.jetty.websocket.api.UpgradeRequest; // Required for WebSocketTaShell constructor's Session mock

// For simplicity, Session and RemoteEndpoint are manually mocked here.
// In a larger project, Mockito or a similar framework would be preferred.

class MockRemoteEndpoint implements RemoteEndpoint {
    private String lastSentMessage;

    public String getLastSentMessage() {
        return lastSentMessage;
    }

    @Override
    public void sendString(String text) throws IOException {
        this.lastSentMessage = text;
    }

    // Other RemoteEndpoint methods are not needed for these tests
    @Override public void sendBytes(java.nio.ByteBuffer data) throws IOException {}
    @Override public void sendPartialString(String fragment, boolean isLast) throws IOException {}
    @Override public void sendPartialBytes(java.nio.ByteBuffer fragment, boolean isLast) throws IOException {}
    @Override public org.eclipse.jetty.websocket.api.BatchMode getBatchMode() { return org.eclipse.jetty.websocket.api.BatchMode.OFF; }
    @Override public void setBatchMode(org.eclipse.jetty.websocket.api.BatchMode mode) {}
    @Override public int getMaxOutgoingFrames() { return 0; }
    @Override public void setMaxOutgoingFrames(int maxOutgoingFrames) {}
    @Override public void flush() throws IOException {}
    @Override public void sendObject(Object data) throws IOException {}
    @Override public void sendPing(java.nio.ByteBuffer applicationData) throws IOException {}
    @Override public void sendPong(java.nio.ByteBuffer applicationData) throws IOException {}
}

class MockSession implements Session {
    private final MockRemoteEndpoint remoteEndpoint;
    private boolean open = true;

    public MockSession(MockRemoteEndpoint remoteEndpoint) {
        this.remoteEndpoint = remoteEndpoint;
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

    // Other Session methods not needed for these tests
    @Override public void close() throws IOException {}
    @Override public void close(int statusCode, String reason) throws IOException {}
    @Override public void close(org.eclipse.jetty.websocket.api.CloseStatus closeStatus) throws IOException {}
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
    @Override public org.eclipse.jetty.websocket.api.UpgradeResponse getUpgradeResponse() { return null; }
    @Override public boolean isSecure() { return false; }
    @Override public void setIdleTimeout(long ms) {}
    @Override public void setMaxBinaryMessageBufferSize(int length) {}
    @Override public void setMaxTextMessageBufferSize(int length) {}
    @Override public org.eclipse.jetty.io.ByteBufferPool getByteBufferPool() { return null; }
    @Override public boolean isOutgoingFramesFlushed() { return false; }
    @Override public void suspend() {}
    @Override public java.util.concurrent.Future<Void> resume() { return null; }
}


public class WebGameOutputTest {

    private MockRemoteEndpoint mockRemote;
    private MockSession mockSession;
    private WebSocketTaShell mockShell; // We'll use a real WebSocketTaShell with a mock Session
    private WebGameOutput output;

    @BeforeMethod
    public void setUp() {
        mockRemote = new MockRemoteEndpoint();
        mockSession = new MockSession(mockRemote);
        // WebSocketTaShell's constructor takes a Session. We provide our mock session.
        // This avoids needing to mock WebSocketTaShell itself if its constructor is simple.
        mockShell = new WebSocketTaShell(mockSession);
        output = new WebGameOutput(mockShell);
    }

    @Test
    public void testPrintlnPlainString() throws IOException {
        output.println("Hello World");
        Assert.assertEquals(mockRemote.getLastSentMessage(), "Hello World\r\n");
    }

    @Test
    public void testPrintPlainString() throws IOException {
        output.print("Hello World");
        Assert.assertEquals(mockRemote.getLastSentMessage(), "Hello World");
    }

    @Test
    public void testPrintlnSingleRedColor() throws IOException {
        output.println("&RRed Text");
        Assert.assertEquals(mockRemote.getLastSentMessage(), "<span style=\"color:red;\">Red Text</span>\r\n");
    }

    @Test
    public void testPrintSingleGreenColor() throws IOException {
        output.print("&GGreen Text");
        Assert.assertEquals(mockRemote.getLastSentMessage(), "<span style=\"color:green;\">Green Text</span>");
    }

    @Test
    public void testPrintlnSingleBlueColor() throws IOException {
        output.println("&bBlue Text"); // Lowercase 'b'
        Assert.assertEquals(mockRemote.getLastSentMessage(), "<span style=\"color:blue;\">Blue Text</span>\r\n");
    }

    @Test
    public void testPrintMultipleColorCodes() throws IOException {
        output.print("&RRed&GGreen&YYellow");
        Assert.assertEquals(mockRemote.getLastSentMessage(), "<span style=\"color:red;\">Red</span><span style=\"color:green;\">Green</span><span style=\"color:yellow;\">Yellow</span>");
    }

    @Test
    public void testPrintlnMultipleColorCodesWithText() throws IOException {
        output.println("Default &RRed &GGreen &bBlue &WBackToWhite");
        Assert.assertEquals(mockRemote.getLastSentMessage(), "Default <span style=\"color:red;\">Red </span><span style=\"color:green;\">Green </span><span style=\"color:blue;\">Blue </span><span style=\"color:white;\">BackToWhite</span>\r\n");
    }

    @Test
    public void testPrintColorResetsToDefaultImplicitly() throws IOException {
        // The current implementation might wrap "DefaultAgain" in white, or not if it's the base.
        // The key is that "Red" is red, and "DefaultAgain" is not red.
        // Based on current parseColorCodesToHtml, "white" explicitly gets a span.
        output.print("Default &RRed &WDefaultAgain");
        Assert.assertEquals(mockRemote.getLastSentMessage(), "Default <span style=\"color:red;\">Red </span><span style=\"color:white;\">DefaultAgain</span>");
    }

    @Test
    public void testPrintlnEscapedAmpersand() throws IOException {
        output.println("This && That");
        Assert.assertEquals(mockRemote.getLastSentMessage(), "This & That\r\n");
    }

    @Test
    public void testPrintEscapedAmpersandAtStart() throws IOException {
        output.print("&&Hello");
        Assert.assertEquals(mockRemote.getLastSentMessage(), "&Hello");
    }

    @Test
    public void testPrintlnUnrecognizedColorCode() throws IOException {
        output.println("&XUnknown &RRed");
        Assert.assertEquals(mockRemote.getLastSentMessage(), "&XUnknown <span style=\"color:red;\">Red</span>\r\n");
    }

    @Test
    public void testPrintUnrecognizedColorCodeMiddle() throws IOException {
        output.print("Before &X After");
        Assert.assertEquals(mockRemote.getLastSentMessage(), "Before &X After");
    }

    @Test
    public void testPrintlnStringStartingWithColor() throws IOException {
        output.println("&YYellow Text");
        Assert.assertEquals(mockRemote.getLastSentMessage(), "<span style=\"color:yellow;\">Yellow Text</span>\r\n");
    }

    @Test
    public void testPrintStringEndingWithColorCode() throws IOException {
        output.print("Text ends with &M");
        Assert.assertEquals(mockRemote.getLastSentMessage(), "Text ends with <span style=\"color:magenta;\"></span>");
    }

    @Test
    public void testPrintlnStringEndingWithColorCodeAndText() throws IOException {
        output.println("Text &MEnds Magenta");
        Assert.assertEquals(mockRemote.getLastSentMessage(), "Text <span style=\"color:magenta;\">Ends Magenta</span>\r\n");
    }

    @Test
    public void testPrintWithoutColor() throws IOException {
        output.printWithoutColor("Hello &RWorld");
        Assert.assertEquals(mockRemote.getLastSentMessage(), "Hello &RWorld");
    }

    @Test
    public void testPrintlnWithoutColor() throws IOException {
        output.printlnWithoutColor("Hello &GWorld");
        Assert.assertEquals(mockRemote.getLastSentMessage(), "Hello &GWorld\r\n");
    }

    @Test
    public void testPrintEmptyString() throws IOException {
        output.print("");
        Assert.assertEquals(mockRemote.getLastSentMessage(), "");
    }

    @Test
    public void testPrintlnEmptyString() throws IOException {
        output.println("");
        Assert.assertEquals(mockRemote.getLastSentMessage(), "\r\n");
    }

    @Test
    public void testPrintOnlyColorCodes() throws IOException {
        output.print("&R&G&b");
        Assert.assertEquals(mockRemote.getLastSentMessage(), "<span style=\"color:red;\"></span><span style=\"color:green;\"></span><span style=\"color:blue;\"></span>");
    }

    @Test
    public void testDanglingAmpersandAtEnd() throws IOException {
        output.println("Hello &");
        Assert.assertEquals(mockRemote.getLastSentMessage(), "Hello &\r\n");
    }

    @Test
    public void testAdjacentColorCodes() throws IOException {
        output.println("A&R&GSB");
        // Expected: A<span style="color:red;"></span><span style="color:green;">S</span>B
        // The behavior for "A&R&GSB" is that &R applies to nothing, then &G applies to S.
        Assert.assertEquals(mockRemote.getLastSentMessage(), "A<span style=\"color:red;\"></span><span style=\"color:green;\">S</span>B\r\n");
    }

    @Test
    public void testWhiteIsHandledCorrectly() throws IOException {
        output.println("&WWhite text &Rthen red");
        Assert.assertEquals(mockRemote.getLastSentMessage(), "<span style=\"color:white;\">White text </span><span style=\"color:red;\">then red</span>\r\n");
    }

    @Test
    public void testNoColorTextThenColored() throws IOException {
        output.println("Plain &CThen Cyan");
        Assert.assertEquals(mockRemote.getLastSentMessage(), "Plain <span style=\"color:cyan;\">Then Cyan</span>\r\n");
    }
}
