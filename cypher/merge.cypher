MERGE (person:Person)
  ON MATCH
  SET person.found = true
RETURN person.name, person.found

MERGE (keanu:Person {name: 'Keanu Reeves', bornIn: 'Beirut', chauffeurName: 'Eric Brown'})
  ON CREATE
  SET keanu.created = timestamp()
RETURN keanu.name, keanu.created


MERGE (keanu:Person {name: 'Keanu Reeves'})
  ON CREATE
  SET keanu.created = timestamp()
  ON MATCH
  SET keanu.lastSeen = timestamp()
RETURN keanu.name, keanu.created, keanu.lastSeen

MATCH
  (charlie:Person {name: 'Charlie Sheen'}),
  (wallStreet:Movie {title: 'Wall Street'})
MERGE (charlie)-[r:ACTED_IN]->(wallStreet)
RETURN charlie.name, type(r), wallStreet.title