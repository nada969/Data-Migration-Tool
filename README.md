Transferring data between MongoDB (source) and PostgreSQL (destination).
App1:
 - loop for each collection:
  - loop for each Doc in this collection
    - insert the values into PostgreSQL tables(= this collection), doc(= column) 
App:
 - configuration JSON
