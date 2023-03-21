package ts.thunder.storm.righttime

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import ts.thunder.storm.righttime.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    val SingleCoinService = CoinService.getInstance(this)

    init {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        SingleCoinService.SetExChange("FRX.KRWUSD",{
            Log.d("Hey","$it")
        })

        SingleCoinService.GetAllUpbit({x->if(x == true){
            SingleCoinService.GetAllBybit({x->if(x ==true){
                SingleCoinService.GetCommonCoinList({
                    Log.d("Hey","Done")



                    binding.viewpager.adapter = MyAdapter(this)
                })
            } })
        } })

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