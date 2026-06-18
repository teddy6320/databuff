package com.databuff.apm.web.ai.agent;

import com.databuff.apm.web.ai.platform.runtime.SessionWorkspaceService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

@RestController
@RequestMapping("/api/v1/ai/sessions/{sessionId}/workspace")
public class SessionWorkspaceController {

    private final SessionWorkspaceService sessionWorkspaceService;

    public SessionWorkspaceController(SessionWorkspaceService sessionWorkspaceService) {
        this.sessionWorkspaceService = sessionWorkspaceService;
    }

    @GetMapping("/files")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String sessionId,
            @RequestParam("path") String path) {
        Path file = sessionWorkspaceService.resolveDownloadPath(sessionId, path);
        String fileName = file.getFileName() == null ? "download" : file.getFileName().toString();
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
