package ts.thunder.storm.righttime

import android.icu.text.DecimalFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ts.thunder.storm.righttime.DataClass.Coin
import ts.thunder.storm.righttime.DataClass.CoinComp

import ts.thunder.storm.righttime.databinding.ItemDataFirstBinding
import ts.thunder.storm.righttime.databinding.ItemMainBinding

class AdapterFirst(var data: MutableList<CoinComp>):RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val decimal = DecimalFormat("#,###.##")
    val decimalCompare = DecimalFormat("#,###.#######")
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return HolderFirst(ItemMainBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as HolderFirst).binding
        val item = data.get(position)

        binding.textNameFirst.text = item.nameFirst + "(" + decimal.format(item.changeFirstLeftComp) +")"
        binding.textNameSecond.text = item.nameSecond + "(" + decimal.format(item.changeFirstRightComp) +")"

        binding.textPriceLeftFirst.text = decimalCompare.format(item.priceFirstLeft).toString()
        binding.textPriceRightFirst.text = decimalCompare.format(item.priceFirstRight).toString()

        binding.textCompareLeft.text = decimal.format(item.compareLeft).toString()
        binding.textCompareRight.text = decimal.format(item.compareRight).toString()
    }

    override fun getItemCount(): Int {
        return data.size
    }

}

class HolderFirst(val binding: ItemMainBinding):RecyclerView.ViewHolder(binding.root){

}