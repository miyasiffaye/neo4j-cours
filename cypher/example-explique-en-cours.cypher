MATCH (:`Person`:`ACTOR` {name: 'Anna'})-[ r:KNOWS WHERE r.since < 2020 ]->(friend:`Person`:`PRODUCTOR`)
RETURN count(r) As numberOfFriends

Le nombre de producteur que la personne anna qui est actor connait depuis 2020
CINEMA

ACTOR
PRODUCTOR

p=MATCH (:`Person`:`ACTOR` {name: 'Anna'})-[ r:KNOWS WHERE r.since < 2020 ]->(friend:`Person`:`PRODUCTOR`)
RETURN p

PERSON:ACTOR

match(anna:Person:ACTOR {name:'Anna'})  return  anna.films

// Resultat
['GAME OF TRONE','BREAKING BAD' ] // list


match(anna:Person:ACTOR {name:'Anna'})  unwind  anna.films as film
Mettre en majuscule
 return film
'GAmE OF tRONE',
'BREAKING BAD'

ACTOR{
name :'',
films :[12,869,456 ],
age: 25 // int
age:'25' // string
}
films []


MATCH (p:Person{name:'Bob'}) RETURN p
MATCH (p:Person) where p.name = 'Bob' RETURN p


   CREATE (alice:Person {name: 'Alice',age:25})
    CREATE (bob:Person {name: 'Bob',age:25,ville:''})
    CREATE (zach:Person {name: 'Zach',age:25,ville:''})

       CREATE (alice:Person {name: 'Alice',age:25})
        CREATE (bob:Person {name: 'Bob',age:25,ville:''})
        CREATE (zach:Person {name: 'Zach',age:25,ville:''})

           CREATE (alice:Person {name: 'Alice',age:25})
            CREATE (bob:Person {name: 'Bob',age:25,ville:''})
            CREATE (zach:Person {name: 'Zach',age:25,ville:''})


    CREATE (alice)-[:FRIEND]->(bob)
    CREATE (bob)-[:FRIEND]->(alice)
    CREATE (bob)-[:FRIEND]->(zach)
    CREATE (zach)-[:FRIEND]->(alice)
 match (n:Person) where n.name = 'Bob' set n.surnom ='B&B'

   match (n:Person)  detach delete n; supprime les noeuds + les relation

   MATCH (p:Person)-[r:FRIEND]->(amis:Person) delete p,r,amis














