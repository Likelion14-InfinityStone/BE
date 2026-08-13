package com.medipass.server.global.mfds.dto;

import java.util.List;

public record MfdsProductSearchResult(
        int totalCount,
        List<MfdsProductItem> items
) {
}
