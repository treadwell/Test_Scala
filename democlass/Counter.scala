class Counter {
  private var value = 0 // you must initialize the field
  def increment() { value += 1 } // methods are public by default
  def current() = value
}
