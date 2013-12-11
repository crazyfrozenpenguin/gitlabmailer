package controllers

import play.api.mvc._
import model.Account
import play.api.libs.json.Json

object Application extends Controller {

  def listAccounts() = Action {
    val list = Account.findAll()

    val json = Json.toJson(list)

    System.out.println(json)

    Ok(json).as("application/json")
  }

  def updateAccount() = Action(parse.json) {
    request => {
      Ok(Json.obj("status" -> "OK")).as("application/json")
    }
  }

}