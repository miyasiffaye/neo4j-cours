package com.sorbonne;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Procedure;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class EvaluateModel {

    @Context
    public Log log;
    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.evaluateModel", mode = Mode.READ)
    @Description("Evaluate the model and return the predicted outputs of the neurons")
    public Stream<ModelEvaluation> evaluateModel() {
        try (Transaction tx = db.beginTx()) {
            Result result = tx.execute("MATCH (n:Neuron {type: 'output'}) RETURN n.id AS id, n.output AS predicted");

            Map<String, Double> predictions = new HashMap<>();
            while (result.hasNext()) {
                Map<String, Object> record = result.next();
                predictions.put((String) record.get("id"), (Double) record.get("predicted"));
            }

            return predictions.entrySet().stream().map(entry -> new ModelEvaluation(entry.getKey(), entry.getValue()));
        }
        catch (Exception e) {

            return Stream.of(new ModelEvaluation("ko", 12));
        }
    }


    public static class ModelEvaluation {
        public String id;
        public double predicted;

        public ModelEvaluation(String id, double predicted) {
            this.id = id;
            this.predicted = predicted;
        }
    }
}
