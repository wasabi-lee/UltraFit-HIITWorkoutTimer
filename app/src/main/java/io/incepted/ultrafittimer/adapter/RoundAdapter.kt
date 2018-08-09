package io.incepted.ultrafittimer.adapter

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.databinding.CustomizeListItemBinding
import io.incepted.ultrafittimer.db.tempmodel.Round
import io.incepted.ultrafittimer.viewmodel.CustomizeViewModel
import timber.log.Timber

class RoundAdapter(var data: List<Round>, val viewModel: CustomizeViewModel) : RecyclerView.Adapter<RoundAdapter.ViewHolder>() {

    class ViewHolder(public val itemBinding: CustomizeListItemBinding): RecyclerView.ViewHolder(itemBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoundAdapter.ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val binding: ViewDataBinding = DataBindingUtil.inflate(layoutInflater, R.layout.customize_list_item, parent,false)
        return ViewHolder(binding as CustomizeListItemBinding)
    }

    override fun onBindViewHolder(holder: RoundAdapter.ViewHolder, position: Int) {
        val currentItem = data[position]
        val itemBinding: CustomizeListItemBinding = holder.itemBinding
        itemBinding.data = currentItem
        itemBinding.executePendingBindings()

        Timber.d("Item #$position populated!")
    }

    override fun getItemCount(): Int {
        return data.size
    }

    private fun setList(newData: List<Round>) {
        this.data = newData
        notifyDataSetChanged()
    }

    fun replaceData(newData: List<Round>) {
        setList(newData)
    }



}