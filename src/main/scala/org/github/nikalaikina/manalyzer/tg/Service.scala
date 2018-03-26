package org.github.nikalaikina.manalyzer.tg

import info.mukel.telegrambot4s.models.{Message => BotMessage}
import org.github.nikalaikina.manalyzer.{Message, Setup}
import org.telegram.api.auth.TLSentCode
import org.telegram.api.contacts.TLContacts
import org.telegram.api.engine.TelegramApi
import org.telegram.api.functions.auth.{TLRequestAuthSendCode, TLRequestAuthSignIn}
import org.telegram.api.functions.contacts.TLRequestContactsGetContacts
import org.telegram.api.functions.messages.{TLRequestMessagesGetDialogs, TLRequestMessagesGetHistory}
import org.telegram.api.input.peer.TLInputPeerUser
import org.telegram.api.messages.TLAbsMessages
import org.telegram.api.messages.dialogs.TLAbsDialogs
import org.telegram.api.user.TLUser

import scala.concurrent.ExecutionContextExecutor
import scala.language.postfixOps

class Service(override val api: TelegramApi) extends RpcUtil {
  import Setup._

  var myId: Int = _

  def sendCode(number: String = phone): TLSentCode = {
    val requestAuthSendCode = new TLRequestAuthSendCode()
    requestAuthSendCode.setApiId(apiId)
    requestAuthSendCode.setApiHash(apiHash)
    requestAuthSendCode.setPhoneNumber(number)
    doRpcCall { () => api.doRpcCallNonAuth(requestAuthSendCode) }
  }

  def signIn(number: String = phone, code: String, hash: String): Unit = {
    val signInReq = new TLRequestAuthSignIn()
    signInReq.setPhoneCode(code)
    signInReq.setPhoneNumber(phone)
    signInReq.setPhoneCodeHash(hash)
    val resp = doRpcCall { () => api.doRpcCallNonAuth(signInReq) }
    myId = resp.getUser.getId
    state.setAuthenticated(state.getPrimaryDc, true)
  }

  def getDialogs: TLAbsDialogs = {
    val getDialogsReq = new TLRequestMessagesGetDialogs()
    getDialogsReq.setOffsetPeer(new TLInputPeerUser())
    getDialogsReq.setLimit(200)
    doRpcCall { () => api.doRpcCall(getDialogsReq) }
  }

  def getContacts: Array[TLUser] = {
    val req = new TLRequestContactsGetContacts()
    val call = doRpcCall { () => api.doRpcCall(req) }
    val users = call
      .asInstanceOf[TLContacts]
      .getUsers
    val array = users.toArray
    array
      .filter(_ != null)
      .filter(_.isInstanceOf[TLUser])
      .map(_.asInstanceOf[TLUser])
  }

  def getHistory(userId: Int, persist: Vector[Message] => Unit = _ => Unit): Vector[Message] = {
    getAll( { (offset, count) =>
      val getHistoryReq = new TLRequestMessagesGetHistory()
      val user = new TLInputPeerUser()
      user.setUserId(userId)
      getHistoryReq.setPeer(user)
      getHistoryReq.setLimit(count)
      getHistoryReq.setAddOffset(offset)
      val call: TLAbsMessages = doRpcCall { () => api.doRpcCall(getHistoryReq) }
      Message.convert(call.getMessages, myId)
    }, persist)
  }

  def getAll(f: (Int, Int) => Vector[Message], persist: Vector[Message] => Unit): Vector[Message] = {
    val count = 100
    def getNext(offset: Int = 0, acc: Vector[Message] = Vector.empty): Vector[Message] = {
      val res = f(offset, count)
      persist(res)
      val next = acc ++ res
      if (res.size < count)
        next
      else
        getNext(offset + count, next)
    }
    getNext()
  }

}
