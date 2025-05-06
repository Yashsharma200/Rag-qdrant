package com.yash.genAI_2.service;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    @Autowired
    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public float[] generateEmbedding(String text){
        EmbeddingResponse embeddingResponse = this.embeddingModel.embedForResponse(List.of(text));
        Map embeddingValue = Map.of("embedding", embeddingResponse);
        return embeddingResponse.getResult().getOutput();
    }
}
