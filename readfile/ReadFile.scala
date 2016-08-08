import scala.io.Source

object ReadFile {
   def main(args: Array[String]) {
      println("Following is the content read:" )

      Source.fromFile("Demo.txt" ).foreach {
         print
      }
   }
}
