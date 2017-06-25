package org.github.nikalaikina.manalyzer

import com.typesafe.config.ConfigFactory
import org.telegram.bot.kernel.engine.MemoryApiState

object Setup {

  val config = ConfigFactory.load()

  val apiId = config.getInt("tg-api.app-id")
  val state = new MemoryApiState("mem_api_state")
  val phone = config.getString("tg-api.phone")

}
