package org.github.nikalaikina.manalyser.tg

import org.github.nikalaikina.manalyser.Setup
import org.telegram.api.engine.{ApiCallback, AppInfo, TelegramApi}
import org.telegram.api.updates.TLAbsUpdates

object ApiFactory {
  import Setup._

  def api = {
    val ans = new TelegramApi(
      state,
      new AppInfo(apiId, "deviceModel", "systemVersion", "1", "scala"),
      new ApiCallback {
        override def onUpdate(updates: TLAbsUpdates): Unit = {
          println(updates)
        }

        override def onUpdatesInvalidated(api: TelegramApi): Unit = {
          println(api)
        }

        override def onAuthCancelled(api: TelegramApi): Unit = {
          println(api)
        }
      }
    )
    ans.switchToDc(2)
    ans
  }
}
