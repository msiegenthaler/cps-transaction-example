cps-transaction-example
=======================

Example implementation of transactions using continuations (cps).


Example method (@tx to mark as transactional)
    def create(first: String, last: String): Person@tx


Example usage

    val myTx = {
      ...
      val person = repo.create("a", "b")
      println(s"Created $person")
      ...
      person
    }
    val person = myTx.run(TxC("Mario"))
    
where TxC is the transaction context (in this example it only contains the name of the user, in a real application it'd contain things like the java.sql.Connection).
