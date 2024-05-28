1.Creer une nouvelle base stackoverflow

2.Executer la requete suivante :
WITH "https://api.stackexchange.com/2.2/questions?pagesize=100&order=desc&sort=creation&tagged=neo4j&site=stackoverflow&filter=!5-i6Zw8Y)4W7vpy91PMYsKM-k9yzEsSC1_Uxlf" AS url
CALL apoc.load.json(url) YIELD value

UNWIND value.items AS q
MERGE (question:Question {id:q.question_id})
ON CREATE SET question.title = q.title,
question.share_link = q.share_link,
question.favorite_count = q.favorite_count

FOREACH (tagName IN q.tags | MERGE (tag:Tag {name:tagName}) MERGE (question)-[:TAGGED]->(tag))
FOREACH (a IN q.answers |
MERGE (question)<-[:ANSWERS]-(answer:Answer {id:a.answer_id})
MERGE (answerer:User {id:a.owner.user_id}) ON CREATE SET answerer.display_name = a.owner.display_name
MERGE (answer)<-[:PROVIDED]-(answerer)
)

WITH * WHERE NOT q.owner.user_id IS NULL
MERGE (owner:User {id:q.owner.user_id}) ON CREATE SET owner.display_name = q.owner.display_name
MERGE (owner)-[:ASKED]->(question)

3. Import depuis  votre PC

    1. CALL apoc.import.json("file:///d:/neo4j/data/all.json")

 Vous devez avoir une erreur. Analyser et corriger.

4. Import un fichier cypher
call apoc.cypher.runFile('file:///d:/neo4j/data/movie.cypher',{statistics:true})