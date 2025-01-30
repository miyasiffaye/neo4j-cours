package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class CreateOuptputBackwardPass {
    @Context
    public Log log; //permet de loguer des messages
    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.createOuptputBackwardPass",mode = Mode.WRITE)
    //mode WRITE car va modifier la base de données
    @Description("Update outputs layers")
    public Stream<CreateOuptputBackwardPass.CreateResult> createOuptputBackwardPass(@Name("learning_rate") int learning_rate,
                                                                                    @Name("beta1") int beta1,
                                                                                    @Name("beta2") int beta2,
                                                                                    @Name("epsilon") int epsilon,
                                                                                    @Name("t") int t

    ) {

        /*Version proposé par l'enseignant*/
        try (Transaction tx = db.beginTx()) {

            tx.execute("MATCH (output:Neuron {type: 'output'})<-[r:CONNECTED_TO]-(prev:Neuron)\n" +
                    "            MATCH (output)-[outputsValues_R:CONTAINS]->(row_for_outputs:Row {type: 'outputsRow'})\n" +
                    "            WITH DISTINCT output,r,prev,outputsValues_R,row_for_outputs,\n" +
                    "                 CASE \n" +
                    "                     WHEN output.activation_function = 'softmax' THEN outputsValues_R.output - outputsValues_R.expected_output\n" +
                    "                     WHEN output.activation_function = 'sigmoid' THEN (outputsValues_R.output - outputsValues_R.expected_output) * outputsValues_R.output * (1 - outputsValues_R.output)\n" +
                    "                     WHEN output.activation_function = 'tanh' THEN (outputsValues_R.output - outputsValues_R.expected_output) * (1 - outputsValues_R.output^2)\n" +
                    "                     ELSE outputsValues_R.output - outputsValues_R.expected_output  //For linear activation\n" +
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
                    "            SET output.gradient = gradient");
            return Stream.of(new CreateOuptputBackwardPass.CreateResult("ok"));

        } catch (Exception e) {

            return Stream.of(new CreateOuptputBackwardPass.CreateResult("ko"));
        }
    }

    public static class CreateResult {

        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}
