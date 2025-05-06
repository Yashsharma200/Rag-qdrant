package com.yash.genAI_2.controller;

//import com.sun.tools.javac.util.List;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.yash.genAI_2.model.Query;
import com.yash.genAI_2.service.ChatService;
import com.yash.genAI_2.service.EmbeddingService;
import com.yash.genAI_2.service.QdrantService;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.WithVectorsSelectorFactory;
import io.qdrant.client.grpc.Points;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.yash.genAI_2.service.QdrantService.COLLECTION_NAME;
import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.QueryFactory.nearest;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;
import static io.qdrant.client.WithPayloadSelectorFactory.enable;


@RestController
public class EmbeddingController {
    List<String> texts = Arrays.asList(
            "The Earth revolves around the Sun once every 365.25 days.",
            "The Great Wall of China is over 13,000 miles long.",
            "Water boils at 100 degrees Celsius at sea level.",
            "The capital of France is Paris.",
            "Albert Einstein developed the theory of relativity.",
            "Mount Everest is the highest mountain above sea level.",
            "The human body has 206 bones.",
            "Photosynthesis allows plants to convert sunlight into energy.",
            "The Internet was first developed as a military project called ARPANET.",
            "Shakespeare wrote more than 30 plays and 150 sonnets.",
            "The speed of light is approximately 299,792 kilometers per second.",
            "The Pacific Ocean is the largest ocean on Earth.",
            "Electricity is the flow of electrons through a conductor.",
            "The Pyramids of Giza are located in Egypt.",
            "The moon affects ocean tides due to gravitational pull.",
            "DNA stands for deoxyribonucleic acid.",
            "The Amazon Rainforest is known as the lungs of the Earth.",
            "The first human on the Moon was Neil Armstrong in 1969.",
            "A leap year occurs every four years to keep the calendar in sync.",
            "The Mona Lisa was painted by Leonardo da Vinci."
    );

    private final EmbeddingModel embeddingModel;
    private final QdrantClient qdrantClient;
    @Autowired
    EmbeddingService embeddingService;

    @Autowired
    ChatService chatService;

    @Autowired
    QdrantService qdrantService;
    public EmbeddingController(QdrantClient qdrantClient, EmbeddingModel embeddingModel) {
        this.qdrantClient = qdrantClient;
        this.embeddingModel = embeddingModel;
    }


    @GetMapping("/ai/test-embedding")
    public Map embed(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        System.out.println("message:"+ message);
        EmbeddingResponse embeddingResponse = this.embeddingModel.embedForResponse(List.of(message));
        return Map.of("embedding", embeddingResponse);

    }

    @GetMapping("/ai/ingest")
    public String ingestText() throws ExecutionException, InterruptedException {
        System.out.println("🚀 Checking/creating Qdrant collection...");

        if (qdrantClient.collectionExistsAsync(COLLECTION_NAME).get()) {
            System.out.println("✅ Collection exists: " + COLLECTION_NAME);

            for (int i = 0; i < texts.size(); i++) {
                float[] embedding = embeddingService.generateEmbedding(texts.get(i));
                qdrantClient
                        .upsertAsync(
                                COLLECTION_NAME,
                                List.of(
                                        Points.PointStruct.newBuilder()
                                                .setId(id(i))
                                                .setVectors(vectors(embedding))
                                                .putAllPayload(Map.of("text", value(texts.get(i))))
                                                .build()))
                        .get();
            }

        } else {
            return "❌ Collection '" + COLLECTION_NAME + "' does not exist.";
        }
        return "Content ingested successfully";
    }

    @GetMapping("/ai/ingest-pdf")
    public String ingestPDFText() throws IOException, ExecutionException, InterruptedException {

        String [] filePath = new String[]{"1.pdf, 2.pdf. 3.pdf"};

        if (qdrantClient.collectionExistsAsync(COLLECTION_NAME).get()) {
            System.out.println("✅ Collection exists: " + COLLECTION_NAME);

            for (int i = 0; i < filePath.length; i++) {
                StringBuilder text = new StringBuilder();
                PdfReader reader = new PdfReader(filePath[i]);
                int pages = reader.getNumberOfPages();

                for (int j = 1; j <= pages; j++) {
                    text.append(PdfTextExtractor.getTextFromPage(reader, j));
                }

                reader.close();

                float[] embedding = embeddingService.generateEmbedding(text.toString());
                qdrantClient
                        .upsertAsync(
                                COLLECTION_NAME,
                                List.of(
                                        Points.PointStruct.newBuilder()
                                                .setId(id(i))
                                                .setVectors(vectors(embedding))
                                                .putAllPayload(Map.of("text", value(text.toString())))
                                                .build()))
                        .get();
            }

        } else {
            return "❌ Collection '" + COLLECTION_NAME + "' does not exist.";
        }
        return "Content ingested successfully";
    }
}