package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;
import java.util.HashMap;
import java.util.Map;

public class CreateHiddenBackwardPass {
    @Context
    public Log log; //permet de loguer des messages
    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.createHiddenBackwardPass", mode = Mode.WRITE)
    @Description("Update hidden layers")
    public Stream<CreateHiddenBackwardPass.CreateResult> createHiddenBackwardPass(
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
            String cypherQuery = "MATCH (n:Neuron {type: 'hidden'})<-[:CONNECTED_TO]-(next:Neuron)\n" +
                    "            WITH n, next, $t AS t\n" +
                    "            MATCH (n)-[r:CONNECTED_TO]->(next)\n" +
                    "            WITH n, SUM(next.gradient * COALESCE(r.weight, 0)) AS raw_gradient, t\n" +
                    "            WITH n,\n" +
                    "                 CASE \n" +
                    "                     WHEN n.activation_function = 'relu' THEN CASE WHEN n.output > 0 THEN raw_gradient ELSE 0 END\n" +
                    "                     WHEN n.activation_function = 'sigmoid' THEN raw_gradient * n.output * (1 - n.output)\n" +
                    "                     WHEN n.activation_function = 'tanh' THEN raw_gradient * (1 - n.output^2)\n" +
                    "                     ELSE raw_gradient  // For linear activation\n" +
                    "                 END AS gradient, t\n" +
                    "            MATCH (prev:Neuron)-[r_prev:CONNECTED_TO]->(n)\n" +
                    "            SET r_prev.m = $beta1 * COALESCE(r_prev.m, 0) + (1 - $beta1) * gradient * COALESCE(prev.output, 0)\n" +
                    "            SET r_prev.v = $beta2 * COALESCE(r_prev.v, 0) + (1 - $beta2) * (gradient * COALESCE(prev.output, 0))^2\n" +
                    "            SET r_prev.weight = r_prev.weight - $learning_rate * (r_prev.m / (1 - ($beta1 ^ t))) / \n" +
                    "                                (SQRT(r_prev.v / (1 - ($beta2 ^ t))) + $epsilon)\n" +
                    "            SET n.m_bias = $beta1 * COALESCE(n.m_bias, 0) + (1 - $beta1) * gradient\n" +
                    "            SET n.v_bias = $beta2 * COALESCE(n.v_bias, 0) + (1 - $beta2) * (gradient^2)\n" +
                    "            SET n.bias = n.bias - $learning_rate * (n.m_bias / (1 - ($beta1 ^ t))) / \n" +
                    "                         (SQRT(n.v_bias / (1 - ($beta2 ^ t))) + $epsilon)\n" +
                    "            SET n.gradient = gradient";

            // Exécution de la requête Cypher avec les paramètres
            tx.execute(cypherQuery, params);

            tx.commit(); // Valider la transaction

            return Stream.of(new CreateHiddenBackwardPass.CreateResult("ok"));

        } catch (Exception e) {
            // Loguer l'erreur pour pouvoir la diagnostiquer
            log.error("Error in createHiddenBackwardPass procedure: " + e.getMessage(), e);
            return Stream.of(new CreateHiddenBackwardPass.CreateResult("ko"));
        }
    }

    public static class CreateResult {
        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}
