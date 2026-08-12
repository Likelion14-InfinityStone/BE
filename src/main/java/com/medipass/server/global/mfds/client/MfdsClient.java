package com.medipass.server.global.mfds.client;

import com.medipass.server.global.mfds.dto.MfdsProductSearchResult;

public interface MfdsClient {

    MfdsProductSearchResult searchByProductName(String productName);
}
