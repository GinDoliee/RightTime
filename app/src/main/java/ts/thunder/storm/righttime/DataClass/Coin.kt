package ts.thunder.storm.righttime.DataClass

data class Coin(
    var name:String,
    var price:Double = 0.0,
    var change:Float = 0.0F,
    var absChange:Double = 0.0,
    var compare:Double = 0.0
)

data class CoinComp(

    var nameFirst:String,
    var nameSecond:String,

    var priceFirstLeft:Double = 0.0,
    var priceFirstRight:Double = 0.0,

    var changeFirstLeft:Float = 0.0F,
    var changeFirstRight:Float = 0.0F,

    var changeFirstLeftComp:Double = 0.0,
    var changeFirstRightComp:Double = 0.0,



    var compareLeft:Double = 0.0,
    var compareRight:Double = 0.0
)
