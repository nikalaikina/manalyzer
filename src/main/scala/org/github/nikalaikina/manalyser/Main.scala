package org.github.nikalaikina.manalyser

import org.telegram.api.message.TLMessage


object Main extends App {

  try {
    Application(new Service(ApiFactory.api)).run
    println("DONE")
  } catch {
    case x: Throwable =>
      println(x)
      throw x
  }

}

case class Application(service: Service) {

  def run = {
    signIn

    val dialogs = service.getDialogs
    val userId: Int = dialogs
      .getMessages.toArray.filter(_.isInstanceOf[TLMessage]).map(_.asInstanceOf[TLMessage])
      .find(_.getMessage == "Киса").get.getFromId
    service.getHistory(userId)
  }

  private def signIn = {
    val resp = service.sendCode
    println("code:")
    val code = scala.io.StdIn.readLine()
    service.signIn(code, resp.getPhoneCodeHash)
  }
}