package com.medipass.server.global.openai.client;

public interface OpenAiClient {

    // system + user 프롬프트로 채팅 완성 요청 후 응답 텍스트 반환
    String chat(String systemPrompt, String userPrompt);
}
