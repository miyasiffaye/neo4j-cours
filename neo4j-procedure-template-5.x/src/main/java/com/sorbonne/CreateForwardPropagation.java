package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

public class CreateForwardPropagation {
    @Context
    public Log log; //permet de loguer des messages
    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.createForwardPropagation",mode = Mode.WRITE)
    //mode WRITE car va modifier la base de données
    @Description("Creates forward propagation across the neural network")
    public Stream<CreateForwardPropagation.CreateResult> createForwardPropagation(
    ) {

        /*Version proposé par l'enseignant*/
        try (Transaction tx = db.beginTx()) {

            tx.execute(" MATCH (row_for_inputs:Row {type: 'inputsRow'})-[inputsValue_R:CONTAINS]->(input:Neuron {type: 'input'})\n" +
                    "            MATCH (input)-[r1:CONNECTED_TO]->(hidden:Neuron {type: 'hidden'})\n" +
                    "            MATCH (hidden)-[r2:CONNECTED_TO]->(output:Neuron {type: 'output'})\n" +
                    "            MATCH (output)-[outputsValues_R:CONTAINS]->(row_for_outputs:Row {type: 'outputsRow'})\n" +
                    "            WITH DISTINCT row_for_inputs,inputsValue_R, input,r1,hidden,r2,output ,outputsValues_R,row_for_outputs,\n" +
                    "            \n" +
                    "            SUM(COALESCE(outputsValues_R.output, 0) * r1.weight) AS weighted_sum\n" +
                    "            SKIP 0 LIMIT 1000\n" +
                    "            SET hidden.output = CASE \n" +
                    "                WHEN hidden.activation_function = 'relu' THEN CASE WHEN (weighted_sum + hidden.bias) > 0 THEN (weighted_sum + hidden.bias) ELSE 0 END\n" +
                    "                WHEN hidden.activation_function = 'sigmoid' THEN 1 / (1 + EXP(-(weighted_sum + hidden.bias)))\n" +
                    "                WHEN hidden.activation_function = 'tanh' THEN (EXP(2 * (weighted_sum + hidden.bias)) - 1) / (EXP(2 * (weighted_sum + hidden.bias)) + 1)\n" +
                    "                ELSE weighted_sum + hidden.bias\n" +
                    "            END\n" +
                    "\t\t\t\n" +
                    "\t        WITH row_for_inputs,inputsValue_R, input,r1,hidden,r2,output ,outputsValues_R,row_for_outputs,\n" +
                    "\t        SUM(COALESCE(hidden.output, 0) * r2.weight) AS weighted_sum\n" +
                    "            SET outputsValues_R.output = CASE \n" +
                    "                WHEN output.activation_function = 'softmax' THEN weighted_sum  //Temporary value; softmax applied later\n" +
                    "                WHEN output.activation_function = 'sigmoid' THEN 1 / (1 + EXP(-(weighted_sum + output.bias)))\n" +
                    "                WHEN output.activation_function = 'tanh' THEN (EXP(2 * (weighted_sum + output.bias)) - 1) / (EXP(2 * (weighted_sum + output.bias)) + 1)\n" +
                    "                ELSE weighted_sum + output.bias\n" +
                    "            END\n" +
                    "\t        WITH COLLECT(output) AS output_neurons, COLLECT(outputsValues_R) AS outputsValues_Rs\n" +
                    "               WITH output_neurons, outputsValues_Rs,\n" +
                    "                    [n IN outputsValues_Rs | exp(COALESCE(n.output, 0))] AS exp_outputs,\n" +
                    "                    [n IN output_neurons | n.activation_function] AS activation_functions\n" +
                    "               WITH output_neurons, outputsValues_Rs, exp_outputs, activation_functions, \n" +
                    "                    REDUCE(sum = 0.0, x IN exp_outputs | sum + x) AS sum_exp_outputs\n" +
                    "               UNWIND RANGE(0, SIZE(output_neurons) - 1) AS i\n" +
                    "               UNWIND RANGE(0, SIZE(outputsValues_Rs) - 1) AS j\n" +
                    "               WITH output_neurons[i] AS neuron,outputsValues_Rs[j] AS outputRow, exp_outputs[i] AS exp_output, \n" +
                    "                    activation_functions[i] AS activation_function, sum_exp_outputs\n" +
                    "               WITH neuron,outputRow, \n" +
                    "                    CASE \n" +
                    "                        WHEN activation_function = 'softmax' THEN exp_output / sum_exp_outputs\n" +
                    "                        ELSE outputRow.output\n" +
                    "                    END AS adjusted_output\n" +
                    "               SET outputRow.output = adjusted_output");
            return Stream.of(new CreateForwardPropagation.CreateResult("ok"));

        } catch (Exception e) {

            return Stream.of(new CreateForwardPropagation.CreateResult("ko"));
        }
    }

    public static class CreateResult {

        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}
