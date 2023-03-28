package ts.thunder.storm.righttime.DataClass

data class Upbit(
    val market: String,
    val trade_price: Double,

    //val opening_price: Double,
    //val high_price: Double,
    //val low_price: Double,

    val change: String,
    //val change_price: String,
    //val change_rate: String,

    val signed_change_rate : Double,
    val signed_change_price : Double,
    //val acc_trade_price_24h: Double

)
