package com.yash.genAI_2.service;

import com.yash.genAI_2.model.Query;
import com.yash.genAI_2.utils.Constants;
import io.qdrant.client.grpc.Points;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class ChatService {

    OllamaApi ollamaApi = new OllamaApi();
    @Autowired
    QdrantService qdrantService;

    OllamaChatModel chatModel = OllamaChatModel.builder()
            .ollamaApi(ollamaApi)
            .defaultOptions(
                    OllamaOptions.builder()
                            .model(Constants.MODEL_NAME)
                            .temperature(0.9)
                            .build())
            .build();

    public String generateRes(Query query) throws ExecutionException, InterruptedException {

        List<Points.ScoredPoint> points =  qdrantService.searchPayload(query);
        System.out.println("points: "+ points);
        StringBuilder contextBuilder = new StringBuilder();
        for (Points.ScoredPoint point : points) {
            String sourceText =  point.getPayloadMap().get("text").getStringValue();
            contextBuilder.append("- ").append(sourceText).append("\n");
        }
        String finalPrompt = getString(query, contextBuilder);
        return this.chatModel.call(finalPrompt);
    }

//    private static String getString(Query query, StringBuilder contextBuilder) {
//        String sourcesContext = contextBuilder.toString();
//
//        String finalPrompt = """
//                You are a helpful assistant. Use the following **sources** to answer the question as clearly and accurately as possible.
//               \s
//                 Only use information that is present in the sources.
//                 Use your language skills to explain the answer naturally.
//                 Do not make up facts or assume anything that is not supported by the sources.
//                 If the sources do not contain enough information, respond with:
//                 "I could not find enough information in the provided sources to answer the question confidently."
//                   \s
//                    Sources:
//                    %s
//                   \s
//                    Question: %s
//                   \s""".formatted(sourcesContext, query.query);
//        return finalPrompt;
//    }
private static String getString(Query query, StringBuilder contextBuilder) {
    String sourcesContext = contextBuilder.toString();

    String finalPrompt = """
            You are an AI assistant that answers employee questions based strictly on the company's official policies and guidelines.

            - Use only the information provided in the sources below.
            - If the answer is not clearly stated in the sources, respond with:
              "I could not find enough information in the provided company policies to answer this question confidently."
            - Do not make assumptions or invent policy details.
            - Use a clear, helpful, and professional tone.

            Sources:
            %s

            Question: %s
            """.formatted(sourcesContext, query.query);

    return finalPrompt;
}

}
