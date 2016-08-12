import com.github.tototoshi.csv._
import java.lang.ArrayIndexOutOfBoundsException
import java.util.UUID.{ randomUUID }
import java.sql.{ PreparedStatement, Statement, Driver, DriverManager, Connection }

object main {

  def main(args: Array[String]) {

    var c: Connection = null

    try {

      c = connectDb()

      args(0) match {

        case "load"   => load(c, args(1))
        case "export" => export(c, args(1))
        case "print"  => print(c)
        case _        => usage()

      }

    } catch {

      case (_: ArrayIndexOutOfBoundsException) =>
        usage()

    } finally {

      if (c != null)
        c.close()

    }

  }

  def load(c: Connection, fp: String) {

    var (s: Statement) = null
    var (iUser: PreparedStatement) = null
    var (iPetTypes: PreparedStatement) = null
    var (iPets: PreparedStatement) = null

    try {

      c.setAutoCommit(false)

      s = c.createStatement()

      s.executeUpdate("delete from pets")
      s.executeUpdate("delete from pet_types")
      s.executeUpdate("delete from users")

      iUser     = c.prepareStatement("insert into users (id, name) values (?, ?) on conflict (name) do update set name = EXCLUDED.name returning id")
      iPetTypes = c.prepareStatement("insert into pet_types (name, creator) values (?, ?) on conflict do nothing")
      iPets     = c.prepareStatement("insert into pets (id, owner, pet_type, name) values (?, ?, ?, ?)")

      CSVReader.open(fp).foreach(fs => {

        val users_name     = fs(0)
        val pet_types_name = fs(1)
        val pets_name      = fs(2)

        var uUUID = randomUUID().toString()
        val pUUID = randomUUID().toString()

        iUser.setString(1, uUUID)
        iUser.setString(2, users_name)

        var rs = iUser.executeQuery()
        rs.next()
        uUUID = rs.getString("id")

        iPetTypes.setString(1, pet_types_name)
        iPetTypes.setString(2, uUUID);

        iPets.setString(1, pUUID)
        iPets.setString(2, uUUID)
        iPets.setString(3, pet_types_name)
        iPets.setString(4, pets_name);

        iPetTypes.executeUpdate()
        iPets.executeUpdate()

      })

      c.commit()
      println("OK")

    } catch {

      case (e: Throwable) => {
        c.rollback()
        e.printStackTrace()
        println("FAIL")
      }

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
        val owner = resultSet.getString(1)
        val petType = resultSet.getString(2)
        val pet = resultSet.getString(3)
        println("owner, pet type, pet = " + owner + ", " + petType + ", " + pet)
    }
  }

  def print(c: Connection) {
    println("print")
  }

  def usage() {
    println("usage: main { load <filepath> | export <filepath> | print }")
  }

  def connectDb(): Connection = {

    val driver   = "org.postgresql.Driver"
    val url      = "jdbc:postgresql://localhost:5432/pets"
    val username = "kbrooks"

    Class.forName(driver)
    DriverManager.getConnection(url, username, null)

  }
}
