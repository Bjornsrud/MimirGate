package com.mimirgate.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class Pager {

    private static final int PAGE_SIZE = 22; // 25 lines screen, keep 3 lines free

    /**
     * Viser en liste med linjer side for side.
     * ENTER = neste side, Q = avbryt.
     */
    public static void display(List<String> lines, PrintWriter out, BufferedReader in) throws IOException {
        int count = 0;
        for (int i = 0; i < lines.size(); i++) {
            out.println(lines.get(i));
            count++;
            if (count >= PAGE_SIZE && i < lines.size() - 1) {
                out.print("--- Press ENTER for more, Q to quit --- ");
                out.flush();
                String input = in.readLine();
                if (input != null && input.trim().equalsIgnoreCase("Q")) {
                    break;
                }
                count = 0;
            }
        }
    }

    /**
     * Returnerer et utsnitt (range) fra en liste.
     */
    public static List<String> slice(List<String> lines, int start, int end) {
        int safeStart = Math.max(0, start);
        int safeEnd = Math.min(lines.size(), end);
        return lines.subList(safeStart, safeEnd);
    }
}
