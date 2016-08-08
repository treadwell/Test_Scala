object Main {

  def main(args: Array[String]) {
    println(fac(args(0).toInt))
  }

  def fac(n: Int): Int = if (n <= 0) 1 else n * fac(n - 1)

}
