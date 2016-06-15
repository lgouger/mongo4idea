/*
 * Copyright (c) 2013 David Boissier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codinjutsu.tools.mongo.logic;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.util.JSON;
import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.codinjutsu.tools.mongo.ServerConfiguration;
import org.codinjutsu.tools.mongo.model.MongoCollection;
import org.codinjutsu.tools.mongo.model.MongoCollectionResult;
import org.codinjutsu.tools.mongo.model.MongoQueryOptions;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class MongoManagerTest {

    private MongoManager mongoManager;
    private ServerConfiguration serverConfiguration;


    @Test
    public void loadCollectionsWithEmptyFilter() throws Exception {
        MongoQueryOptions mongoQueryOptions = new MongoQueryOptions();
        mongoQueryOptions.setResultLimit(3);
        MongoCollectionResult mongoCollectionResult = mongoManager.loadCollectionValues(serverConfiguration, new MongoCollection("dummyCollection", "test"), mongoQueryOptions);
        assertNotNull(mongoCollectionResult);
        assertEquals(3, mongoCollectionResult.getMongoObjects().size());
    }

    @Test
    public void loadCollectionsWithFilterAndProjection() throws Exception {
        MongoQueryOptions mongoQueryOptions = new MongoQueryOptions();
        mongoQueryOptions.setFilter("{\"label\":\"tata\"}");
        mongoQueryOptions.setProjection("{\"label\":1, \"_id\": 0}");
        mongoQueryOptions.setResultLimit(3);
        MongoCollectionResult mongoCollectionResult = mongoManager.loadCollectionValues(serverConfiguration, new MongoCollection("dummyCollection", "test"), mongoQueryOptions);
        assertNotNull(mongoCollectionResult);
        assertEquals(2, mongoCollectionResult.getMongoObjects().size());
        assertEquals("[{ \"label\" : \"tata\"}, { \"label\" : \"tata\"}]", mongoCollectionResult.getMongoObjects().toString());
    }

    @Test
    public void loadCollectionsWithFilterAndProjectionAndSortByPrice() throws Exception {
        MongoQueryOptions mongoQueryOptions = new MongoQueryOptions();
        mongoQueryOptions.setFilter("{\"label\":\"tata\"}");
        mongoQueryOptions.setProjection("{\"label\": 1, \"_id\": 0, \"price\": 1}");
        mongoQueryOptions.setSort("{\"price\": 1}");
        mongoQueryOptions.setResultLimit(3);
        MongoCollectionResult mongoCollectionResult = mongoManager.loadCollectionValues(serverConfiguration, new MongoCollection("dummyCollection", "test"), mongoQueryOptions);
        assertNotNull(mongoCollectionResult);
        assertEquals(2, mongoCollectionResult.getMongoObjects().size());
        assertEquals("[{ \"label\" : \"tata\" , \"price\" : 10}, { \"label\" : \"tata\" , \"price\" : 15}]", mongoCollectionResult.getMongoObjects().toString());
    }

    @Test
    public void updateMongoDocument() throws Exception {
        MongoQueryOptions mongoQueryOptions = new MongoQueryOptions();
        mongoQueryOptions.setFilter("{'label': 'tete'}");
        MongoCollection mongoCollection = new MongoCollection("dummyCollection", "test");
        MongoCollectionResult initialData = mongoManager.loadCollectionValues(serverConfiguration, mongoCollection, mongoQueryOptions);
        assertEquals(1, initialData.getMongoObjects().size());
        DBObject initialMongoDocument = initialData.getMongoObjects().get(0);

        initialMongoDocument.put("price", 25);
        mongoManager.update(serverConfiguration, mongoCollection, initialMongoDocument);

        MongoCollectionResult updatedResult = mongoManager.loadCollectionValues(serverConfiguration, mongoCollection, mongoQueryOptions);
        List<DBObject> updatedMongoDocuments = updatedResult.getMongoObjects();
        assertEquals(1, updatedMongoDocuments.size());
        DBObject updatedMongoDocument = updatedMongoDocuments.get(0);

        assertEquals(25, updatedMongoDocument.get("price"));
    }


    @Test
    public void deleteMongoDocument() throws Exception {
        MongoQueryOptions mongoQueryOptions = new MongoQueryOptions();
        mongoQueryOptions.setFilter("{'label': 'tete'}");
        MongoCollection mongoCollection = new MongoCollection("dummyCollection", "test");
        MongoCollectionResult initialData = mongoManager.loadCollectionValues(serverConfiguration, mongoCollection, mongoQueryOptions);
        assertEquals(1, initialData.getMongoObjects().size());
        DBObject initialMongoDocument = initialData.getMongoObjects().get(0);

        mongoManager.delete(serverConfiguration, mongoCollection, initialMongoDocument.get("_id"));

        MongoCollectionResult deleteResult = mongoManager.loadCollectionValues(serverConfiguration, mongoCollection, mongoQueryOptions);
        List<DBObject> updatedMongoDocuments = deleteResult.getMongoObjects();
        assertEquals(0, updatedMongoDocuments.size());
    }


    @Test
    public void loadCollectionsWithAggregateOperators() throws Exception {
        MongoQueryOptions mongoQueryOptions = new MongoQueryOptions();
        mongoQueryOptions.setOperations("[{'$match': {'price': 15}}, {'$project': {'label': 1, 'price': 1}}, {'$group': {'_id': '$label', 'total': {'$sum': '$price'}}}]");
        MongoCollectionResult mongoCollectionResult = mongoManager.loadCollectionValues(serverConfiguration, new MongoCollection("dummyCollection", "test"), mongoQueryOptions);
        assertNotNull(mongoCollectionResult);

        List<DBObject> mongoObjects = mongoCollectionResult.getMongoObjects();

        assertEquals(2, mongoObjects.size());
        assertEquals("{ \"_id\" : \"tutu\" , \"total\" : 15}", mongoObjects.get(0).toString());
        assertEquals("{ \"_id\" : \"tata\" , \"total\" : 15}", mongoObjects.get(1).toString());
    }

    @Before
    public void setUp() throws Exception {
        MongoClient mongo = new MongoClient("localhost:27017");
        MongoDatabase db = mongo.getDatabase("test");

        com.mongodb.client.MongoCollection<Document> dummyCollection = db.getCollection("dummyCollection");
        dummyCollection.deleteMany(new BasicDBObject());
        fillCollectionWithJsonData(dummyCollection, IOUtils.toString(getClass().getResourceAsStream("dummyCollection.json")));

        mongoManager = new MongoManager();
        serverConfiguration = new ServerConfiguration();
        serverConfiguration.setServerUrls(Arrays.asList("localhost:27017"));
    }

    private static void fillCollectionWithJsonData(com.mongodb.client.MongoCollection<Document> collection, String jsonResource) throws IOException {
        Object jsonParsed = JSON.parse(jsonResource);
        if (jsonParsed instanceof BasicDBList) {
            BasicDBList jsonObject = (BasicDBList) jsonParsed;
            for (Object o : jsonObject) {
                DBObject dbObject = (DBObject) o;
                Document document = new Document();
                for (String key : dbObject.keySet()) {
                    document.append(key, dbObject.get(key));
                }
                collection.insertOne(document);
            }
        } else {
            DBObject dbObject = (DBObject) jsonParsed;
            Document document = new Document();
            for (String key : dbObject.keySet()) {
                document.append(key, dbObject.get(key));
            }
            collection.insertOne(document);
        }
    }
}

