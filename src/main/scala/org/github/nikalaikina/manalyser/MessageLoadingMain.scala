package org.github.nikalaikina.manalyser

import org.github.nikalaikina.manalyser.tg.{ApiFactory, Service}
import org.telegram.api.contact.TLContact
import org.telegram.api.contacts.TLAbsContacts
import org.telegram.api.message.TLMessage


object MessageLoadingMain extends App {

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

    val friends = service.getContacts
//    friends.filter(_.get)

    val dialogs = service.getDialogs
    val userId: Int = dialogs
      .getMessages.toArray.filter(_.isInstanceOf[TLMessage]).map(_.asInstanceOf[TLMessage])
      .find(_.getMessage == "лалала").get.getFromId
    service.getHistory(userId)
  }

  private def signIn = {
    val resp = service.sendCode(Setup.phone)
    println("code:")
    val code = scala.io.StdIn.readLine()
    service.signIn(Setup.phone, code, resp.getPhoneCodeHash)
  }

}