package com.medipass.server.domain.document.web.dto;

import java.net.URI;

public record DocumentDownloadRes(
        Long documentId,
        String originalFilename,
        URI downloadUrl
) {
}
