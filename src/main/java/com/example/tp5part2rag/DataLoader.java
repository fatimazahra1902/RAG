package com.example.tp5part2rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

@Component
public class DataLoader {
    @Value("classpath:/pdfs/cv.pdf")
    private Resource pdfFile;
    @Value("vectorestore.json")
    private String vectorStoreName;

    private  static Logger log = LoggerFactory.getLogger(DataLoader.class);

    @Bean
    public SimpleVectorStore simpleVectorStore(EmbeddingModel embeddingModel){
        SimpleVectorStore simpleVectorStore = new SimpleVectorStore(embeddingModel);
        String path = Path.of("src", "main","resources","vectorstore").toFile().getAbsolutePath()+"/"+vectorStoreName;
        File fileStore = new File(path);
        if(fileStore.exists()){
            log.info("Vectore store exist => " + path);
            simpleVectorStore.load(fileStore);
        }else{
            // Lecture du PDF et récupération des documents
            PagePdfDocumentReader documentReader = new PagePdfDocumentReader(pdfFile);
            List<Document> documents = documentReader.get();

            // Division du texte en segments plus petits
            TextSplitter textSplitter = new TokenTextSplitter();
            List<Document> chunks = textSplitter.split(documents);

            // Ajout des segments (convertis en vecteurs) au vector store
            simpleVectorStore.add(chunks);

            // Sauvegarde du vector store sur le disque
            simpleVectorStore.save(fileStore);

        }
        return simpleVectorStore;
        }
}
