package controllers

import mail.{Mail, send}
import play.api.mvc._
import play.api.libs.json._

/**
 * Created with IntelliJ IDEA.
 * Date: 12/9/13
 * Time: 9:19 PM
 */
object Mailer extends Controller {

  def fromGitlab(app: String) = Action(parse.json) {
    request => {
      Option(app) match {
        case None => BadRequest(Json.obj("status" -> "KO", "message" -> "Application identifier required")).as("application/json")
        case _ => {
          System.out.println("Request mailer app: " + app)
          System.out.println("Received body: " + request.body)

          try {
            send a new Mail(
              from = ("gitlabmailer@test.com", "Gitlab Mailer"),
              to = Seq("bmrosantos@gmail.com"),
              subject = "Import stuff",
              message = request.body.toString()
            )

            Ok(Json.obj("status" -> "OK")).as("application/json")

          } catch {
            case _ => ServiceUnavailable(Json.obj("status" -> "KO", "message" -> "Internal Server Error"))
          }
        }
      }
    }
  }
}
