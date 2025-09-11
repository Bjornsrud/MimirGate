package com.mimirgate.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class TextInputEditor {

    /**
     * Lar brukeren skrive inn tekst med en maksgrense.
     * Bruk /save for å lagre, /cancel for å avbryte.
     * @param out PrintWriter for å sende tekst til klient
     * @param in BufferedReader for å lese input
     * @param prompt Tittel eller instruksjonstekst
     * @param maxChars Maksimalt antall tegn
     * @return Teksten som brukeren skrev, eller null hvis avbrutt
     * @throws IOException Hvis lesing feiler
     */
    public static String promptForText(PrintWriter out, BufferedReader in, String prompt, int maxChars) throws IOException {
        out.println();
        out.println(prompt + " (max " + maxChars + " chars)");
        out.println("Type /save to finish, /cancel to abort.");
        out.flush();

        StringBuilder buffer = new StringBuilder();
        int remaining = maxChars;

        while (true) {
            out.print("> ");
            out.flush();
            String line = in.readLine();
            if (line == null) {
                return null; // klient koblet fra
            }

            // Kommandoer
            if ("/save".equalsIgnoreCase(line.trim())) {
                return buffer.toString().trim();
            } else if ("/cancel".equalsIgnoreCase(line.trim())) {
                out.println("Input canceled.");
                return null;
            }

            // Legg til tekst hvis plass
            if (buffer.length() + line.length() > maxChars) {
                int canTake = maxChars - buffer.length();
                if (canTake > 0) {
                    buffer.append(line, 0, canTake);
                }
                out.println("Character limit reached! Ignoring rest of input.");
            } else {
                if (!buffer.isEmpty()) {
                    buffer.append(System.lineSeparator());
                }
                buffer.append(line);
            }

            remaining = maxChars - buffer.length();
            out.println("Characters remaining: " + remaining);
        }
    }
}
