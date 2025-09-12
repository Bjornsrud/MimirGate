package com.mimirgate.core.util;

import com.mimirgate.model.WallMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class WallRenderer {

    private static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Viser veggen med alle meldinger som får plass for gitt terminalbredde
     */
    public static void renderWall(PrintWriter out, BufferedReader in, List<WallMessage> messages, int terminalWidth) {
        out.println();
        out.println((terminalWidth == 80)
                ? "================================[ MESSAGE WALL ]================================"
                : "===========[ MESSAGE-WALL! ]===========");

        if (messages.isEmpty()) {
            out.println("(No messages yet.)");
        } else {
            for (WallMessage m : messages) {
                out.println(m.getUsername() + " @ " + m.getTimestamp().format(fmt));
                for (String line : wrapText(m.getContent(), terminalWidth)) {
                    out.println(line);
                }
                out.println();
            }
        }

        out.println((terminalWidth == 80)
                ? "==============================================================================="
                : "=======================================");
        out.println("Press any key to continue...");
        out.flush();

        try {
            in.readLine(); // pause til bruker trykker ENTER
        } catch (IOException ignored) { }
    }

    /**
     * Bryter tekst til linjer basert på terminalbredde
     */
    private static List<String> wrapText(String text, int width) {
        List<String> lines = new ArrayList<>();
        String remaining = text;
        while (remaining.length() > width) {
            lines.add(remaining.substring(0, width));
            remaining = remaining.substring(width);
        }
        if (!remaining.isEmpty()) {
            lines.add(remaining);
        }
        return lines;
    }
}
