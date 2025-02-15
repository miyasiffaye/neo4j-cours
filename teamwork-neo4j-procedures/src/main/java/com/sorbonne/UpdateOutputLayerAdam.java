package com.sorbonne;

import org.neo4j.procedure.*;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.Result;
import java.util.Map;

public class UpdateOutputLayerAdam{

    @Context
    public GraphDatabaseService db;

    @Procedure(name = "nn.updateOutputLayerAdam", mode = Mode.WRITE)
    @Description("Update output layer during the backward pass")
    public void updateOutputLayerAdam(
            @Name("learning_rate") double learningRate,
            @Name("beta1") double beta1,
            @Name("beta2") double beta2,
            @Name("epsilon") double epsilon,
            @Name("t") long t) {

        try (Transaction tx = db.beginTx()) {

            String cypherQuery = """
                MATCH (output:Neuron {type: 'output'})<-[r:CONNECTED_TO]-(prev:Neuron)
                MATCH (output)-[outputsValues_R:CONTAINS]->(row_for_outputs:Row {type: 'outputsRow'})
                WITH DISTINCT output,r,prev,outputsValues_R,row_for_outputs,
                     CASE 
                         WHEN output.activation_function = 'softmax' THEN outputsValues_R.output - outputsValues_R.expected_output
                         WHEN output.activation_function = 'sigmoid' THEN (outputsValues_R.output - outputsValues_R.expected_output) * outputsValues_R.output * (1 - outputsValues_R.output)
                         WHEN output.activation_function = 'tanh' THEN (outputsValues_R.output - outputsValues_R.expected_output) * (1 - outputsValues_R.output^2)
                         ELSE outputsValues_R.output - outputsValues_R.expected_output  //For linear activation
                     END AS gradient,
                     $t AS t
                MATCH (prev)-[r:CONNECTED_TO]->(output)
                SET r.m = $beta1 * COALESCE(r.m, 0) + (1 - $beta1) * gradient * COALESCE(prev.output, 0)
                SET r.v = $beta2 * COALESCE(r.v, 0) + (1 - $beta2) * (gradient * COALESCE(prev.output, 0))^2
                SET r.weight = r.weight - $learning_rate * (r.m / (1 - ($beta1 ^ t))) / 
                               (SQRT(r.v / (1 - ($beta2 ^ t))) + $epsilon)
                SET output.m_bias = $beta1 * COALESCE(output.m_bias, 0) + (1 - $beta1) * gradient
                SET output.v_bias = $beta2 * COALESCE(output.v_bias, 0) + (1 - $beta2) * (gradient^2)
                SET output.bias = output.bias - $learning_rate * (output.m_bias / (1 - ($beta1 ^ t))) / 
                             (SQRT(output.v_bias / (1 - ($beta2 ^ t))) + $epsilon)
                SET output.gradient = gradient
            """;
            
            tx.execute(cypherQuery, Map.of(
                    "learning_rate", learningRate,
                    "beta1", beta1,
                    "beta2", beta2,
                    "epsilon", epsilon,
                    "t", t
            ));

            tx.commit();
        }
    }
}
