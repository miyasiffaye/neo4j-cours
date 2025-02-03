package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;
import java.util.HashMap;
import java.util.Map;

public class CreateOutputBackwardPass {
    @Context
    public Log log; //permet de loguer des messages
    @Context
    public GraphDatabaseService db; // référence à la base de données Neo4j

    @Procedure(name = "nn.createOutputBackwardPass", mode = Mode.WRITE)
    @Description("Update outputs layers")
    public Stream<CreateOutputBackwardPass.CreateResult> createOutputBackwardPass(
            @Name("learning_rate") long learning_rate,
            @Name("beta1") long beta1,
            @Name("beta2") long beta2,
            @Name("epsilon") long epsilon,
            @Name("t") long t
    ) {
        try (Transaction tx = db.beginTx()) {

            // Création d'un Map pour les paramètres
            Map<String, Object> params = new HashMap<>();
            params.put("learning_rate", learning_rate);
            params.put("beta1", beta1);
            params.put("beta2", beta2);
            params.put("epsilon", epsilon);
            params.put("t", t);

            // Requête Cypher avec les paramètres
            String cypherQuery = "MATCH (output:Neuron {type: 'output'})<-[r:CONNECTED_TO]-(prev:Neuron)\n" +
                    "            MATCH (output)-[outputsValues_R:CONTAINS]->(row_for_outputs:Row {type: 'outputsRow'})\n" +
                    "            WITH DISTINCT output, r, prev, outputsValues_R, row_for_outputs,\n" +
                    "                 CASE \n" +
                    "                     WHEN output.activation_function = 'softmax' THEN outputsValues_R.output - outputsValues_R.expected_output\n" +
                    "                     WHEN output.activation_function = 'sigmoid' THEN (outputsValues_R.output - outputsValues_R.expected_output) * outputsValues_R.output * (1 - outputsValues_R.output)\n" +
                    "                     WHEN output.activation_function = 'tanh' THEN (outputsValues_R.output - outputsValues_R.expected_output) * (1 - outputsValues_R.output^2)\n" +
                    "                     ELSE outputsValues_R.output - outputsValues_R.expected_output  // For linear activation\n" +
                    "                 END AS gradient,\n" +
                    "                 $t AS t\n" +
                    "            MATCH (prev)-[r:CONNECTED_TO]->(output)\n" +
                    "            SET r.m = $beta1 * COALESCE(r.m, 0) + (1 - $beta1) * gradient * COALESCE(prev.output, 0)\n" +
                    "            SET r.v = $beta2 * COALESCE(r.v, 0) + (1 - $beta2) * (gradient * COALESCE(prev.output, 0))^2\n" +
                    "            SET r.weight = r.weight - $learning_rate * (r.m / (1 - ($beta1 ^ t))) / \n" +
                    "                           (SQRT(r.v / (1 - ($beta2 ^ t))) + $epsilon)\n" +
                    "            SET output.m_bias = $beta1 * COALESCE(output.m_bias, 0) + (1 - $beta1) * gradient\n" +
                    "            SET output.v_bias = $beta2 * COALESCE(output.v_bias, 0) + (1 - $beta2) * (gradient^2)\n" +
                    "            SET output.bias = output.bias - $learning_rate * (output.m_bias / (1 - ($beta1 ^ t))) / \n" +
                    "                         (SQRT(output.v_bias / (1 - ($beta2 ^ t))) + $epsilon)\n" +
                    "            SET output.gradient = gradient";

            // Exécution de la requête Cypher avec les paramètres
            tx.execute(cypherQuery, params);

            tx.commit(); // Valider la transaction

            return Stream.of(new CreateOutputBackwardPass.CreateResult("ok"));

        } catch (Exception e) {
            // Loguer l'erreur pour pouvoir la diagnostiquer
            log.error("Error in createOutputBackwardPass procedure: " + e.getMessage(), e);
            return Stream.of(new CreateOutputBackwardPass.CreateResult("ko"));
        }
    }

    public static class CreateResult {
        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}
