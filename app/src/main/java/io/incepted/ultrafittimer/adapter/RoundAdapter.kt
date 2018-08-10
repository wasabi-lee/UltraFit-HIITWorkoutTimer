package io.incepted.ultrafittimer.adapter

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.vipulasri.timelineview.TimelineView
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.databinding.CustomizeListItemBinding
import io.incepted.ultrafittimer.db.tempmodel.Round
import io.incepted.ultrafittimer.viewmodel.CustomizeViewModel
import kotlinx.android.synthetic.main.summary_list_item.view.*
import timber.log.Timber

class RoundAdapter(var data: MutableList<Round>, val viewModel: CustomizeViewModel) : RecyclerView.Adapter<RoundAdapter.ViewHolder>() {

    class ViewHolder(v: View, viewType: Int) : RecyclerView.ViewHolder(v) {

        lateinit var itemBinding: CustomizeListItemBinding

        constructor(itemBinding: CustomizeListItemBinding, viewType: Int) : this(itemBinding.root, viewType) {
            this.itemBinding = itemBinding
            val timelineView: TimelineView = itemBinding.root.findViewById(R.id.timelineView)
            timelineView.initLine(viewType)
            Timber.d("$viewType")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoundAdapter.ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val binding: ViewDataBinding = DataBindingUtil.inflate(layoutInflater, R.layout.customize_list_item, parent, false)

        if (viewType == 1001) {
            Timber.d("creating footer viewholder")
            return ViewHolder(layoutInflater.inflate(R.layout.list_item_footer, parent, false), viewType)
        }
        Timber.d("creating item viewholder")
        return ViewHolder(binding as CustomizeListItemBinding, viewType)
    }

    override fun onBindViewHolder(holder: RoundAdapter.ViewHolder, position: Int) {

        if (holder.itemViewType != 1001) {
            val itemBinding: CustomizeListItemBinding = holder.itemBinding
            itemBinding.data = data[position]
            itemBinding.viewmodel = viewModel
            itemBinding.root.tag = position + 1
            itemBinding.executePendingBindings()
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        if (position == data.size - 1) return 1001
        return TimelineView.getTimeLineViewType(position, itemCount - 1)
    }

    fun removeAt(position: Int?) {
        Timber.d("Removing item $position")
        data.removeAt(position ?: return)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, data.size-1)
    }

    private fun setList(newData: MutableList<Round>) {
        this.data = newData
        notifyDataSetChanged()
    }

    fun replaceData(newData: MutableList<Round>) {
        setList(newData)
    }


}