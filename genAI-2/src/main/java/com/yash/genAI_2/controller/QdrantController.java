package com.yash.genAI_2.controller;

import com.yash.genAI_2.service.QdrantService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("qdrant")
public class QdrantController {

    private final QdrantService qdrantService;

    public QdrantController( QdrantService qdrantService) {
        this.qdrantService = qdrantService;
    }

    @GetMapping("get-collections")
    public List<String> getCollectionList () throws ExecutionException, InterruptedException {
        return qdrantService.getAllCollectionNames();
    }
}

