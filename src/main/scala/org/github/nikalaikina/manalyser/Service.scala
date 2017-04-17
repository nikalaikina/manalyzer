package org.github.nikalaikina.manalyser

import akka.util.Timeout
import org.github.nikalaikina.manalyser.dao.MessageDao
import org.telegram.api.engine.{RpcException, TelegramApi}
import org.telegram.api.functions.auth.{TLRequestAuthSendCode, TLRequestAuthSignIn}
import org.telegram.api.functions.messages.{TLRequestMessagesGetDialogs, TLRequestMessagesGetHistory}
import org.telegram.api.input.peer.TLInputPeerUser
import org.telegram.api.messages.TLAbsMessages

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

class Service(api: TelegramApi) {
  import Setup._

  implicit val ec = scala.concurrent.ExecutionContext.global
  val t = 8 hours

  val dao = new MessageDao()

  var myId: Int = _

  def sendCode = {
    val requestAuthSendCode = new TLRequestAuthSendCode()
    requestAuthSendCode.setApiId(apiId)
    requestAuthSendCode.setApiHash("3d9eca44d19a205c079d0508772dbee7")
    requestAuthSendCode.setPhoneNumber(phone)
    doRpcCall { () => api.doRpcCallNonAuth(requestAuthSendCode) }
  }

  def signIn(code: String, hash: String): Unit = {
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

  def getHistory(userId: Int): Vector[Message] = {
    val ans = getAll { (offset, count) =>
      val getHistoryReq = new TLRequestMessagesGetHistory()
      val user = new TLInputPeerUser()
      user.setUserId(userId)
      getHistoryReq.setPeer(user)
      getHistoryReq.setLimit(count)
      getHistoryReq.setAddOffset(offset)
      val call: TLAbsMessages = doRpcCall { () => api.doRpcCall(getHistoryReq) }
      Message.convert(call.getMessages, myId)
    }
    Await.result(ans, t)
  }

  def doRpcCall[T](f: () => T, attempts: Int = 3): T = {
    Try(f()) match {
      case Success(result) =>
        result
      case Failure(exception: Throwable) if attempts > 0 =>
        println(exception)
        exception match {
          case e: RpcException if e.getErrorCode == 303 =>
            val destDC = if (e.getErrorTag.startsWith("NETWORK_MIGRATE_")) {
              Integer.parseInt(e.getErrorTag.substring("NETWORK_MIGRATE_".length()))
            } else if (e.getErrorTag.startsWith("PHONE_MIGRATE_")) {
              Integer.parseInt(e.getErrorTag.substring("PHONE_MIGRATE_".length()))
            } else /*if (e.getErrorTag.startsWith("USER_MIGRATE_")) */ {
              Integer.parseInt(e.getErrorTag.substring("USER_MIGRATE_".length()))
            }
            api.switchToDc(destDC)
          case e: RpcException if e.getErrorCode == 500 && e.getErrorTag == "AUTH_RESTART" =>
            // retry
          case e: RpcException if e.getErrorTag.startsWith("FLOOD_WAIT_") =>
            val secs = Integer.parseInt(e.getErrorTag.substring("FLOOD_WAIT_".length()))
            Thread.sleep(1000 * (secs + 1))
          case e: Throwable =>
            Thread.sleep(1000 * 2)
        }
        doRpcCall(f, attempts - 1)
      case Failure(exception: Throwable) =>
        null.asInstanceOf[T]
    }
  }

  def getAll(f: (Int, Int) => Vector[Message]): Future[Vector[Message]] = {
    val count = 100
    def getNext(offset: Int, acc: Vector[Message]): Vector[Message] = {
      val res = f(offset, count)
      dao.insertAll(res)
      val next = acc ++ res
      if (res.size < count)
        next
      else
        getNext(offset + count, next)
    }
    dao.count.map(_.toInt).map(getNext(_, Vector.empty))
  }

}
