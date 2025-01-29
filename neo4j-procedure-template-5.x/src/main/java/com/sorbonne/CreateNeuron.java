package com.sorbonne;

import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;




public class CreateNeuron {
    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/console.log`
    @Context
    public Log log; //permet de loguer des messages
    @Context
    public GraphDatabaseService db; //référence a la base de données neo4J pour éxécuter les requêtes et manipuler des graphes


    // Pour utiliser call nn.createNeuron("123","0","input","sotfmax")
    @Procedure(name = "nn.createNeuron",mode = Mode.WRITE)
    //mode WRITE car va modifier la base de données
    @Description("Creates a neuron")
    public Stream<CreateResult> createNeuron(@Name("id") String id,
                                       @Name("layer") int layer,
                                       @Name("type") String type,
                                       @Name("activation_function") String activation_function
    ) {

        /*Version proposé par l'enseignant*/
        try (Transaction tx = db.beginTx()) {

            tx.execute("CREATE (n:Neuron {\n" +
                    "id: " + id + ",\n" +
                    "layer:" + layer + ",\n" +
                    "type: " + type + ",\n" +
                    "bias: 0.0,\n" +
                    "output: null,\n" +
                    "m_bias: 0.0,\n" +
                    "v_bias: 0.0,\n" +
                    "activation_function:" + activation_function + "\n" +
                    "})");
            return Stream.of(new CreateNeuron.CreateResult("ok"));

        } catch (Exception e) {

            return Stream.of(new CreateNeuron.CreateResult("ko"));
        }
    }

    public static class CreateResult {

        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}
