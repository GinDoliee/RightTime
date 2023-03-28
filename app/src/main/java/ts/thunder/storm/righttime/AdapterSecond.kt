package ts.thunder.storm.righttime

import android.icu.text.DecimalFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ts.thunder.storm.righttime.DataClass.Coin
import ts.thunder.storm.righttime.databinding.ActivityMainBinding
import ts.thunder.storm.righttime.databinding.ItemDataFirstBinding
import ts.thunder.storm.righttime.databinding.ItemDataSecondBinding

class AdapterSecond(val data: MutableList<Coin>):RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val decimal = DecimalFormat("#,###.##")
    val decimalCompare = DecimalFormat("#,###.###")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return HolderSecond(ItemDataSecondBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as HolderSecond).binding

        val item = data.get(position)

    }

    override fun getItemCount(): Int {
        return data.size
    }


}

class HolderSecond(val binding: ItemDataSecondBinding):RecyclerView.ViewHolder(binding.root){

}