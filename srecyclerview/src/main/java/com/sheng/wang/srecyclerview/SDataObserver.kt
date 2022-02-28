package com.sheng.wang.srecyclerview

import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.orhanobut.logger.Logger
import com.sheng.wang.srecyclerview.adapter.SRecyclerArrayAdapter

/**
 * 数据观察者
 */
class SDataObserver(private val recyclerView: SRecyclerView) : AdapterDataObserver() {
    private var adapter: SRecyclerArrayAdapter<*>? = null

    private fun isHeaderFooter(position: Int): Boolean {
        Logger.d("SDataObserver isHeaderFooter:$position |" + adapter!!.headerCount + "|" + adapter!!.count)
        return adapter != null && (position < adapter!!.headerCount || position >= adapter!!.headerCount + adapter!!.count)
    }

    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
        super.onItemRangeChanged(positionStart, itemCount)
        Logger.d("SDataObserver onItemRangeChanged")
        update()
    }

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        super.onItemRangeInserted(positionStart, itemCount)
        Logger.d("SDataObserver onItemRangeInserted")
        if (adapter!!.count > 0 || !isHeaderFooter(positionStart)) {
            update()
        }
    }

    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
        super.onItemRangeRemoved(positionStart, itemCount)
        Logger.d("SDataObserver onItemRangeRemoved")
        update()
    }

    override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        super.onItemRangeMoved(fromPosition, toPosition, itemCount)
        Logger.d("SDataObserver onItemRangeMoved")
        update() //header&footer不会有移动操作
    }

    override fun onChanged() {
        super.onChanged()
        Logger.d("SDataObserver onChanged")
        update() //header&footer不会引起changed
    }

    //自动更改Container的样式
    private fun update() {
        val count: Int = if (recyclerView.adapter is SRecyclerArrayAdapter<*>) {
            val adapter = recyclerView.adapter as SRecyclerArrayAdapter<*>
            // 有Header Footer就不显示Empty,但排除EventFooter。
            adapter.count + adapter.headerCount + adapter.footerCount - if (adapter.hasEventFooter()) 1 else 0
        } else {
            recyclerView.adapter!!.itemCount
        }
        Logger.d("SDataObserver update:$count")
        if (count == 0) {
            recyclerView.showEmpty()
        } else {
            recyclerView.showRecycler()
        }
    }

    init {
        if (recyclerView.adapter is SRecyclerArrayAdapter<*>) {
            adapter = recyclerView.adapter as SRecyclerArrayAdapter<*>
        }
    }
}