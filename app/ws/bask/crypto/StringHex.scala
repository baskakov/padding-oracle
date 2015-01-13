package ws.bask.crypto

object StringHex {
  def xorByKey(keyHex: String, inputHex: String) = {
    inputHex.foldLeft(("", 0))({
      case ((xs, i), b) =>
        implicit def hexCharToInt(char: Char) = Integer.parseInt(char.toString, 16)
        println("zz "+b.toInt.toString)
        val msgChar = Integer.parseInt(b.toString, 16)
        val keyChar = Integer.parseInt(keyHex.charAt(i % keyHex.size).toString, 16)
        val xor = Integer.toHexString(msgChar ^ keyChar)
        (xs ++ xor, i + 1)
    })._1.toUpperCase
  }

  def textToHex: String => String = _.map(c => Integer.toHexString(c.toInt)).mkString("").toUpperCase

  def hexToText: String => String = hexToAscii.andThen(_.map(_.toChar.toString).mkString(""))

  def hexToAscii: String => List[Int] = _.grouped(2).map(twoBytes => Integer.parseInt(twoBytes, 16)).toList

  def asciiToHex: List[Int] => String = _.map(n => {
    val i = Integer.toHexString(n)
    if(i.size == 1) "0" + i else i
  }).mkString("").toUpperCase
}