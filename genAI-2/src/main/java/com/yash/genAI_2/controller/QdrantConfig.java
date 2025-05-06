package com.yash.genAI_2.controller;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QdrantConfig {

    @Bean
    public QdrantClient qdrantClient(){
        System.out.println(" Creating QdrantClient from QdrantConfig...");
        return new QdrantClient(QdrantGrpcClient.newBuilder("localhost", 6334, false).build());
    }


}
