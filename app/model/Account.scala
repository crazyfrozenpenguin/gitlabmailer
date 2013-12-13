package model

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import play.api.libs.json._
import anorm.~
import anorm.Id
import scala.collection.{mutable, LinearSeq}
import scala.collection.mutable.ListBuffer

import mutable.Buffer

case class Account(email:Pk[String], projects:String)

/**
 * Created with IntelliJ IDEA.
 * Date: 12/10/13
 * Time: 8:46 PM
 */
object Account {

  implicit object AccountWrites extends Writes[Account] {
    def writes(acc:Account) = Json.obj(
      "email" -> Json.toJson(acc.email.get),
      "projects" -> Json.toJson(acc.projects)
    )
  }

  implicit object AccountReads extends Reads[Account] {
     def reads(json:JsValue): JsResult[Account] = JsSuccess(Account(
         Id((json \ "email").as[String]),
         (json \ "projects").as[String]
       )
     )
  }

  val simple = {
      get[Pk[String]]("email") ~
      get[String]("projects") map {
      case email~projects => Account(email, projects)
    }
  }

  def findAll(): Seq[Account] = {
    DB.withConnection { implicit connection =>
      SQL("select * from account").as(Account.simple *)
    }
  }

  def findAllEmailsOnProject(proj:String):Buffer[String] = {
    var result:Buffer[String] = ListBuffer()
    DB.withConnection { implicit connection =>
      var list:Seq[Account] = SQL("SELECT * FROM account acc WHERE acc.projects LIKE {project}").on(
        'project -> ("%" + proj + "%")
      ).as(Account.simple *)

      // FIXME: It's late and Coffee is required! Update DB structure so that this lame loop is unnecessary... if ever!
      list.foreach( acc =>
        if (acc.projects.split(",").contains(proj)) {
          result += acc.email.get
        }
      )
    }
    result
  }

  def create(account: Account): Unit = {
    DB.withConnection { implicit connection =>
      SQL("insert into account(email, projects) values ({email},{projects})").on(
        'email -> account.email, 'projects -> account.projects
      ).executeUpdate()
    }
  }

  def update(account:Account):Unit = {
    DB.withConnection {
      implicit connection =>
        SQL("update account set projects = {projects} where email = {email}").on(
          'email -> account.email, 'projects -> account.projects
        ).executeUpdate()
    }
  }

  def exists(email:String):Boolean = {
    DB.withConnection { implicit connection =>
      val sql = SQL("select 1 from account where email = {email}").on(
        'email -> Id(email)
      )
      sql.execute()
      sql.list().size == 1
    }
  }

  def delete(email:String):Unit = {
    DB.withConnection { implicit connection =>
      SQL("delete from account where email = {email}").on(
        'email -> email
      ).execute();
    }
  }

}
