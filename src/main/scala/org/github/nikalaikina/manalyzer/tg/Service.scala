package org.github.nikalaikina.manalyzer.tg

import org.github.nikalaikina.manalyzer.dao.MessageDao
import org.github.nikalaikina.manalyzer.{Message, Setup}
import org.telegram.api.contacts.TLContacts
import org.telegram.api.engine.TelegramApi
import org.telegram.api.functions.auth.{TLRequestAuthSendCode, TLRequestAuthSignIn}
import org.telegram.api.functions.contacts.TLRequestContactsGetContacts
import org.telegram.api.functions.messages.{TLRequestMessagesGetDialogs, TLRequestMessagesGetHistory}
import org.telegram.api.input.peer.TLInputPeerUser
import org.telegram.api.messages.TLAbsMessages
import org.telegram.api.user.TLUser
import info.mukel.telegrambot4s.models.{Message => BotMessage}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class Service(override val api: TelegramApi) extends RpcUtil {
  import Setup._

  implicit val ec = scala.concurrent.ExecutionContext.global
  val t = 8 hours

  val dao = new MessageDao()

  var myId: Int = _

  def sendCode(number: String = phone) = {
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

  def getDialogs = {
    val getDialogsReq = new TLRequestMessagesGetDialogs()
    getDialogsReq.setOffsetPeer(new TLInputPeerUser())
    getDialogsReq.setLimit(200)
    doRpcCall { () => api.doRpcCall(getDialogsReq) }
  }

  def getContacts = {
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

  def getHistory(userId: Int): Vector[Message] = {
    getAll { (offset, count) =>
      val getHistoryReq = new TLRequestMessagesGetHistory()
      val user = new TLInputPeerUser()
      user.setUserId(userId)
      getHistoryReq.setPeer(user)
      getHistoryReq.setLimit(count)
      getHistoryReq.setAddOffset(offset)
      val call: TLAbsMessages = doRpcCall { () => api.doRpcCall(getHistoryReq) }
      Message.convert(call.getMessages, myId)
    }
  }

  def getHistory(me: Long, userId: Long): Iterable[Message] = {
    dao.find(me, userId)
  }

  def getHistory(msg: BotMessage): Iterable[Message] = {
    val userId = msg.forwardFrom.get.id
    val me = msg.from.get.id
    dao.find(me, userId)
  }

  def persistHistory(msg: BotMessage): Unit = {
    val count = 100
    val userId = msg.forwardFrom.get.id
    val me = msg.from.get.id
    val offset = dao.count(me, userId)
    def getNext(offset: Long = 0, acc: Vector[Message] = Vector.empty): Unit = {
      val res = {
        val getHistoryReq = new TLRequestMessagesGetHistory()
        val user = new TLInputPeerUser()
        user.setUserId(userId)
        getHistoryReq.setPeer(user)
        getHistoryReq.setLimit(count)
        getHistoryReq.setAddOffset(offset.toInt)
        val call: TLAbsMessages = doRpcCall { () => api.doRpcCall(getHistoryReq) }
        Message.convert(call.getMessages, myId)
      }
      dao.insertAll(res)
      val next = acc ++ res
      if (res.size == count) getNext(offset + count, next)
    }
    getNext(offset)
  }

  def getAll(f: (Int, Int) => Vector[Message], persist: Boolean = false): Vector[Message] = {
    val count = 100
    def getNext(offset: Int = 0, acc: Vector[Message] = Vector.empty): Vector[Message] = {
      val res = f(offset, count)
      if (persist) dao.insertAll(res)
      val next = acc ++ res
      if (res.size < count)
        next
      else
        getNext(offset + count, next)
    }
    getNext()
  }

}
