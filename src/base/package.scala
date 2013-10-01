import scala.util.continuations._

package object base {
  /** Indicates that the value is produced in a transaction. */
  type tx = cps[Transaction[Any]]

  /** Coverts a Transaction (monad) into a transactional value (continuation). */
  def tx[A](transaction: Transaction[A]): A@tx = shift[A, Transaction[Any], Transaction[Any]] {
    f: (A => Transaction[Any]) => transaction.flatMap(f)
  }
  implicit def transactionToTx[A](transaction: Transaction[A]) = tx(transaction)
  implicit class TransactionToTx[+A](transaction: Transaction[A]) {
    def tx = transactionToTx(transaction)
  }

  /** Converts a @tx value into a Transaction monad. */
  def transaction[A](body: => A@tx): Transaction[A] = {
    val ctx = reify[A, Transaction[Any], Transaction[Any]](body)
    val r = ctx.foreach(Transaction.bind)
    r.asInstanceOf[Transaction[A]]
  }
  implicit def txToTransaction[A](body: => A@tx) = transaction(body)
  implicit class TxToTransaction[A](cpsTx: => A@tx) {
    def transaction = txToTransaction(cpsTx)
  }


  /** Noop @tx value. i.e. usefull for if (con) doTransaction else noop. */
  def noop = asTx(())

  /** Converts a value into to value lifted to @tx. */
  def asTx[A](a: A): A@tx = Transaction.bind(a).tx
  implicit def valueToTx[A](a: A) = asTx(a)
  implicit class ValueToTx[A](a: A) {
    def bind: A@tx = valueToTx(a)
    def asTx: A@tx = valueToTx(a)
  }

  /** Discard a @tx value. Useful in Unit@tx methods for the return value. */
  implicit def anyTxToUnitTx[A](a: => A@tx) = {
    a
    ()
  }
}
