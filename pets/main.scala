import com.github.tototoshi.csv._
import java.lang.ArrayIndexOutOfBoundsException
import java.sql.{ DriverManager, Connection, Driver, Statement, PreparedStatement }
import java.util.UUID.{ randomUUID }

// Reads and writes data between a .csv and MariaDB database.
// Prior to running start MariaDB with user kbrooks enabled.

object main {
  def main(args: Array[String]) {

    var c: Connection = null

    try {

      c = connectDB()

      args(0) match {
        case "load" => load(c, args(1))
        case "export" => export(c, args(1))
        case "print" => print(c)
        case _ => usage()

      }

    } catch {

      case (_: ArrayIndexOutOfBoundsException) => usage()

    } finally {

      if (c != null) c.close()

    }

  }

  def load(c: Connection, fp: String) {

    var s: Statement = null
    var iUser: PreparedStatement = null
    var iPetTypes: PreparedStatement = null
    var iPets: PreparedStatement = null

    try {
      //
      // start transaction
      c.setAutoCommit(false);
      s = c.createStatement()

      // clear tables
      s.executeQuery("delete from pets")
      s.executeQuery("delete from pet_types")
      s.executeQuery("delete from users")

      iUser = c.prepareStatement("insert ignore into users(id, name) values (?, ?)")
      iPetTypes = c.prepareStatement("insert ignore into pet_types(name, creator) values (?, ?)")
      iPets = c.prepareStatement("insert ignore into pets (id, owner, pet_type, name) values (?, ?, ?, ?)")

      CSVReader.open(fp).foreach(fs => {

        val users_name      = fs(0)
        val pet_types_name = fs(1)
        val pets_name      = fs(2)

        val uUUID = randomUUID().toString
        val pUUID = randomUUID().toString

        iUser.setString(1, uUUID)
        iUser.setString(2, users_name)

        iPetTypes.setString(1, pet_types_name)
        iPetTypes.setString(2, users_name)

        iPets.setString(1, pUUID)
        iPets.setString(2, uUUID)
        iPets.setString(3, pet_types_name)
        iPets.setString(4, pets_name)

        iUser.executeQuery()
        iPetTypes.executeQuery()
        iPets.executeQuery()

      })

      // commit transaction
      c.commit()
      println("Load OK")

    } catch {

      // rollback
      case (e: Throwable) =>
        c.rollback()
        println("Load Failed")
        e.printStackTrace()

    } finally {
      if (s != null)
        s.close()
      if (iUser != null)
        iUser.close()
      if (iPetTypes != null)
        iPetTypes.close()
      if (iPets != null)
        iPets.close()
    }

  }

  def export(c: Connection, fp: String) {
    println("export")

    val s = c.createStatement()

    // set query
    val query: String = """select users.name, pet_types.name, pets.name from users, pet_types, pets
        where users.id=pets.owner and pets.pet_type=pet_types.name"""
    // run query
    val resultSet = s.executeQuery(query)

    // write csv
    while (resultSet.next()) {
      println()
    }

  }

  def print(c: Connection) {
    println("export")
  }

  def usage() {
    println("usage: main {load <filename> | export <filepath> | print }")
  }

  def connectDB(): Connection = {

    val driver = "org.mariadb.jdbc.Driver"
    val url = "jdbc:mysql://localhost/pets"
    val username = "kbrooks"

    Class.forName(driver)
    DriverManager.getConnection(url, username, null)

  }

}
