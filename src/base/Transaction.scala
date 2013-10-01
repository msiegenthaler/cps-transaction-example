package base


trait Transaction[+A] {
  def run(context: TransactionContext): A

  import Transaction._

  def map[B](f: A => B) = flatMap(f.andThen(bind))

  def flatMap[B](f: A => Transaction[B]) = Transaction { context =>
    val a = run(context)
    f(a).run(context)
  }
}

object Transaction {
  def bind[A](a: A) = apply(_ => a)

  def apply[A](f: TransactionContext => A): Transaction[A] = new Transaction[A] {
    def run(context: TransactionContext) = f(context)
  }
}

trait TransactionContext