package ts.thunder.storm.righttime

import android.icu.text.DecimalFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ts.thunder.storm.righttime.DataClass.Coin

import ts.thunder.storm.righttime.databinding.ItemDataFirstBinding

class AdapterFirst(val data: MutableList<Coin>):RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val decimal = DecimalFormat("#,###.##")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return HolderFirst(ItemDataFirstBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as HolderFirst).binding

        val item = data.get(position)
        binding.textName.text = item.name
        binding.textPrice.text = decimal.format(item.price).toString()
        binding.textChange.text = item.change.toString()
        binding.textCompare.text = item.compare.toString()
    }

    override fun getItemCount(): Int {
        return data.size
    }


}

class HolderFirst(val binding: ItemDataFirstBinding):RecyclerView.ViewHolder(binding.root){

}