package controllers

import play.api.mvc._
import model.Account
import play.api.libs.json.{JsError, Json}

object Application extends Controller {

  def listAccounts = Action {
    val list = Account.findAll()

    val json = Json.toJson(list)

    System.out.println(json)

    Ok(json).as("application/json")
  }

  def updateAccount = Action(parse.json) {
    request => {
      request.body.validate[Account].map {
        case acc => {
          System.out.println(acc.email + " + " + acc.projects)
          Account.exists(acc.email.get) match {
            case true => Account.update(acc)
            case false => Account.create(acc)
          }
          Ok(Json.obj("status" -> "OK")).as("application/json")
        }
      }.recoverTotal {
        e => BadRequest("Detected error:" + JsError.toFlatJson(e))
      }
    }
  }

  def deleteAccount(email: String) = Action {
    Option(email) match {
      case None => BadRequest(Json.obj("status" -> "KO", "message" -> "Account email required")).as("application/json")
      case _ => {
        System.out.println("Removing account " + email)
        if (Account.exists(email)) {
          Account.delete(email)
        }
        Ok(Json.obj("status" -> "OK")).as("application/json")
      }
    }
  }

}