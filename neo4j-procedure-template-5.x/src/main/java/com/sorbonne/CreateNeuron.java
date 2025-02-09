package com.sorbonne;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.logging.Log;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

public class CreateNeuron {
    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/console.log`
    @Context
    public Log log;
    @Context
    public GraphDatabaseService db;

    // Exemple de base à lire completer corriger
    // Pour utiliser call nn.createNeuron("123","0","input","sotfmax")
    @Procedure(name = "nn.createNeuron", mode = Mode.WRITE)
    @Description("")
    public Stream<CreateResult> createNeuron(@Name("id") String id,
            @Name("layer") String layer,
            @Name("type") String type,
            @Name("activation_function") String activation_function) {
        try (Transaction tx = db.beginTx()) {

            tx.execute("CREATE (n:Neuron {\n" +
                    "id: '" + id + "',\n" +
                    "layer:" + layer + ",\n" +
                    "type: '" + type + "',\n" +
                    "bias: 0.0,\n" +
                    "output: null,\n" +
                    "m_bias: 0.0,\n" +
                    "v_bias: 0.0,\n" +
                    "activation_function:'" + activation_function + "'\n" +
                    "})");
            tx.commit();
            return Stream.of(new CreateResult("ok"));

        } catch (Exception e) {

            return Stream.of(new CreateResult("ko"));
        }
    }

    @Procedure(name = "nn.createRelationShipsNeuron", mode = Mode.WRITE)
    @Description("")
    public Stream<CreateResult> createRelationShipsNeuron(
            @Name("from_id") String from_id,
            @Name("to_id") String to_id,
            @Name("weight") String weight) {
        try (Transaction tx = db.beginTx()) {

            tx.execute(
                    "MATCH (n1:Neuron" + "{id:'" + from_id + "'})\n" +
                            "MATCH (n2:Neuron" + "{id:'" + to_id + "'})\n" +
                            "CREATE (n1)-[:CONNECTED_TO {weight:" + weight + "}]->(n2)");
            tx.commit();
            return Stream.of(new CreateResult("ok"));

        } catch (Exception e) {

            return Stream.of(new CreateResult("ko"));
        }
    }

    @Procedure(name = "nn.constrainWeights", mode = Mode.READ)
    @Description("Constrain the weights of relationships to be within the range [-1.0, 1.0]")
    public Stream<CreateResult> constrainWeights() {
        String query = "MATCH ()-[r:CONNECTED_TO]->() " +
                "SET r.weight = CASE " +
                "WHEN r.weight > 1.0 THEN 1.0 " +
                "WHEN r.weight < -1.0 THEN -1.0 " +
                "ELSE r.weight " +
                "END";
        try (Transaction tx = db.beginTx()) {
            tx.execute(query);
            tx.commit();
            return Stream.of(new CreateResult("ok"));
        }
    }

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
    }

    @Procedure(name = "nn.expectedOutput", mode = Mode.READ)
    @Description("Retrieve the expected outputs of the neurons")
    public Stream<ExpectedOutput> expectedOutput() {
        try (Transaction tx = db.beginTx()) {
            Result result = tx
                    .execute("MATCH (n:Neuron {type: 'output'}) RETURN n.id AS id, n.expected_output AS expected");

            Map<String, Double> expectedOutputs = new HashMap<>();
            while (result.hasNext()) {
                Map<String, Object> record = result.next();
                expectedOutputs.put((String) record.get("id"), (Double) record.get("expected"));
            }

            return expectedOutputs.entrySet().stream()
                    .map(entry -> new ExpectedOutput(entry.getKey(), entry.getValue()));
        }
    }

    public static class ExpectedOutput {
        public String id;
        public double expected;

        public ExpectedOutput(String id, double expected) {
            this.id = id;
            this.expected = expected;
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

    public static class CreateResult {

        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}