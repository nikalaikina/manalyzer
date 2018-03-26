package org.github.nikalaikina.manalyzer

import org.github.nikalaikina.manalyzer.tg.{ApiFactory, Service}
import org.telegram.api.contact.TLContact
import org.telegram.api.contacts.TLAbsContacts
import org.telegram.api.message.TLMessage

import scala.collection.immutable


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

//    val friends = service.getContacts
//    friends.filter(_.get)

    val dialogs = service.getDialogs
    val userId: Int = dialogs
      .getMessages.toArray.filter(_.isInstanceOf[TLMessage]).map(_.asInstanceOf[TLMessage])
      .find(_.getMessage == "хз").get.getFromId
    val history: Seq[Message] = service.getHistory(userId)
    println("done")
  }

  private def signIn = {
    val resp = service.sendCode(Setup.phone)
    println("code:")
    val code = scala.io.StdIn.readInt()
    service.signIn(Setup.phone, code.toString, resp.getPhoneCodeHash)
  }

}