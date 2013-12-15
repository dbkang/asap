package asapstack

import java.sql._

object DB {
  Class.forName("org.postgresql.Driver")
  def connection = DriverManager.getConnection("jdbc:postgresql:dan", "dan", "password")
}
