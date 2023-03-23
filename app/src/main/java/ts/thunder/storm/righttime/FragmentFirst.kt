package ts.thunder.storm.righttime

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import ts.thunder.storm.righttime.DataClass.Coin
import ts.thunder.storm.righttime.databinding.FragmentFirstBinding
import ts.thunder.storm.righttime.databinding.FragmentThirdBinding


class FragmentFirst : Fragment() {

    lateinit var mainActivity: MainActivity
    lateinit var binding : FragmentFirstBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentFirstBinding.inflate(inflater,container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mainActivity = context as MainActivity

        binding.recyclerView1.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView1.adapter = AdapterFirst(mainActivity.SingleCoinService.GetCoinFirstList() as MutableList<Coin>)

        binding.recyclerView2.layoutManager = LinearLayoutManager(activity)
        binding.recyclerView2.adapter = AdapterSecond(mainActivity.SingleCoinService.GetCoinSecondList() as MutableList<Coin>)
    }
}