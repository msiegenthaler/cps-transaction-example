package example

import base._

object Example extends App {
  case class Person(id: Long, first: String, last: String)

  trait PersonRepo {
    def create(first: String, last: String): Person@tx
    def get(first: String, last: String): Option[Person]@tx
    def delete(id: Long): Unit@tx
  }


  case class TxC(user: String) extends TransactionContext
  object DbPersonRepo extends PersonRepo {
    private def execSql(sql: String): List[String]@tx = Transaction {
      case TxC(user) =>
        println(s"User $user executes '$sql'")
        Nil
      //List("a")
    }.tx

    def create(first: String, last: String) = {
      val personOpt = get(first, last)
      if (personOpt.isDefined) personOpt.get
      else {
        execSql("Insert")
        Person(3, first, last)
      }
    }

    def create3(first: String, last: String): Person@tx = {
      val a: Option[Person] = get(first, last)
      val b = a.map(Transaction.bind).getOrElse[Transaction[Person]](transaction {
        Person(execSql("Insert").size, first, last)
      })
      b.tx
    }

    def create2(first: String, last: String): Person@tx = {
      val a: Transaction[Option[Person]] = get(first, last).transaction
      val b: Transaction[Person] = a.flatMap {
        c =>
          c.map(Transaction.bind).getOrElse {
            execSql("Insert").transaction.map(x => Person(x.length, first, last))
          }
      }
      b.tx
    }

    def get(first: String, last: String) = {
      val o = execSql("Select")
      o.headOption.map(x => Person(x.length, first, last))
    }
    def delete(id: Long) = {
      execSql("Delete")
    }
  }

  val repo = DbPersonRepo
  val a = repo.create3("Maaa", "Seee").transaction
  println("A " + a)
  val b: Person = a.run(TxC("Mario"))
  println("B " + b)


  val aa = repo.create2("Xee", "Yaa").transaction
  println("AA " + aa)
  val ab: Person = aa.run(TxC("Hans"))
  println("AB " + ab)
}
