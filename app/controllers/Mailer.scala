package controllers

import mail.{Mail, send}
import play.api.mvc._
import play.api.libs.json._
import model.Account
import scala.collection.mutable.ListBuffer
import scala.collection.mutable

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

            val accounts = Account.findAllEmailsOnProject(proj)
            val plainOnly: Seq[String] = accounts collect {
              case acc if acc._2 == "Text" => acc._1
            }
            val htmlOnly: Seq[String] = accounts collect {
              case acc if acc._2 == "HTML" => acc._1
            }

            val subject = gp.username + " pushed to " + proj + "/" + gp.ref.split("/").last + " (" + gp.after + ")"
            val message = createMessageBody(gp)
            if (!plainOnly.isEmpty) {
              sendMail(plainOnly, subject, message)
            }
            if (!htmlOnly.isEmpty) {
              val htmlMessage = Option(createHtmlMessageBody(gp, subject))
              sendMail(htmlOnly, subject, message, htmlMessage)
            }

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

  def sendMail(emailList: Seq[String], subject: String, plain: String, html: Option[String] = None) = {
    send a new Mail(
      from = (mail.getConfiguredSenderEmail, "Mail Monster"),
      to = emailList,
      subject = subject,
      message = plain,
      richMessage = html
    )
  }

  def createMessageBody(gp: GitPush): String = {
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
        "\tMessage: " + c.message + "\n" +
        "\tUrl: " + c.url + "\n\n"
    )

    body
  }

  def createHtmlMessageBody(gp: GitPush, subject: String): String = {
    var commits = "";

    gp.commits.foreach(c =>
      commits += """
          <table style="padding-left:50px;">
            <thead>
              <th>Commit:</th>
              <th>By:</th>
              <th>When:</th>
              <th>Message:</th>
              <th>Url:</th>
            </thead>
            <tbody>""" +
        "<tr><td>" + c.id + "</td></tr>" +
        "<tr><td>" + c.author.name + "(" + c.author.email + ")</td></tr>" +
        "<tr><td>" + c.timestamp + "</td></tr>" +
        "<tr><td>" + c.message + "</td></tr>" +
        "<tr><td>" + c.url +
        """  </td></tr>
            </tbody>
          </table><br><br>"""
    )

    val html = """
    <html>
      <head>
        <title>""" + subject + """</title>
        <style>
          table, td, th {border: 1px;}

          thead {float:left;}

          thead th {display:block; margin-bottom:2px; text-align:right;}

          tbody {float:right;}
        </style>
      </head>
      <body>
        <h2>""" + subject + """</h2>
        <table>
          <thead>
            <th>Repository:</th>
            <th>Git:</th>
            <th>Gitlab:</th>
            <th>Pushed by:</th>
            <th>Ref:</th>
            <th>Before:</th>
            <th>After:</th>
          </thead>
          <tbody>
            <tr>
              <td>""" + gp.repository.name + """</td>
            </tr>
            <tr>
              <td>""" + gp.repository.url + """</td>
            </tr>
            <tr>
              <td>""" + gp.repository.homepage + """</td>
            </tr>
            <tr>
              <td>""" + gp.username + """</td>
            </tr>
            <tr>
              <td>""" + gp.ref + """</td>
            </tr>
            <tr>
              <td>""" + gp.before + """</td>
            </tr>
            <tr>
              <td>""" + gp.after + """</td>
            </tr>
          </tbody>
        </table>
        <h3>""" + gp.totalCommitsCount + """ new commits:</h3>
        """ + commits + """
      </body>
    </html> """
    html
  }


}
