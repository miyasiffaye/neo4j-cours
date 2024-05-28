//########################Query Populate database####################
CREATE
  (charlie:Person {name: 'Charlie Sheen'}),
  (martin:Person {name: 'Martin Sheen'}),
  (michael:Person {name: 'Michael Douglas'}),
  (oliver:Person {name: 'Oliver Stone'}),
  (rob:Person {name: 'Rob Reiner'}),
  (wallStreet:Movie {title: 'Wall Street'}),
  (charlie)-[:ACTED_IN {role: 'Bud Fox'}]->(wallStreet),
  (martin)-[:ACTED_IN {role: 'Carl Fox'}]->(wallStreet),
  (michael)-[:ACTED_IN {role: 'Gordon Gekko'}]->(wallStreet),
  (oliver)-[:DIRECTED]->(wallStreet),
  (thePresident:Movie {title: 'The American President'}),
  (martin)-[:ACTED_IN {role: 'A.J. MacInerney'}]->(thePresident),
  (michael)-[:ACTED_IN {role: 'President Andrew Shepherd'}]->(thePresident),
  (rob)-[:DIRECTED]->(thePresident),
  (martin)-[:FATHER_OF]->(charlie),
  (rob)-[:COUSIN]->(charlie)


Exo : Creer des relations entre des entites existantes// COUSIN,BROTHER


Match(rob:Person where rob.name = 'Rob Reiner'),
     (michael:Person where michael.name ='Michael Douglas')

MERGE(rob)-[:COUSIN]->(michael)

//#################Query Get all nodes##############################
MATCH (n)
RETURN n
//##################Query Get all nodes with a label##################
MATCH (movie:Movie)
RETURN movie.title
//##################Query Related nodes###############################
//##Returns tous les films  directed par  Oliver Stone.
MATCH (director {name: 'Oliver Stone'})--(movie)
RETURN movie.title
//#################### Match en utilisant le  label  de la liste des labels  ##############
//### Ici cette expression Movie|Person  veut dire Movie ou Person
MATCH (n:Movie|Person)
RETURN n.name AS name, n.title AS title
//#################### Match en utilisant plusieurs relationships  ##############
MATCH (wallstreet {title: 'Wall Street'})<-[:ACTED_IN|DIRECTED]-(person)
RETURN person.name

MATCH (charlie {name: 'Charlie Sheen'})-[:ACTED_IN]->(movie)<-[:DIRECTED]-(director)
RETURN movie.title, director.name

//######################OPTIONAL MATCH#####################################
//### Rejouer cette requete dans une base vide
CREATE
  (charlie:Person {name: 'Charlie Sheen'}),
  (martin:Person {name: 'Martin Sheen'}),
  (michael:Person {name: 'Michael Douglas'}),
  (oliver:Person {name: 'Oliver Stone'}),
  (rob:Person {name: 'Rob Reiner'}),
  (wallStreet:Movie {title: 'Wall Street'}),
  (charlie)-[:ACTED_IN]->(wallStreet),
  (martin)-[:ACTED_IN]->(wallStreet),
  (michael)-[:ACTED_IN]->(wallStreet),
  (oliver)-[:DIRECTED]->(wallStreet),
  (thePresident:Movie {title: 'The American President'}),
  (martin)-[:ACTED_IN]->(thePresident),
  (michael)-[:ACTED_IN]->(thePresident),
  (rob)-[:DIRECTED]->(thePresident),
  (martin)-[:FATHER_OF]->(charlie)
//###################Comparer les resultats de ces 2 requetes suivantes ####################
1.
    MATCH (a:Person {name: 'Martin Sheen'})
    MATCH (a)-[r:DIRECTED]->()
    RETURN a.name, r
2.
    MATCH (p:Person {name: 'Martin Sheen'})
    OPTIONAL MATCH (p)-[r:DIRECTED]->()
    RETURN p.name, r
3. Optional RelationShip
    MATCH (a:Movie {title: 'Wall Street'})
    OPTIONAL MATCH (a)-->(x)
    RETURN x

//######### UNWIND et WITH #########################################
1.
    WITH
      [1, 2] AS a,
      [3, 4] AS b
    UNWIND (a + b) AS x
    RETURN x

    WITH [[1, 2], [3, 4], 5] AS nested
    UNWIND nested AS x
    UNWIND x AS y
    RETURN y
2. Sur les liste vide
    UNWIND [] AS empty
    RETURN 'literal_that_is_not_returned'

    WITH [] AS list
    UNWIND
      CASE
      WHEN list = [] THEN [null]
      ELSE list
      END AS emptylist
    RETURN emptylist

 3. Creer Nodes depuis une liste de parametres
    Parametres :

       :param {
         "events" : [ {
         "year" : 2014,
         "id" : 1
         }, {
         "year" : 2014,
         "id" : 2
         } ]
       }

     Requete :
    UNWIND $events AS event
    MERGE (y:Year {year: event.year})
    MERGE (y)<-[:IN]-(e:Event {id: event.id})
    RETURN e.id AS x ORDER BY x

  //############### CALL CLAUSE ##########################
  1.Creer une base et inserer
      CREATE
      (a:Person:Child {name: 'Alice', age: 20}),
      (b:Person {name: 'Bob', age: 27}),
      (c:Person:Parent {name: 'Charlie', age: 65}),
      (d:Person {name: 'Dora', age: 30})
      CREATE (a)-[:FRIEND_OF]->(b)
      CREATE (a)-[:CHILD_OF]->(c)
      CREATE (a)-[:OWES {dollars: 20}]->(c)
      CREATE (a)-[:OWES {dollars: 25}]->(b)
      CREATE (b)-[:OWES {dollars: 35}]->(d)
      CREATE (d)-[:OWES {dollars: 15}]->(b)
      CREATE (d)-[:OWES {dollars: 30}]->(b)
      CREATE (:Counter {count: 0})

2.
    UNWIND [0, 1, 2] AS x
    CALL {
      MATCH (n:Counter)
      SET n.count = n.count + 1
      RETURN n.count AS innerCount
    }
    WITH innerCount
    MATCH (n:Counter)
    RETURN
      innerCount,
      n.count AS totalCount
