package tech.kicky.hms.helper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import tech.kicky.hms.scan.databinding.RvItemTextBinding

/**
 * ViewBinding Adapter
 * author: yidong
 * 2021-07-10
 */
class TextAdapter(private val click: (Int) -> Unit) :
    RecyclerView.Adapter<BindingViewHolder>() {

    private val titles = mutableListOf<String>()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
        val binding = RvItemTextBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BindingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        val binding = holder.binding as RvItemTextBinding
        binding.title.text = titles[position]
        holder.itemView.setOnClickListener {
            click.invoke(position)
        }
    }

    override fun getItemCount(): Int = titles.size

    fun addData(list: List<String>) {
        val start = this.titles.size
        this.titles.addAll(list)
        this.notifyItemRangeInserted(start, list.size)
    }

    fun setData(list: List<String>) {
        this.titles.clear()
        this.titles.addAll(list)
        this.notifyItemRangeChanged(0, list.size)
    }


}