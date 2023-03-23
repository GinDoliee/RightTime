package ts.thunder.storm.righttime

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import ts.thunder.storm.righttime.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    val SingleCoinService = CoinService.getInstance(this)

    val scopeUpbitUpdate = CoroutineScope(Dispatchers.Default + Job())
    val scopeByBitUpdate = CoroutineScope(Dispatchers.Default + Job())

    val channelUpbit = Channel<Int>()
    val channelBybit = Channel<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        SingleCoinService.SetExChange("FRX.KRWUSD",{
            Log.d("Hey","$it")
        })


        GlobalScope.launch(Dispatchers.Main) {

            channelUpbit.consumeEach {
                if(it < SingleCoinService.GetCoinList().size){
                    Thread.sleep(50)
                    UpdateUpbit(it)
                }else{
                    UpdateUpbit(0)
                }
            }
        }

        GlobalScope.launch(Dispatchers.Main) {
            channelBybit.consumeEach {
                if(it < SingleCoinService.GetCoinList().size){

                    UpdateByBit(it)
                }else{
                    UpdateByBit(0)
                }
            }
        }


        SingleCoinService.GetAllUpbit({x->if(x == true){
            SingleCoinService.GetAllBybit({x->if(x ==true){
                SingleCoinService.GetCommonCoinList({
                    UpdateUpbit(0)
                    UpdateByBit(0)
                    binding.viewpager.adapter = MyAdapter(this)
                })
            } })
        } })

    }


    fun UpdateUpbit(index : Int){

        scopeUpbitUpdate.launch {

            SingleCoinService.UpdateUpbit(index) { x ->if(x == true) {
                    runBlocking {
                        channelUpbit.send(index + 1)
                    }
                }else{
                    runBlocking {
                        Thread.sleep(50)
                        channelUpbit.send(index)
                    }
                }
            }
        }
    }

    fun UpdateByBit(index : Int){
        scopeByBitUpdate.launch {

            SingleCoinService.UpdateBybit(index) { x ->if(x == true) {
                runBlocking {
                    channelBybit.send(index + 1)
                }
            }else{
                runBlocking {
                    Thread.sleep(50)
                    channelBybit.send(index)
                }
            }
            }
        }
    }
}


class MyAdapter(activity: FragmentActivity): FragmentStateAdapter(activity){

    val FragmentList:List<Fragment>
    init{
        FragmentList = listOf(FragmentFirst(),FragmentSecond(),FragmentThird())
    }

    override fun getItemCount(): Int {
        return FragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return FragmentList[position]
    }

}