package com.databuff.apm.web.monitor.policy;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public final class AlarmExportSupport {

    private AlarmExportSupport() {
    }

    public static void writeCsv(
            HttpServletResponse response,
            String fileName,
            List<String> headers,
            List<Map<String, Object>> rows,
            List<String> fields) throws IOException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8))) {
            writer.write('\ufeff');
            writer.println(String.join(",", headers));
            for (Map<String, Object> row : rows) {
                writer.println(fields.stream()
                        .map(field -> csvCell(row.get(field)))
                        .reduce((left, right) -> left + "," + right)
                        .orElse(""));
            }
            writer.flush();
        }
    }

    private static String csvCell(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }
}
