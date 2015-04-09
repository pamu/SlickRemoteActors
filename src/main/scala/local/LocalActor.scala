package local

import akka.actor._
import com.typesafe.config.ConfigFactory
import database.tableQueries.TableWithIdQuery
import database.tables.IdTable

import scala.slick.driver.MySQLDriver.simple._

/**
 * Created by pamu on 9/4/15.
 */
object LocalActor {
  case object Send
}

class LocalActor extends Actor with ActorLogging {
  import LocalActor._

  var remoteActor: Option[ActorSelection] = None

  override def preStart(): Unit = {
    remoteActor = Some(context.actorSelection("akka.tcp://ActorSystem@127.0.0.1:2222/user/RemoteActor"))
    remoteActor.getOrElse {
      println("ChatBox unreachable, shutting down :(")
      context.stop(self)
    }
  }

  override def receive = {
    case Send =>
      case class User(name: String, id: Option[Long] = None)

      class Users(tag: Tag) extends IdTable[User, Long](tag, "users") {
        def name = column[String]("name")
        def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
        def * = (name, id.?) <> (User.tupled, User.unapply)
      }

      val users = new TableWithIdQuery[User, Long, Users](tag => new Users(tag)) {
        override def extractId(model: User): Option[Long] = model.id
        override def withId(model: User, id: Long): User = model.copy(id = Some(id))
      }

      import remote.RemoteActor._

      remoteActor.map(remote => remote ! Entry[User, Long, Users](users, User("pamu nagarjuna")))
      log.info("message sent :)")

    case msg => log.info(s"unknown message of type ${msg.getClass}")
  }
}

object Starter {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load("client")
    val actorSystem = ActorSystem("ActorSystem", config)
    val localActor = actorSystem.actorOf(Props[LocalActor], name = "LocalActor")
    import LocalActor._
    for(i <- 1 to 100) {
      localActor ! Send
      Thread.sleep(5000)
    }
  }
}