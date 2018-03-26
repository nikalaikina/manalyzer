package org.github.nikalaikina.manalyzer.tg

import org.telegram.api.functions.auth.{TLRequestAuthSendCode, TLRequestAuthSignIn}
import org.telegram.api.functions.contacts.TLRequestContactsGetContacts
import org.telegram.api.functions.messages.{TLRequestMessagesGetDialogs, TLRequestMessagesGetHistory}
import org.telegram.api.input.peer.TLInputPeerUser

object RequestBuilder {

  def getHistoryRequest(userId: Int, offset: Int, count: Int): TLRequestMessagesGetHistory = {
    val getHistoryReq = new TLRequestMessagesGetHistory()
    val user = new TLInputPeerUser()
    user.setUserId(userId)
    getHistoryReq.setPeer(user)
    getHistoryReq.setLimit(count)
    getHistoryReq.setAddOffset(offset)
    getHistoryReq
  }

  def signInRequest(code: String, hash: String, number: String): TLRequestAuthSignIn = {
    val signInReq = new TLRequestAuthSignIn()
    signInReq.setPhoneCode(code)
    signInReq.setPhoneNumber(number)
    signInReq.setPhoneCodeHash(hash)
    signInReq
  }

  def sendCodeRequest(number: String, apiId: Int, apiHash: String): TLRequestAuthSendCode = {
    val requestAuthSendCode = new TLRequestAuthSendCode()
    requestAuthSendCode.setApiId(apiId)
    requestAuthSendCode.setApiHash(apiHash)
    requestAuthSendCode.setPhoneNumber(number)
    requestAuthSendCode
  }


  def getDialogsRequest: TLRequestMessagesGetDialogs = {
    val getDialogsReq = new TLRequestMessagesGetDialogs()
    getDialogsReq.setOffsetPeer(new TLInputPeerUser())
    getDialogsReq.setLimit(200)
    getDialogsReq
  }

  def getContactsRequest: TLRequestContactsGetContacts = {
    new TLRequestContactsGetContacts()
  }
}
