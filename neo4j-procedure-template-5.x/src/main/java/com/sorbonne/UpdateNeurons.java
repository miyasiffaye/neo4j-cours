package  com.sorbonne;

import java.util.stream.Stream;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;

public class UpdateNeurons {


    @Context
    public GraphDatabaseService db;

    public static class ResultMessage {
        public String message;
        public ResultMessage(String message) { this.message = message; }
    }

    @Procedure(name = "nn.updateNeurons", mode = Mode.WRITE)
    @Description("Met à jour les poids et biais des neurones avec Adam Optimizer")
    public Stream<ResultMessage> updateNeurons(
            @Name("t") long t,
            @Name("beta1") double beta1,
            @Name("beta2") double beta2,
            @Name("learning_rate") double learningRate,
            @Name("epsilon") double epsilon
    ) {
        try (Transaction tx = db.beginTx()) {
            tx.execute("MATCH (n:Neuron {type: 'hidden'})<-[:CONNECTED_TO]-(next:Neuron) " +
                    "WITH n, next, " + t + " AS t " +
                    "MATCH (n)-[r:CONNECTED_TO]->(next) " +
                    "WITH n, SUM(next.gradient * COALESCE(r.weight, 0)) AS raw_gradient, t " +
                    "WITH n, " +
                    "     CASE " +
                    "        WHEN n.activation_function = 'relu' THEN CASE WHEN n.output > 0 THEN raw_gradient ELSE 0 END " +
                    "        WHEN n.activation_function = 'sigmoid' THEN raw_gradient * n.output * (1 - n.output) " +
                    "        WHEN n.activation_function = 'tanh' THEN raw_gradient * (1 - n.output^2) " +
                    "        ELSE raw_gradient " +
                    "     END AS gradient, t " +
                    "MATCH (prev:Neuron)-[r_prev:CONNECTED_TO]->(n) " +
                    "SET r_prev.m = " + beta1 + " * COALESCE(r_prev.m, 0) + (1 - " + beta1 + ") * gradient * COALESCE(prev.output, 0) " +
                    "SET r_prev.v = " + beta2 + " * COALESCE(r_prev.v, 0) + (1 - " + beta2 + ") * (gradient * COALESCE(prev.output, 0))^2 " +
                    "SET r_prev.weight = r_prev.weight - " + learningRate + " * (r_prev.m / (1 - (" + beta1 + " ^ t))) / " +
                    "                    (SQRT(r_prev.v / (1 - (" + beta2 + " ^ t))) + " + epsilon + ") " +
                    "SET n.m_bias = " + beta1 + " * COALESCE(n.m_bias, 0) + (1 - " + beta1 + ") * gradient " +
                    "SET n.v_bias = " + beta2 + " * COALESCE(n.v_bias, 0) + (1 - " + beta2 + ") * (gradient^2) " +
                    "SET n.bias = n.bias - " + learningRate + " * (n.m_bias / (1 - (" + beta1 + " ^ t))) / " +
                    "             (SQRT(n.v_bias / (1 - (" + beta2 + " ^ t))) + " + epsilon + ") " +
                    "SET n.gradient = gradient " +
                    "RETURN COUNT(n) AS updatedNeurons");

            tx.commit();
            return Stream.of(new ResultMessage("Mise à jour des neurones effectuée avec succès."));
        } catch (Exception e) {
            return Stream.of(new ResultMessage("Erreur lors de la mise à jour des neurones : " + e.getMessage()));
        }
    }

}
