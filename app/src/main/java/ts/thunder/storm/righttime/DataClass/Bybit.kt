package ts.thunder.storm.righttime.DataClass

data class Bybit(
  val retCode: Int,
  val retMsg: String,
  val result: TickerResult

)

data class TickerResult(
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



