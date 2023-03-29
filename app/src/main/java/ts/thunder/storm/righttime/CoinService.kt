package ts.thunder.storm.righttime

import android.content.Context
import android.graphics.Color
import android.icu.text.DecimalFormat
import android.util.Log
import android.widget.TextView
import com.google.firebase.firestore.util.Util.comparator

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import retrofit2.*

import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ts.thunder.storm.righttime.DataClass.*

import java.lang.Math.abs

class CoinService private constructor() {

    companion object {
        private var instance: CoinService? = null
        private lateinit var context: Context

        fun getInstance(_context: Context): CoinService {
            return instance ?: synchronized(this) {
                instance ?: CoinService().also {
                    context = _context
                    instance = it
                }
            }
        }

        interface ApiExChange{
            @GET("v1/forex/recent")
            fun getExChangeInfo(
                @Query("codes")arg:String
            ): retrofit2.Call<List<ExChange>>
        }

        val RetrofitExChange = Retrofit.Builder()
            .baseUrl("https://quotation-api-cdn.dunamu.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        interface ApiUpbit{
            @GET("v1/market/all")
            fun getCoinAllInfo(
            ): retrofit2.Call<List<UpbitList>>

            @GET("v1/ticker")
            fun getCoinInfo(
                @Query("markets")arg:String
            ): retrofit2.Call<List<Upbit>>
        }
        val RetrofitUpbit = Retrofit.Builder()
            .baseUrl("https://api.upbit.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()


        interface ApiBybit{
            @GET("spot/v3/public/quote/ticker/24hr")
            fun getCoinAllInfo(
            ): retrofit2.Call<BybitList>

            @GET("spot/v3/public/quote/ticker/24hr")
            fun getCoinInfo(
                @Query("symbol")arg:String
            ): retrofit2.Call<Bybit>
        }

        val RetrofitBybit = Retrofit.Builder()
            .baseUrl("https://api.bybit.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()



        interface ApiMexc{
            @GET("api/v3/exchangeInfo")
            fun getCoinAllInfo(
            ): retrofit2.Call<Mexc>

            @GET("api/v3/ticker/price")
            fun getCoinInfo(
                @Query("symbol")arg:String
            ): retrofit2.Call<MexcPrice>
        }

        val RetrofitMexc = Retrofit.Builder()
            .baseUrl("https://api.mexc.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()


        val ServiceExChange = RetrofitExChange.create(ApiExChange::class.java)
        val ServiceUpbit = RetrofitUpbit.create(ApiUpbit::class.java)
        val ServiceBybit = RetrofitBybit.create(ApiBybit::class.java)
        val ServiceMexc = RetrofitMexc.create(ApiMexc::class.java)



    }

    val scopeExChange = CoroutineScope(Dispatchers.Default + Job())
    val scopeUpbit = CoroutineScope(Dispatchers.Default + Job())
    val scopeBybit = CoroutineScope(Dispatchers.Default + Job())
    val scopeMexc = CoroutineScope(Dispatchers.Default + Job())



    private var CurrentCurrency = 0.0
    private var CoinListFirstPage = mutableListOf<String>()
    private var CoinListSecondPage = mutableListOf<String>()

    private var CoinFirstList = mutableListOf<Coin>()
    private var CoinSecondList = mutableListOf<Coin>()
    private var CoinThirdList = mutableListOf<Coin>()
    private var CoinForthList = mutableListOf<Coin>()


    fun GetCurrency():Double{
        return CurrentCurrency
    }


    fun GetCoinListFirstPage():List<String>{
        return CoinListFirstPage
    }

    fun GetCoinFirstList():List<Coin>{
        return CoinFirstList
    }

    fun GetCoinSecondList():List<Coin>{
        return CoinSecondList
    }

    fun GetCoinListSecondPage():List<String>{
        return CoinListSecondPage
    }

    fun GetCoinThirdList():List<Coin>{
        return CoinThirdList
    }

    fun GetCoinForthList():List<Coin>{
        return CoinForthList
    }

    fun SetExChange(value:String, callback: (Double)->Unit){

        var temp = 0.0

        scopeExChange.launch {
            while(scopeExChange.isActive) {
                ServiceExChange.getExChangeInfo(value).enqueue(object : Callback<List<ExChange>> {
                    override fun onResponse(
                        call: retrofit2.Call<List<ExChange>>,
                        response: Response<List<ExChange>>
                    ) {
                        if(temp != (response.body()!!.get(0).basePrice).toDouble()){
                            temp = (response.body()!!.get(0).basePrice).toDouble()
                            CurrentCurrency = temp
                            callback.invoke((response.body()!!.get(0).basePrice).toDouble())
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<List<ExChange>>, t: Throwable) {

                    }
                })
                Thread.sleep(6000)
            }
        }

    }


    fun GetAllUpbit(callback: (Boolean)->Unit){
        scopeUpbit.launch {
            ServiceUpbit.getCoinAllInfo().enqueue(object : Callback<List<UpbitList>> {
                override fun onResponse(
                    call: Call<List<UpbitList>>,
                    response: Response<List<UpbitList>>
                ) {

                    for (i in 0 until response.body()!!.size) {

                        var temp = response.body()!!.get(i).market
                        if (temp.contains("KRW-") == true) {
                            CoinFirstList.add(Coin(temp.substring(4, temp.length)))
                        }
                    }

                    Log.d("Hey", "CoinListFirst = ${CoinFirstList}")
                    callback.invoke(true)
                }

                override fun onFailure(call: Call<List<UpbitList>>, t: Throwable) {
                    callback.invoke(false)
                }
            })
        }
    }

    fun UpdateBybit(index:Int, callback: (Boolean) -> Unit){

        scopeBybit.launch {

        ServiceBybit.getCoinInfo(CoinListFirstPage.get(index)+"USDT").enqueue(object :Callback<Bybit>{
            override fun onResponse(call: Call<Bybit>, response: Response<Bybit>) {

                if(response.body() != null){

                    val temp = response.body()
                    val price = temp!!.result.lp.toDouble() * CurrentCurrency
                    val change = temp.result.o.toFloat()
                    val absChange = temp.result.o.toDouble() - temp.result.lp.toDouble()

                    var comp = 0.0

                    if(CoinFirstList.get(index).price > 0 ) {
                        comp = ((price / CoinFirstList.get(index).price) -1)*100
                    }

                    CoinSecondList.set(index,Coin(CoinListFirstPage.get(index), price,change,absChange,comp))

                    callback.invoke(true)
                }else{
                    callback.invoke(false)
                }
            }

            override fun onFailure(call: Call<Bybit>, t: Throwable) {
                callback.invoke(false)
            }
        })
        }
    }



    fun UpdateUpbit(callback: (Boolean) -> Unit){
        var coins : String = String()
        for(i in 0 until CoinListFirstPage.size){
            coins += "KRW-"
            coins += CoinListFirstPage.get(i)
            coins += ","
        }
        coins = coins.substring(0, coins.length-1)

            ServiceUpbit.getCoinInfo(coins).enqueue(object : Callback<List<Upbit>> {
                override fun onResponse(call: Call<List<Upbit>>,response: Response<List<Upbit>>
                ) {
                    if(response.body() != null){
                        for( i in 0 until response.body()!!.size){
                            val temp = response.body()!!.get(i)
                            val price = temp.trade_price
                            val change = (temp?.signed_change_rate.toString()).toFloat() * 100
                            var absChange = abs(temp.signed_change_price)

                            if(temp.change.equals("RISE") == false){
                                absChange = absChange * -1
                            }
                            var comp = 0.0

                            if(CoinSecondList.get(i).price > 0 ) {
                                comp = ((price / CoinSecondList.get(i).price) -1)*100
                            }

                            CoinFirstList.set(i,Coin(CoinListFirstPage.get(i), price,change,absChange,comp))
                        }

                        callback.invoke(true)
                    }else{
                        callback.invoke(false)
                    }
                }

                override fun onFailure(call: Call<List<Upbit>>, t: Throwable) {
                    Log.d("Hey", "UpBit = onFailure")
                    callback.invoke(false)
                }
            })
    }

    fun GetAllBybit(callback: (Boolean)->Unit){

        ServiceBybit.getCoinAllInfo().enqueue(object : Callback<BybitList> {
            override fun onResponse(call: Call<BybitList>, response: Response<BybitList>) {

                    for(i in 0 until response.body()!!.result.list.size){
                        var temp = response.body()!!.result.list.get(i)
                        if(temp.s.substring(temp.s.length-4,temp.s.length).equals("USDT") == true){
                            CoinSecondList.add(Coin(temp.s.substring(0, temp.s.length - 4)))
                        }
                    }

                Log.d("Hey", "CoinListSecond = ${CoinSecondList}")
                callback.invoke(true)
            }

            override fun onFailure(call: Call<BybitList>, t: Throwable) {
                callback.invoke(false)
            }
        })
    }

    fun GetAllMexc(callback: (Boolean) -> Unit){
        ServiceMexc.getCoinAllInfo().enqueue(object :Callback<Mexc>{
            override fun onResponse(call: Call<Mexc>, response: Response<Mexc>) {
                for(i in 0 until response.body()!!.symbols.size){
                    var temp = response.body()!!.symbols.get(i)
                    if(temp.symbol.substring(temp.symbol.length-4,temp.symbol.length).equals("USDT") == true){
                        CoinThirdList.add(Coin(temp.symbol.substring(0, temp.symbol.length - 4)))
                    }
                }
                callback.invoke(true)
            }

            override fun onFailure(call: Call<Mexc>, t: Throwable) {

            }

        })
    }

    fun UpdateMexc(index:Int, callback: (Boolean) -> Unit){

            ServiceMexc.getCoinInfo(CoinListSecondPage.get(index)+"USDT").enqueue(object :Callback<MexcPrice> {
                override fun onResponse(call: Call<MexcPrice>, response: Response<MexcPrice>) {
                    if(response.body() != null){

                        val temp = response.body()
                        val price = temp!!.price * CurrentCurrency

                        val absChange = 0.0

                        var comp = 0.0

                        if(CoinThirdList.get(index).price > 0 ) {
                            comp = ((price / CoinThirdList.get(index).price) -1)*100
                        }


                        CoinForthList.set(index, Coin(CoinListSecondPage.get(index),price,compare=comp))
                        callback.invoke(true)
                    }
                }

                override fun onFailure(call: Call<MexcPrice>, t: Throwable) {

                }
            })
    }


    fun UpdateSecondUpbit(callback: (Boolean) -> Unit){
        var coins : String = String()
        for(i in 0 until CoinListSecondPage.size){
            coins += "KRW-"
            coins += CoinListSecondPage.get(i)
            coins += ","
        }
        coins = coins.substring(0, coins.length-1)

        ServiceUpbit.getCoinInfo(coins).enqueue(object : Callback<List<Upbit>> {
            override fun onResponse(call: Call<List<Upbit>>,response: Response<List<Upbit>>
            ) {
                if(response.body() != null){
                    for( i in 0 until response.body()!!.size){
                        val temp = response.body()!!.get(i)
                        val price = temp.trade_price
                        val change = (temp?.signed_change_rate.toString()).toFloat() * 100
                        var absChange = abs(temp.signed_change_price)

                        if(temp.change.equals("RISE") == false){
                            absChange = absChange * -1
                        }
                        var comp = 0.0

                        if(CoinForthList.get(i).price > 0 ) {
                            comp = ((price / CoinForthList.get(i).price) -1)*100
                        }

                        CoinThirdList.set(i,Coin(CoinListSecondPage.get(i), price,change,absChange,comp))
                    }

                    callback.invoke(true)
                }else{
                    callback.invoke(false)
                }
            }

            override fun onFailure(call: Call<List<Upbit>>, t: Throwable) {
                Log.d("Hey", "UpBit = onFailure")
                callback.invoke(false)
            }
        })
    }

    fun GetCommonCoinSecondList(callback: (Boolean)->Unit){

        val exceptList = mutableListOf("BTC","ETH","GMT")

        for(i in 0 until CoinFirstList.size){
            for(j in 0 until CoinThirdList.size){
                if(CoinFirstList.get(i).name.equals(CoinThirdList.get(j).name) == true) {
                    CoinListSecondPage.add(CoinFirstList.get(i).name)
                }
            }
        }

        CoinListSecondPage.removeAll(exceptList)

        Log.d("Hey", "coinList = ${CoinListSecondPage}")
        Log.d("Hey", "coinList Size = ${CoinListSecondPage.size}")
        CoinThirdList.clear()
        CoinForthList.clear()


        for(i in 0 until CoinListSecondPage.size){
            CoinThirdList.add(Coin(CoinListSecondPage.get(i)))
            CoinForthList.add(Coin(CoinListSecondPage.get(i)))
        }

        Log.d("Hey2","CoinThirdList : $CoinThirdList")
        Log.d("Hey2","CoinForthList : $CoinForthList")

        callback.invoke(true)
    }

    fun GetCommonCoinList(callback: ()->Unit){

        val exceptList = mutableListOf("BTC","ETH","TON","SRM","BTG","BTT","WAVES")

        for(i in 0 until CoinFirstList.size){
            for(j in 0 until CoinSecondList.size){
                if(CoinFirstList.get(i).name.equals(CoinSecondList.get(j).name) == true) {
                    CoinListFirstPage.add(CoinFirstList.get(i).name)
                }
            }
        }

        CoinListFirstPage.removeAll(exceptList)

        Log.d("Hey", "coinList = ${CoinListFirstPage}")
        Log.d("Hey", "coinList Size = ${CoinListFirstPage.size}")
        CoinFirstList.clear()
        CoinSecondList.clear()


        for(i in 0 until CoinListFirstPage.size){
            CoinFirstList.add(Coin(CoinListFirstPage.get(i)))
            CoinSecondList.add(Coin(CoinListFirstPage.get(i)))
        }
        callback.invoke()
    }






}