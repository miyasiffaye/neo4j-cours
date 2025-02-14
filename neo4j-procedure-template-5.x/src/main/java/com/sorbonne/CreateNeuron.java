package com.sorbonne;

import java.util.stream.Stream;

//import org.eclipse.jetty.util.Index;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Description;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Name;
import org.neo4j.procedure.Procedure;


public class CreateNeuron {
    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/console.log`
    //@Context
    //public Log log;
    @Context
    public GraphDatabaseService db;

    // Exemple de base à lire completer corriger
    // Pour utiliser call nn.createNeuron("123","0","input","sotfmax")
    @Procedure(name = "nn.createNeuron",mode = Mode.WRITE)
    @Description("")
    public Stream<CreateResult> createNeuron(@Name("id") String id,
                                       @Name("layer") String layer,
                                       @Name("type") String type,
                                       @Name("activation_function") String activation_function
    ) {
        try (Transaction tx = db.beginTx()) {

          tx.execute("CREATE (n:Neuron {\n" +
                    "id: '" + id + "',\n" +
                    "layer:" + layer + ",\n" +
                    "type: '" + type + "',\n" +
                    "bias: 0.0,\n" +
                    "output: null,\n" +
                    "m_bias: 0.0,\n" +
                    "v_bias: 0.0,\n" +
                    "activation_function:'" + activation_function + "'\n" +
                    "})");
            tx.commit();
            return Stream.of(new CreateResult("ok"));

        } catch (Exception e) {

            return Stream.of(new CreateResult("ko"));
        }
    }

    public static class CreateResult {

        public final String result;

        public CreateResult(String result) {
            this.result = result;
        }
    }
}