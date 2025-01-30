package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.procedure.*;

import java.util.stream.Stream;

/*  Python code without Procedure:
   tx.run("""
     'CREATE (n:Neuron {
        id: $id,
        layer: $layer,
        type: $type,
        bias: 0.0,
        output: null,
        m_bias: 0.0,
        v_bias: 0.0,
        activation_function: $activation_function
     })
     """, id=f"{layer_index}-{neuron_index}", layer=layer_index, type=layer_type,
     activation_function=activation_function)
 */

/* Python code with procedure
    tx.run(
        "call nn.createNeuron($id,$type,$layer,$activation_function)",
        id=layer_index-neuron_index, layer=layer_index, type=layer_type,
        activation_function=activation_function)
 */
public class CreateNeuron {
    @Context
    public GraphDatabaseService db;

    // Exemple de base à lire completer corriger
    // Pour utiliser call nn.createNeuron("123","0","input","softmax")
    @Procedure(name = "nn.createNeuron",mode = Mode.WRITE)
    @Description("Creates a neuron")
    public Stream<CreateResult> createNeuron(@Name("id") Long id,
                                             @Name("layer") Long layer,
                                             @Name("type") String type,
                                             @Name("activation_function") String activation_function) {
        try (Transaction tx = db.beginTx()) {
            // We made 3 changes to the original example:
            // 1. Added tx.commit to commit the transaction and make the node available for further requests
            // 2. Changed the type of id and layer to Long, since they are numeric and neo4j doesn't support java.lang.Integer class
            // 3. Added ' for the type and activation function, since they are string values

            tx.execute("CREATE (n:Neuron {\n" +
                    "id: " + id + ",\n" +
                    "layer:" + layer + ",\n" +
                    "type: '" + type + "',\n" +
                    "bias: 0.0,\n" +
                    "output: null,\n" +
                    "m_bias: 0.0,\n" +
                    "v_bias: 0.0,\n" +
                    "activation_function: '" + activation_function + "'\n" +
                    "})");
            tx.commit();
            return Stream.of(new CreateResult("Success"));
        } catch (Exception e) {
            return Stream.of(new CreateResult("Failure"));
        }
    }

    public static class CreateResult {
        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}
