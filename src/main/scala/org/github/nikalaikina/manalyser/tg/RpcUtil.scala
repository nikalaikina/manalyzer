package org.github.nikalaikina.manalyser.tg

import org.telegram.api.engine.{RpcException, TelegramApi}

import scala.util.{Failure, Success, Try}

trait RpcUtil {
  val api: TelegramApi

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
}
