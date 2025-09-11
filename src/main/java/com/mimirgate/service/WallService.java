package com.mimirgate.service;

import com.mimirgate.model.WallMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class WallService {
    private final List<WallMessage> messages = Collections.synchronizedList(new ArrayList<>());
    private static final int MAX_CHARS = 160;
    private static final int SCREEN_HEIGHT = 24; // standard høyde på terminalen
    private static final int HEADER_FOOTER_LINES = 4; // header, footer og instruksjoner

    public void addMessage(String username, String text) {
        messages.add(new WallMessage(username, text));
    }

    public List<WallMessage> getMessagesForWidth(int terminalWidth) {
        // Hvor mange linjer en melding bruker: 1 for username+timestamp + innhold + 1 blank linje
        int linesPerMessage = 1 + (int) Math.ceil((double) MAX_CHARS / terminalWidth) + 1;

        // Hvor mange meldinger som får plass:
        int availableLines = SCREEN_HEIGHT - HEADER_FOOTER_LINES;
        int maxMessages = Math.max(1, availableLines / linesPerMessage);

        int fromIndex = Math.max(0, messages.size() - maxMessages);
        return new ArrayList<>(messages.subList(fromIndex, messages.size()));
    }

    public int getMaxChars() {
        return MAX_CHARS;
    }
}
