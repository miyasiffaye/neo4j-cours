package com.sorbonne;

import org.eclipse.jetty.util.Index;
import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.stream.Stream;


public class CreateNeuron {
    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/console.log`
    @Context
    public Log log;
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
    @Procedure(name = "nn.createRelationShipsNeuron",mode = Mode.WRITE)
    @Description("")
    public Stream<CreateResult> createRelationShipsNeuron(
            @Name("from_id") String from_id,
            @Name("to_id") String to_id,
             @Name("weight") String weight
    ) {
        try (Transaction tx = db.beginTx()) {

            tx.execute(
            "MATCH (n1:Neuron" + "{id:'"+ from_id +"'})\n" +
            "MATCH (n2:Neuron" + "{id:'"+ to_id +"'})\n" +
            "CREATE (n1)-[:CONNECTED_TO {weight:" + weight + "}]->(n2)"
            );
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