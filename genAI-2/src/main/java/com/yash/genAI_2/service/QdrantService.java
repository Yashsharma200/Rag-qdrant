package com.yash.genAI_2.service;

import com.yash.genAI_2.model.Query;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Points;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static io.qdrant.client.QueryFactory.nearest;
import static io.qdrant.client.WithPayloadSelectorFactory.enable;


@Service
public class QdrantService {


    private final QdrantClient qdrantClient;

    private final EmbeddingService embeddingService;

    public  QdrantService(QdrantClient qdrantClient, EmbeddingService embeddingService){
        this.qdrantClient = qdrantClient;
        this.embeddingService  = embeddingService;
    }




    public List<String> getAllCollectionNames() throws ExecutionException, InterruptedException {
        return qdrantClient.listCollectionsAsync().get();
    }

    public static final String COLLECTION_NAME = "global-policies";

    @PostConstruct
    public void initCollection() throws ExecutionException, InterruptedException {
        System.out.println("Checking/creating Qdrant collection...");

        if (qdrantClient.collectionExistsAsync(COLLECTION_NAME).get()) {
            System.out.println("Collection already exists: " + COLLECTION_NAME);
        } else {
            System.out.println("Creating collection: " + COLLECTION_NAME);

            Collections.VectorParams vectorParams = Collections.VectorParams.newBuilder()
                    .setSize(1024)
                    .setDistance(Collections.Distance.Cosine)
                    .build();

            Collections.CreateCollection createRequest = Collections.CreateCollection.newBuilder()
                    .setCollectionName(COLLECTION_NAME)
                    .setVectorsConfig(Collections.VectorsConfig.newBuilder()
                            .setParams(vectorParams)
                            .build())
                    .build();

            qdrantClient.createCollectionAsync(createRequest).get();
            System.out.println("✅ Collection created: " + COLLECTION_NAME);
        }
    }

    public List<Points.ScoredPoint>  searchPayload(Query queryObject ) throws ExecutionException, InterruptedException {
        float[] queryEmbedding = embeddingService.generateEmbedding(queryObject.query);
//        System.out.println(Arrays.toString(queryEmbedding));
        List<Points.ScoredPoint>  points = qdrantClient.queryAsync(Points.QueryPoints.newBuilder()
                .setCollectionName(COLLECTION_NAME)
                .setQuery(nearest(queryEmbedding))
                .setParams(Points.SearchParams.newBuilder().setExact(true).setHnswEf(128).build())
                .setWithPayload(enable(true))
                .setLimit(3)
                .build()).get();
        return points;
    }
}
