package com.heaven.android.heavenlib.base.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView


abstract class BaseAdapter<DB : ViewDataBinding, M>(var listData: MutableList<M>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    @get:LayoutRes
    protected abstract val layoutRes: Int
    protected abstract fun createVH(binding: DB): RecyclerView.ViewHolder
    protected lateinit var recyclerView: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: DB =
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), layoutRes, parent, false)
        return createVH(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as BaseVH<Any?>).bind(listData[position])
    }

    override fun getItemCount(): Int = listData.size

    @SuppressLint("NotifyDataSetChanged")
    fun addList(newList: MutableList<M>) {
        listData.clear()
        listData.addAll(newList)
        notifyDataSetChanged()
    }

    fun insertList(fromIdx: Int, insertedList: MutableList<M>) {
        listData.addAll(fromIdx, insertedList)
        notifyItemRangeInserted(fromIdx, insertedList.size)
    }

    abstract inner class BaseVH<T>(val binding: DB) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener { onItemClickListener(listData[adapterPosition]) }
        }

        open fun onItemClickListener(data: M) = Unit
        open fun bind(data: M) {
//            binding.setVariable(BR.model, data)
            binding.executePendingBindings()
        }
    }
}