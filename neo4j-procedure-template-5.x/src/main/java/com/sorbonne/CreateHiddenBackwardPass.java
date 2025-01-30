package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class CreateHiddenBackwardPass {
    @Context
    public Log log; //permet de loguer des messages
    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.createHiddenBackwardPass",mode = Mode.WRITE)
    //mode WRITE car va modifier la base de données
    @Description("Update hidden layers")
    public Stream<CreateHiddenBackwardPass.CreateResult> createHiddenBackwardPass(@Name("learning_rate") int learning_rate,
                                                                                    @Name("beta1") int beta1,
                                                                                    @Name("beta2") int beta2,
                                                                                    @Name("epsilon") int epsilon,
                                                                                    @Name("t") int t

    ) {

        /*Version proposé par l'enseignant*/
        try (Transaction tx = db.beginTx()) {

            tx.execute("MATCH (n:Neuron {type: 'hidden'})<-[:CONNECTED_TO]-(next:Neuron)\n" +
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
                    "            SET n.gradient = gradient");
            return Stream.of(new CreateHiddenBackwardPass.CreateResult("ok"));

        } catch (Exception e) {

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
