package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class CreateConnection {
    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.createConnection",mode = Mode.WRITE)
    @Description("Crée une connexion entre deux neurones avec un poids donné.")
    public Stream<CreateResult> createConnection(@Name("from_id") String fromId,
                                                 @Name("to_id") String toId,
                                                 @Name("weight") Double weight) {
        try (Transaction tx = db.beginTx()) {
            Node neuron1 = tx.findNode(Label.label("Neuron"), "id", fromId);
            Node neuron2 = tx.findNode(Label.label("Neuron"), "id", toId);

            if (neuron1 == null) {
                return Stream.of(new CreateResult("Error: Source neuron was not found."));
            }
            if (neuron2 == null) {
                return Stream.of(new CreateResult("Error: Target neuron was not found."));
            }

            Relationship rel = neuron1.createRelationshipTo(neuron2, RelationshipType.withName("CONNECTED_TO"));
            rel.setProperty("weight", weight);
            tx.commit();

            return Stream.of(new CreateResult("Success"));
        } catch (Exception e) {
            return Stream.of(new CreateResult("Error: " + e.getMessage()));
        }
    }

    public static class CreateResult {
        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}
