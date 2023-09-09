package com.distsystem.test.cache;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CacheStorageMongoSimpleTest {
    private static final Logger log = LoggerFactory.getLogger(CacheStorageMongoSimpleTest.class);

    @Test
    public void mongoSimpleTest() {
        log.info("START ------ ");
        String host = "localhost";
        int port = 8081;
        MongoClient mongo = new MongoClient( "localhost" , 27017 );
        MongoCredential credential = MongoCredential.createCredential("sampleUser", "myDb", "password".toCharArray());
        //mongo.listDatabaseNames();

        log.info("Mongo databases: " + mongo.listDatabaseNames());
        MongoDatabase database = mongo.getDatabase("distsystem");
        log.info("Mongo db: " + database.getName());
        database.createCollection("disttest");

        log.info("Mongo collection created");
        var distTestCollection = database.getCollection("disttest");
        List<Document> items = List.of(
                Map.of("type", "agent", "guid", "aaa111", "category", "c1"),
                Map.of("type", "agent", "guid", "bbb222", "category", "c1"),
                Map.of("type", "agent", "guid", "ccc333", "category", "c2"),
                Map.of("type", "agent", "guid", "ddd444", "category", "c2")
        ).stream().map(attrs -> {
            Document doc = new Document();
            doc.putAll(attrs);
            return doc;
        }).collect(Collectors.toList());

        distTestCollection.countDocuments();


        log.info("Documents to be inserted: " + items.size());
        distTestCollection.insertMany(items);

        log.info("Closing Mongo connection");
        mongo.close();
        log.info("END-----");
    }

}
