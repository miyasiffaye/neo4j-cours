# Description

Neo4j project Sorbonne Data Analytics 2024-2025 

# Authors

* TSURANOVA Svetlana
* JEAN Luckner
* FALL Aminata
* DIA Yaye Touti

# Instuctions
## Build the project
```
cd teamwork-neo4j-procedures
mvn clean
mvn package
```

## Configure neo4j
Copy teamwork-neo4j-procedures/target/procedure-template-1.0.0-SNAPSHOT.jar
to the plugins directory of neo4j.

Add following line to the conf/neo4j.conf to increase the memory
available for neo4j up to 15gb in order to avoid out of memory errors:
```
server.memory.heap.max_size=15g
```

Add our procedures to allow list in conf/neo4j.conf:
```
dbms.security.procedures.unrestricted=nn.*
```

Run neo4j with:
```
bin/neo4j console
```

## Configure the project
```
cd neo4j-cours\projet-final-promotion-2024-2025
// Configure virtual environment
python3 -m venv .venv
// Activate the virtual environment in the current console window
.venv\Script\activate
// Install dependencies
pip install pandas
pip install neo4j
pip install matplotlib
```

Update username and dbSettings in the
projet-final-promotion-2024-2025/Neo4jGraphAsNnetwork.py:
```
password = "..."
database = "..."
```

## Run
Cleanup previous run with Cypher (DETACH will also delete connections):
```
MATCH (n) DETACH DELETE n
```
Run python script with:
```
cd projet-final-promotion-2024-2025
.venv\Script\activate
python Neo4jGraphAsNnetwork.py
```