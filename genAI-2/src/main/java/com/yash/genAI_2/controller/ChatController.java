package com.yash.genAI_2.controller;

import com.yash.genAI_2.model.Query;
import com.yash.genAI_2.service.ChatService;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
public class ChatController {

//    private final OllamaChatModel chatModel;
    private  final ChatService chatService;
    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    OllamaApi ollamaApi = new OllamaApi();

    OllamaChatModel chatModel = OllamaChatModel.builder()
            .ollamaApi(ollamaApi)
            .defaultOptions(
                    OllamaOptions.builder()
                            .model("gemma3")
                            .temperature(0.9)
                            .build())
            .build();

    @GetMapping("/ai/generate")
    public Map<String,String> generate(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return Map.of("generation", this.chatModel.call(message));
    }

    @GetMapping("/ai/generateStream")
    public Flux<ChatResponse> generateStream(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        return this.chatModel.stream(prompt);
    }

    @PostMapping("ai/query")
    public String chat(@RequestBody Query queryObject) throws ExecutionException, InterruptedException {
        return  chatService.generateRes(queryObject);
    }

}