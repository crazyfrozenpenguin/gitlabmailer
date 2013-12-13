package controllers

import mail.{Mail, send}
import play.api.mvc._
import play.api.libs.json._
import model.Account

/**
 * Created with IntelliJ IDEA.
 * Date: 12/9/13
 * Time: 9:19 PM
 */
object Mailer extends Controller {

  case class GitUser(name: String, email: String)

  case class GitCommit(id: String, message: String, timestamp: String, url: String, author: GitUser)

  case class GitRepo(name: String, url: String, description: String, homepage: String)

  case class GitPush(before: String, after: String, ref: String, userId: Int, username: String, totalCommitsCount: Int, repository: GitRepo, commits: Seq[GitCommit])

  implicit object GitUserReads extends Reads[GitUser] {
    def reads(json: JsValue): JsResult[GitUser] = JsSuccess(
      GitUser(
        (json \ "name").as[String],
        (json \ "email").as[String]
      ))
  }

  implicit object GitCommitReads extends Reads[GitCommit] {
    def reads(json: JsValue): JsResult[GitCommit] = JsSuccess(
      GitCommit(
        (json \ "id").as[String],
        (json \ "message").as[String],
        (json \ "timestamp").as[String],
        (json \ "url").as[String],
        (json \ "author").as[GitUser]
      ))
  }

  implicit object GitRepoReads extends Reads[GitRepo] {
    def reads(json: JsValue): JsResult[GitRepo] = JsSuccess(
      GitRepo(
        (json \ "name").as[String],
        (json \ "url").as[String],
        (json \ "description").as[String],
        (json \ "homepage").as[String]
      ))
  }

  implicit object GitPushReads extends Reads[GitPush] {
    def reads(json: JsValue): JsResult[GitPush] = JsSuccess(
      GitPush(
        (json \ "before").as[String],
        (json \ "after").as[String],
        (json \ "ref").as[String],
        (json \ "user_id").as[Int],
        (json \ "user_name").as[String],
        (json \ "total_commits_count").as[Int],
        (json \ "repository").as[GitRepo],
        (json \ "commits").as[Seq[GitCommit]]
      ))
  }

  def fromGitlab(proj: String) = Action(parse.json) {
    request => {
      Option(proj) match {
        case None => BadRequest(Json.obj("status" -> "KO", "message" -> "Application identifier required")).as("application/json")
        case _ => {
          System.out.println("Request mailer app: " + proj)
          System.out.println("Received body: " + request.body)

          try {
            val gp = request.body.as[GitPush]

            send a new Mail(
              from = (mail.getDefaultFrom, "Mail Monster"),
              to = Account.findAllEmailsOnProject(proj),
              subject = gp.username + " pushed to " + proj + "/" + gp.ref.split("/").last + " (" + gp.after + ")",
              message = createMessageBody(gp)
            )

            Ok(Json.obj("status" -> "OK")).as("application/json")

          } catch {
            case e: Throwable => {
              System.err.println(e)
              ServiceUnavailable(Json.obj("status" -> "KO", "message" -> "Internal Server Error"))
            }
          }
        }
      }
    }
  }

  def createMessageBody(gp:GitPush) = {
    var body = "Mail Monster Push Notification\n\n" +
      "Repository: " + gp.repository.name + "\n" +
      "Git: " + gp.repository.url + "\n" +
      "Gitlab: " + gp.repository.homepage + "\n\n" +
      "Pushed by: " + gp.username + "\n" +
      "Ref: " + gp.ref + "\n" +
      "Before: " + gp.before + "\n" +
      "After: " + gp.after + "\n\n" +
      gp.totalCommitsCount + " new commits:\n\n";

    gp.commits.foreach(c =>
      body += "\tCommit: " + c.id + "\n" +
        "\tBy: " + c.author.name + " (" + c.author.email + ")\n" +
        "\tWhen: " + c.timestamp + "\n" +
        "\tUrl: " + c.url + "\n" +
        "\tMessage: " + c.message + "\n\n"
    )

    body
  }

}
