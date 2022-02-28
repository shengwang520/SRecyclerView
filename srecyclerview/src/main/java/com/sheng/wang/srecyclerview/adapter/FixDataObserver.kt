package com.sheng.wang.srecyclerview.adapter

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver

/**
 * 极端情况下界面适配
 */
class FixDataObserver(private val recyclerView: RecyclerView) : AdapterDataObserver() {
    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        if (recyclerView.adapter is SRecyclerArrayAdapter<*>) {
            val adapter = recyclerView.adapter as SRecyclerArrayAdapter<*>?
            if (adapter!!.footerCount > 0 && adapter.count == itemCount) {
                recyclerView.scrollToPosition(0)
            }
        }
    }
}