package controllers

import play.api._
import play.api.mvc._
import ws.bask.crypto.StringHex._
import scala.concurrent._
import ws.bask.crypto.paddingoracle.{PaddingOracle, Oracle}
import ExecutionContext.Implicits.global

object Application extends Controller {

  def index = Action.async({ request =>
    val query = request.body.asFormUrlEncoded.getOrElse(Map.empty)
    val ciphertext = query.get("ciphertext").flatMap(_.headOption)
    val asciiCipher = ciphertext//.map(s => hexToAscii(s))
    val result = asciiCipher.map(ascii => {
      Future({
        /*val oracle = new Oracle()
        oracle.connect()
        val res = oracle.send(ascii.toArray, ascii.size / 16)
        val list = (0 until ascii.size).map(x => ascii.updated(x, ((ascii(x) + 1) % 256))).map(newAscii => {
          asciiToHex(newAscii) + " - " + oracle.send(newAscii.toArray, 3)
        }).mkString("\r\n")
        //val rndList = (0 until 15).map(x => (0 until x).map(_ => math.random*255.toInt) ++ (x to 15).map(y => 16-y) )
        val rnd = (0 to 30).map(_ => 0).toList

        /*val listFirst = (0 to 255).toList.map(y => rnd :+ y).toList.map(asc => {
          val ass = asc ++ ascii.drop(32)
          asciiToHex(ass) +" - " + oracle.send((ass).toArray,3)
        }).mkString("\r\n")*/
        /*val list2 = (0 until 256).map(x => ascii.updated(47, x)).map(newAscii => {
          asciiToHex(newAscii) + " - " + oracle.send(newAscii.toArray, 3)
        }).mkString("\r\n")*/
        oracle.disconnect()*/
        val res = PaddingOracle.brek(ascii)

        Ok(views.html.index(ciphertext, Some(res)))
      })
    }).getOrElse(Future.successful(Ok(views.html.index(ciphertext, None))))
    result
  })

}