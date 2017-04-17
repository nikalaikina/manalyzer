package org.github.nikalaikina.manalyser

import com.googlecode.charts4j.Plots
import org.telegram.api.auth.TLSentCode
import org.telegram.api.chat.TLChatEmpty
import org.telegram.api.engine.{ApiCallback, AppInfo, RpcException, TelegramApi}
import org.telegram.api.engine.storage.AbsApiState
import org.telegram.api.functions.auth.{TLRequestAuthSendCode, TLRequestAuthSignIn}
import org.telegram.api.functions.messages.{TLRequestMessagesGetDialogs, TLRequestMessagesGetHistory}
import org.telegram.api.input.peer.{TLInputPeerEmpty, TLInputPeerUser}
import org.telegram.api.message.{TLAbsMessage, TLMessage, TLMessageService}
import org.telegram.api.messages.TLAbsMessages
import org.telegram.api.messages.dialogs.TLAbsDialogs
import org.telegram.api.updates.TLAbsUpdates
import org.telegram.bot.kernel.engine.MemoryApiState
import org.telegram.mtproto.pq.Authorizer
import org.telegram.mtproto.state.ConnectionInfo


object Main extends App {

  try {
    val service = new Service(ApiFactory.api)

    val resp = service.sendCode

    println("code:")
    val code = scala.io.StdIn.readLine()
    service.signIn(code, resp.getPhoneCodeHash)
    val dialogs = service.getDialogs
    val maxId = dialogs
      .getMessages.toArray.filter(_.isInstanceOf[TLMessage]).map(_.asInstanceOf[TLMessage])
      .find(_.getMessage == "Киса").get.getFromId
    val history = service.getHistory(maxId)

    println("hello world")
  } catch {
    case x: Throwable =>
      println(x)
      throw x
  }



}