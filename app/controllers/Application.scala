package controllers

import play.api._
import play.api.mvc._
import ws.bask.crypto.StringHex._
import scala.concurrent._
import ws.bask.crypto.paddingoracle.Oracle
import ExecutionContext.Implicits.global

object Application extends Controller {

  def index = Action.async({ request =>
    val query = request.body.asFormUrlEncoded.getOrElse(Map.empty)
    val ciphertext = query.get("ciphertext").flatMap(_.headOption)
    val asciiCipher = ciphertext.map(s => hexToAscii(s))
    val result = asciiCipher.map(ascii => {
      Future({
        val oracle = new Oracle()
        oracle.connect()
        val res = oracle.send(ascii.toArray, 3)
        oracle.disconnect()
        Ok(views.html.index(ciphertext, Some(res.toString)))
      })
    }).getOrElse(Future.successful(Ok(views.html.index(ciphertext, None))))
    result
  })

}