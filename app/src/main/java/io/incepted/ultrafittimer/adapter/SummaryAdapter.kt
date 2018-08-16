package io.incepted.ultrafittimer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.github.vipulasri.timelineview.TimelineView
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.databinding.SummaryListItemBinding
import io.incepted.ultrafittimer.db.tempmodel.Round
import io.incepted.ultrafittimer.viewmodel.SummaryViewModel
import timber.log.Timber

class SummaryAdapter(var data: MutableList<Round>, val viewmodel: SummaryViewModel)
    : RecyclerView.Adapter<SummaryAdapter.ViewHolder>() {

    class ViewHolder(val itemBinding: SummaryListItemBinding, viewType: Int)
        : RecyclerView.ViewHolder(itemBinding.root) {
        init {
            val timelineView: TimelineView = itemBinding.root.findViewById(R.id.summary_list_item_timeline_view)
            timelineView.initLine(viewType)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val binding: ViewDataBinding = DataBindingUtil.inflate(layoutInflater, R.layout.summary_list_item, parent, false)
        return ViewHolder(binding as SummaryListItemBinding, viewType)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Timber.d("Item $position bound!")
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        return TimelineView.getTimeLineViewType(position, itemCount)
    }

    private fun setList(newData: MutableList<Round>) {
        this.data = newData
        notifyDataSetChanged()
    }

    fun replaceData(newData: MutableList<Round>) {
        setList(newData)
    }

}