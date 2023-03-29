package ts.thunder.storm.righttime.DataClass

data class Mexc(
    val timezone: String,
    val serverTime: Long,
    val symbols: List<Symbol>,
    val rateLimits: List<Any>,
    val exchangeFilters: List<Any>
)

data class Symbol(
    val symbol: String,
    val status: String,
    val baseAsset: String,
    val baseAssetPrecision: Int,
    val quoteAsset: String,
    val quotePrecision: Int,
    val quoteAssetPrecision: Int,
    val baseCommissionPrecision: Int,
    val quoteCommissionPrecision: Int,
    val orderTypes: List<String>,
    val quoteOrderQtyMarketAllowed: Boolean,
    val isSpotTradingAllowed: Boolean,
    val isMarginTradingAllowed: Boolean,
    val quoteAmountPrecision: String,
    val baseSizePrecision: String,
    val permissions: List<String>,
    val filters: List<Any>,
    val maxQuoteAmount: String,
    val makerCommission: String,
    val takerCommission: String,
    val quoteAmountPrecisionMarket: String,
    val maxQuoteAmountMarket: String
)
data class MexcPrice(
    val symbol: String,
    var price:Double = 0.0
)