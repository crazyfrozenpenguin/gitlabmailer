package model

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import play.api.libs.json.{Writes, Json}

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

  def create(account: Account): Unit = {
    DB.withConnection { implicit connection =>
      SQL("insert into account(email, projects) values ({email},{projects})").on(
        'email -> account.email, 'projects -> account.projects
      ).executeUpdate()
    }
  }

}
