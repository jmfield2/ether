package org.tdod.ether.ta.web;

import org.eclipse.jetty.websocket.api.Session;
import org.tdod.ether.ta.output.GameOutput;
import org.tdod.ether.taimpl.websocket.WebSocketTaShell;

import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class WebGameOutput implements GameOutput {

    private final Session session;

    public WebGameOutput(WebSocketTaShell shell) {
        this.session = shell.getSession();
    }

    private String getHtmlColor(char c) {
        switch (c) {
            case 'B': return "black";
            case 'R': return "red";
            case 'G': return "green";
            case 'Y': return "yellow";
            case 'b': return "blue"; // Lowercase 'b' for blue
            case 'M': return "magenta";
            case 'C': return "cyan";
            case 'W': return "white";
            default:  return null; // Unrecognized color
        }
    }

    private String parseColorCodesToHtml(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }

        StringBuilder htmlOutput = new StringBuilder();
        CharacterIterator it = new StringCharacterIterator(s);
        StringBuilder currentSegment = new StringBuilder();
        String currentColor = "white"; // Default color

        boolean inSpan = false;

        for (char ch = it.first(); ch != CharacterIterator.DONE; ch = it.next()) {
            if (ch == '&') {
                char nextChar = it.next();
                if (nextChar == CharacterIterator.DONE) {
                    currentSegment.append(ch); // Dangling '&' at the end
                    break;
                }
                if (nextChar == '&') {
                    currentSegment.append('&'); // Escaped '&'
                } else {
                    String newHtmlColor = getHtmlColor(nextChar);
                    if (newHtmlColor != null) {
                        // Append current segment with previous color
                        if (currentSegment.length() > 0) {
                            if (inSpan) {
                                htmlOutput.append(currentSegment.toString());
                                htmlOutput.append("</span>");
                            } else if (!"white".equals(currentColor)) { // Don't wrap default white in span initially
                                htmlOutput.append("<span style=\"color:").append(currentColor).append(";\">");
                                htmlOutput.append(currentSegment.toString());
                                htmlOutput.append("</span>");
                            } else {
                                htmlOutput.append(currentSegment.toString());
                            }
                            currentSegment.setLength(0);
                        } else if (inSpan && !newHtmlColor.equals(currentColor)){
                            htmlOutput.append("</span>"); // Close previous span if color changes
                            inSpan = false;
                        }


                        currentColor = newHtmlColor;
                        if (!"white".equals(currentColor)) {
                             htmlOutput.append("<span style=\"color:").append(currentColor).append(";\">");
                             inSpan = true;
                        } else {
                            // If we switch to white and were in a span, it's implicitly closed by previous logic or next segment
                            inSpan = false;
                        }

                    } else {
                        // Unrecognized color code, treat '&' and nextChar literally
                        currentSegment.append('&');
                        currentSegment.append(nextChar);
                    }
                }
            } else {
                currentSegment.append(ch);
            }
        }

        // Append any remaining segment
        if (currentSegment.length() > 0) {
             if (inSpan) {
                htmlOutput.append(currentSegment.toString());
                htmlOutput.append("</span>");
            } else if (!"white".equals(currentColor)) { // Don't wrap default white in span initially
                htmlOutput.append("<span style=\"color:").append(currentColor).append(";\">");
                htmlOutput.append(currentSegment.toString());
                htmlOutput.append("</span>");
            } else {
                htmlOutput.append(currentSegment.toString());
            }
        } else if (inSpan) {
            // If the string ends with a color code, the span might be open without content
            htmlOutput.append("</span>");
        }


        return htmlOutput.toString();
    }

    @Override
    public void println(String s) {
        try {
            session.getRemote().sendString(parseColorCodesToHtml(s) + "\r\n");
        } catch (IOException e) {
            // In a real app, consider more robust error handling or logging
            System.err.println("Error sending WebSocket message: " + e.getMessage());
            // Depending on requirements, you might want to re-throw, perhaps wrapped
            // throw new RuntimeException(e);
        }
    }

    @Override
    public void print(String s) {
        try {
            session.getRemote().sendString(parseColorCodesToHtml(s));
        } catch (IOException e) {
            System.err.println("Error sending WebSocket message: " + e.getMessage());
            // throw new RuntimeException(e);
        }
    }

    @Override
    public void printlnWithoutColor(String s) {
        // Send raw string, browser default color will apply
        try {
            session.getRemote().sendString(s + "\r\n");
        } catch (IOException e) {
            System.err.println("Error sending WebSocket message: " + e.getMessage());
            // throw new RuntimeException(e);
        }
    }

    @Override
    public void printWithoutColor(String s) {
        // Send raw string, browser default color will apply
        try {
            session.getRemote().sendString(s);
        } catch (IOException e) {
            System.err.println("Error sending WebSocket message: " + e.getMessage());
            // throw new RuntimeException(e);
        }
    }
}
