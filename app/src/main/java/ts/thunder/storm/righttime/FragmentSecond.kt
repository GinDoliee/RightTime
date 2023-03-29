package ts.thunder.storm.righttime

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import ts.thunder.storm.righttime.DataClass.Coin
import ts.thunder.storm.righttime.DataClass.CoinComp
import ts.thunder.storm.righttime.databinding.FragmentFirstBinding
import ts.thunder.storm.righttime.databinding.FragmentSecondBinding

class FragmentSecond : Fragment() {

    val MAX = 15
    lateinit var mainActivity: MainActivity
    lateinit var binding : FragmentSecondBinding

    val scopeUpbitUpdate = CoroutineScope(Dispatchers.Default + Job())
    val scopeMexcUpdate = CoroutineScope(Dispatchers.Default + Job())

    val channelUpbit = Channel<Boolean>()
    val channelMexc = Channel<Int>()



    private var CoinCompList = mutableListOf<CoinComp>()
    private var CoinFirstTempList = mutableListOf<Coin>()
    private var CoinSecondTempList = mutableListOf<Coin>()

    init {
        for(i in 0 until MAX){
            CoinCompList.add(CoinComp("None","None"))
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSecondBinding.inflate(inflater,container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = context as MainActivity

        GlobalScope.launch(Dispatchers.Main) {

            channelUpbit.consumeEach {
                if(it == true){
                    //   binding.recyclerView1.adapter?.notifyDataSetChanged()
                    Thread.sleep(50)
                    UpdateUpbit()
                }else{
                    Thread.sleep(100)
                    UpdateUpbit()
                }
            }
        }

        GlobalScope.launch(Dispatchers.Main) {
            channelMexc.consumeEach {
                if(it < mainActivity.SingleCoinService.GetCoinListSecondPage().size){
                    UpdateMexc(it)
                }else{
                    UpdateMexc(0)
                    SetMainData()
                    binding.recyclerMainView.adapter?.notifyDataSetChanged()
                }
            }
        }

        mainActivity.SingleCoinService.GetAllMexc({x->if(x == true){
            mainActivity.SingleCoinService.GetCommonCoinSecondList({x->if(x == true){
                        mainActivity.SingleCoinService.UpdateMexc(0,{x->if(x==true){
                                UpdateUpbit()
                                UpdateMexc(0)
                            }
                        })
                    }
                })
            }
        })


        binding.recyclerMainView.layoutManager = LinearLayoutManager(activity)
        binding.recyclerMainView.adapter = AdapterFirst(CoinCompList)

    }

    fun UpdateMexc(index : Int) {
        scopeMexcUpdate.launch {
            mainActivity.SingleCoinService.UpdateMexc(index, { x ->
                if (x == true) {
                    runBlocking {
                        channelMexc.send(index+1)
                    }
                } else {
                    runBlocking {
                        channelMexc.send(index)
                    }
                }
            })
        }
    }

    fun UpdateUpbit(){
        scopeUpbitUpdate.launch {
            mainActivity.SingleCoinService.UpdateSecondUpbit{ x->if(x == true){
                runBlocking {
                    channelUpbit.send(true)
                }
            }else{
                runBlocking {
                    channelUpbit.send(false)
                }
            }
            }
        }
    }

    fun SetMainData(){

        if(CoinFirstTempList.size == 0 || CoinSecondTempList.size ==0) {
            for (i in 0 until mainActivity.SingleCoinService.GetCoinListFirstPage().size) {
                CoinFirstTempList.add(Coin("None"))
                CoinSecondTempList.add(Coin("None"))
            }
        }


        val temp1 = mainActivity.SingleCoinService.GetCoinThirdList()
        for(i in 0 until mainActivity.SingleCoinService.GetCoinListSecondPage().size){
            val cpy = temp1.get(i).copy()
            CoinFirstTempList.set(i,Coin(cpy.name,cpy.price,cpy.change,cpy.absChange,cpy.compare))
        }

        val temp2 = mainActivity.SingleCoinService.GetCoinForthList()
        for(i in 0 until mainActivity.SingleCoinService.GetCoinListSecondPage().size){
            val cpy = temp2.get(i).copy()
            CoinSecondTempList.set(i,Coin(cpy.name,cpy.price,cpy.change,cpy.absChange,cpy.compare))
        }

        CoinFirstTempList.sortWith(compareBy{it.compare})
        CoinSecondTempList.sortWith(compareBy{it.compare})

        for(i in 0 until MAX){
            val temp1_1 = CoinFirstTempList.get(i).copy()   //X1
            var temp1_2 = Coin("None")
            val temp2_2 = CoinSecondTempList.get(i).copy()  //Y2
            var temp2_1 = Coin("None")


            for(j in 0 until CoinSecondTempList.size){
                if(temp1_1.name.equals(CoinSecondTempList.get(j).name) == true){
                    temp2_1 = CoinSecondTempList.get(j)
                }
            }

            for(j in 0 until CoinFirstTempList.size){
                if(temp2_2.name.equals(CoinFirstTempList.get(j).name) == true){
                    temp1_2 = CoinFirstTempList.get(j)
                }
            }

            val currency = mainActivity.SingleCoinService.GetCurrency()

            temp2_1.price = temp2_1.price/currency
            temp2_2.price = temp2_2.price/currency

            var temp1 = 1000000 / temp1_1.price * temp2_1.price / temp2_2.price * temp1_2.price
            var temp2 = 1000000 / temp2_1.price * temp1_1.price / temp1_2.price * temp2_2.price

            CoinCompList.set(i,CoinComp(temp1_1.name,temp2_2.name,temp1_1.price,temp2_2.price,temp1_1.change,temp2_2.change,temp1_1.compare, temp2_2.compare, temp1,temp2))

        }


    }

}