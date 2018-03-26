package org.github.nikalaikina.manalyzer.tg

import org.github.nikalaikina.manalyzer.{Message, Setup}
import org.telegram.api.auth.TLSentCode
import org.telegram.api.contacts.TLContacts
import org.telegram.api.engine.TelegramApi
import org.telegram.api.messages.dialogs.TLAbsDialogs
import org.telegram.api.user.TLUser


class Service(override val api: TelegramApi) extends RpcUtil {
  import RequestBuilder._
  import Setup._

  var myId: Int = _

  def sendCode(number: String = phone): TLSentCode = {
    val requestAuthSendCode = sendCodeRequest(number, apiId, apiHash)
    doRpcCall { () => api.doRpcCallNonAuth(requestAuthSendCode) }
  }

  def signIn(number: String = phone, code: String, hash: String): Unit = {
    val signInReq = signInRequest(code, hash, number)
    val resp = doRpcCall { () => api.doRpcCallNonAuth(signInReq) }
    myId = resp.getUser.getId
    state.setAuthenticated(state.getPrimaryDc, true)
  }

  def getDialogs: TLAbsDialogs = {
    doRpcCall { () => api.doRpcCall(getDialogsRequest) }
  }

  def getContacts: Array[TLUser] = {
    val call = doRpcCall { () => api.doRpcCall(getContactsRequest) }
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
      val getHistoryReq = getHistoryRequest(userId, offset, count)
      val call = doRpcCall { () => api.doRpcCall(getHistoryReq) }
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
