package remote

import akka.actor.{Props, ActorSystem, ActorLogging, Actor}
import com.typesafe.config.ConfigFactory
import database.tableQueries.TableWithIdQuery
import database.tables.IdTable

import scala.slick.driver.MySQLDriver.simple._

/**
 * Created by pamu on 9/4/15.
 */
object RemoteActor {
  case class Entry[M, I: BaseColumnType, T <: IdTable[M, I]](tableWithIdQuery: TableWithIdQuery[M, I, T], model: M)
}

class RemoteActor extends Actor with ActorLogging {
  import RemoteActor._

  lazy val db = Database.forURL(
    url = "jdbc:mysql://localhost/demo",
    driver = "com.mysql.jdbc.Driver",
    user= "root",
    password= "root")

  override def receive = {
    case Entry(table, model) =>
      db.withSession { implicit sx =>
        table.createIfNotExists
        table.save(model)
      }
      log.info("done saving to database")
    case msg => log.info(s"unknown message of type ${msg.getClass}")
  }
}

object Starter {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val actorSystem = ActorSystem("ActorSystem", config)
    val remoteActor = actorSystem.actorOf(Props[RemoteActor], name = "RemoteActor")
  }
}