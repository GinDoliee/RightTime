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


        val ServiceExChange = RetrofitExChange.create(ApiExChange::class.java)
        val ServiceUpbit = RetrofitUpbit.create(ApiUpbit::class.java)
        val ServiceBybit = RetrofitBybit.create(ApiBybit::class.java)



    }

    val scopeExChange = CoroutineScope(Dispatchers.Default + Job())
    val scopeUpbit = CoroutineScope(Dispatchers.Default + Job())
    val scopeBybit = CoroutineScope(Dispatchers.Default + Job())


    private var CurrentCurrency = 0.0
    private var CoinList = mutableListOf<String>()

    private var CoinFirstList = mutableListOf<Coin>()
    private var CoinSecondList = mutableListOf<Coin>()


    fun GetCurrency():Double{
        return CurrentCurrency
    }


    fun GetCoinList():List<String>{
        return CoinList
    }

    fun GetCoinFirstList():List<Coin>{
        return CoinFirstList
    }

    fun GetCoinSecondList():List<Coin>{
        return CoinSecondList
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


        ServiceBybit.getCoinInfo(CoinList.get(index)+"USDT").enqueue(object :Callback<Bybit>{
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

                    CoinSecondList.set(index,Coin(CoinList.get(index), price,change,absChange,comp))

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
        for(i in 0 until CoinList.size){
            coins += "KRW-"
            coins += CoinList.get(i)
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

                            CoinFirstList.set(i,Coin(CoinList.get(i), price,change,absChange,comp))
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

    fun GetCommonCoinList(callback: ()->Unit){

        val exceptList = mutableListOf("BTC","ETH","TON","SRM","BTG","BTT")

        for(i in 0 until CoinFirstList.size){
            for(j in 0 until CoinSecondList.size){
                if(CoinFirstList.get(i).name.equals(CoinSecondList.get(j).name) == true) {
                    CoinList.add(CoinFirstList.get(i).name)
                }
            }
        }

        CoinList.removeAll(exceptList)

        Log.d("Hey", "coinList = ${CoinList}")
        Log.d("Hey", "coinList Size = ${CoinList.size}")
        CoinFirstList.clear()
        CoinSecondList.clear()


        for(i in 0 until CoinList.size){
            CoinFirstList.add(Coin(CoinList.get(i)))
            CoinSecondList.add(Coin(CoinList.get(i)))
        }
        callback.invoke()
    }




}