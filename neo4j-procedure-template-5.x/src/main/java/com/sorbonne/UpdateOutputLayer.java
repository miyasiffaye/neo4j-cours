package com.sorbonne;
import org.neo4j.graphdb.*;
//import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;
public class UpdateOutputLayer {
    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.updateOutputLayer", mode = Mode.WRITE)
    @Description("Met à jour les poids et les biais des neurones de sortie avec Adam Optimizer.")
    public Stream<ResultMessage> updateOutputLayer(
            @Name("t") long t,
            @Name("beta1") double beta1,
            @Name("beta2") double beta2,
            @Name("learning_rate") double learningRate,
            @Name("epsilon") double epsilon
    ) {
        try (Transaction tx = db.beginTx()) {
            String query = "MATCH (output:Neuron {type: 'output'})<-[r:CONNECTED_TO]-(prev:Neuron) " +
                    "MATCH (output)-[outputsValues_R:CONTAINS]->(row_for_outputs:Row {type: 'outputsRow'}) " +
                    "WITH DISTINCT output, r, prev, outputsValues_R, row_for_outputs, " +
                    "     CASE " +
                    "         WHEN output.activation_function = 'softmax' THEN outputsValues_R.output - outputsValues_R.expected_output " +
                    "         WHEN output.activation_function = 'sigmoid' THEN (outputsValues_R.output - outputsValues_R.expected_output) * outputsValues_R.output * (1 - outputsValues_R.output) " +
                    "         WHEN output.activation_function = 'tanh' THEN (outputsValues_R.output - outputsValues_R.expected_output) * (1 - outputsValues_R.output^2) " +
                    "         ELSE outputsValues_R.output - outputsValues_R.expected_output " + // Linear activation
                    "     END AS gradient, " + t + " AS t " +
                    "MATCH (prev)-[r:CONNECTED_TO]->(output) " +
                    "SET r.m = " + beta1 + " * COALESCE(r.m, 0) + (1 - " + beta1 + ") * gradient * COALESCE(prev.output, 0) " +
                    "SET r.v = " + beta2 + " * COALESCE(r.v, 0) + (1 - " + beta2 + ") * (gradient * COALESCE(prev.output, 0))^2 " +
                    "SET r.weight = r.weight - " + learningRate + " * (r.m / (1 - (" + beta1 + " ^ " + t + "))) / " +
                    "               (SQRT(r.v / (1 - (" + beta2 + " ^ " + t + "))) + " + epsilon + ") " +
                    "SET output.m_bias = " + beta1 + " * COALESCE(output.m_bias, 0) + (1 - " + beta1 + ") * gradient " +
                    "SET output.v_bias = " + beta2 + " * COALESCE(output.v_bias, 0) + (1 - " + beta2 + ") * (gradient^2) " +
                    "SET output.bias = output.bias - " + learningRate + " * (output.m_bias / (1 - (" + beta1 + " ^ " + t + "))) / " +
                    "                 (SQRT(output.v_bias / (1 - (" + beta2 + " ^ " + t + "))) + " + epsilon + ") " +
                    "SET output.gradient = gradient";

            tx.execute(query);
            tx.commit();
            return Stream.of(new ResultMessage("Mise à jour des neurones de sortie effectuée avec succès."));

        } catch (Exception e) {
            return Stream.of(new ResultMessage("Erreur lors de la mise à jour des neurones de sortie : " + e.getMessage()));
        }
    }

    public static class ResultMessage {
        public final String message;

        public ResultMessage(String message) {
            this.message = message;
        }
    }
}
