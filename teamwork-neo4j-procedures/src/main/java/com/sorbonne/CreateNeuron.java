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
            Node neuron = tx.createNode(Label.label("Neuron"));
            neuron.setProperty("id", id);
            neuron.setProperty("layer", layer);
            neuron.setProperty("type", type);
            neuron.setProperty("bias", 0.0);
            neuron.setProperty("m_bias", 0.0);
            neuron.setProperty("v_bias", 0.0);
            neuron.setProperty("activation_function", activation_function);
            tx.commit();
            return Stream.of(new CreateResult("Success"));
        } catch (Exception e) {
            return Stream.of(new CreateResult("Error: " + e.getMessage()));
        }
    }

    public static class CreateResult {
        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}
