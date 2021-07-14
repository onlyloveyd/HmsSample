package tech.kicky.hms.helper

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import tech.kicky.hms.colorfilter.Filter
import tech.kicky.hms.scan.databinding.RvItemFilterBinding

/**
 * CheckBox & TextView 滤镜 Adapter
 * author: yidong
 * 2021-07-10
 */
class FilterAdapter(val callback: (Int) -> Unit) :
    RecyclerView.Adapter<BindingViewHolder>() {
    private val filters = mutableListOf<Filter>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
        val binding =
            RvItemFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BindingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        val binding = holder.binding as RvItemFilterBinding
        binding.name.text = filters[position].name
        binding.checkbox.isChecked = filters[position].checked
        binding.checkbox.setOnClickListener {
            filters.forEach {
                it.checked = false
            }
            if ((it as CheckBox).isChecked) {
                filters[position].checked = true
                callback(position)
            } else {
                callback(0)
            }
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = filters.size

    fun addData(list: List<Filter>) {
        val start = this.filters.size
        this.filters.addAll(list)
        this.notifyItemRangeInserted(start, list.size)
    }

    fun setData(list: List<Filter>) {
        this.filters.clear()
        this.filters.addAll(list)
        this.notifyItemRangeChanged(0, list.size)
    }
}