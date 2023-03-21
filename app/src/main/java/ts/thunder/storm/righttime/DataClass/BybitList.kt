package ts.thunder.storm.righttime.DataClass

import com.google.gson.annotations.SerializedName

// https://api.bybit.com/spot/v3/public/quote/ticker/24hr

data class BybitList(
  val retCode: Int,
  val retMsg: String,
  val result: Result,
  val retExtInfo: RetExtInfo,
  val time: Long
)

data class Result(
  val list: List<Crypto>
)

data class Crypto(
  val t: Long,
  val s: String,
  val bp: String,
  val ap: String,
  val lp: String,
  val o: String,
  val h: String,
  val l: String,
  val v: String,
  val qv: String
)

data class RetExtInfo(
  val errorCode: String,
  val errorMsg: String
)

