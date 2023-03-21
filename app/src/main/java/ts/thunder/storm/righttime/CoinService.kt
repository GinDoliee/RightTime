package ts.thunder.storm.righttime

import android.content.Context
import android.graphics.Color
import android.icu.text.DecimalFormat
import android.util.Log
import android.widget.TextView
import kotlinx.coroutines.*
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


    private var coinList = mutableListOf<String>()

    private var CoinListFirst = mutableListOf<Coin>()
    private var CoinListSecond = mutableListOf<Coin>()



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
            ServiceUpbit.getCoinAllInfo().enqueue(object :Callback<List<UpbitList>>{
                override fun onResponse(call: Call<List<UpbitList>>, response: Response<List<UpbitList>>) {

                    for(i in 0 until response.body()!!.size){

                        var temp = response.body()!!.get(i).market
                        if(temp.contains("KRW-") == true){
                            CoinListFirst.add(Coin(temp.substring(4,temp.length)))
                        }
                    }

                    Log.d("Hey", "CoinListFirst = ${CoinListFirst}")
                    scopeUpbit.cancel()
                    callback.invoke(true)

                }

                override fun onFailure(call: Call<List<UpbitList>>, t: Throwable) {
                    callback.invoke(false)
                }


            })
        }
    }

    fun GetAllBybit(callback: (Boolean)->Unit){
        scopeBybit.launch {
            ServiceBybit.getCoinAllInfo().enqueue(object : Callback<BybitList> {
                override fun onResponse(call: Call<BybitList>, response: Response<BybitList>) {

                        for(i in 0 until response.body()!!.result.list.size){
                            var temp = response.body()!!.result.list.get(i)
                            if(temp.s.substring(temp.s.length-4,temp.s.length).equals("USDT") == true){
                                CoinListSecond.add(Coin(temp.s.substring(0,temp.s.length-4)))
                            }
                        }

                    Log.d("Hey", "CoinListSecond = ${CoinListSecond}")
                    scopeBybit.cancel()
                    callback.invoke(true)
                }

                override fun onFailure(call: Call<BybitList>, t: Throwable) {
                    callback.invoke(false)
                }

            })
        }
    }

    fun GetCommonCoinList(callback: ()->Unit){
        for(i in 0 until CoinListFirst.size){
            for(j in 0 until CoinListSecond.size){
                if(CoinListFirst.get(i).name.equals(CoinListSecond.get(j).name)) {
                    coinList.add(CoinListFirst.get(i).name)
                }
            }
        }
        Log.d("Hey", "coinList = ${coinList}")
        callback.invoke()
    }




}