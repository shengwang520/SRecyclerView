package com.sheng.wang.srecyclerview

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.orhanobut.logger.Logger
import com.sheng.wang.srecyclerview.adapter.SCallback
import com.sheng.wang.srecyclerview.adapter.SRecyclerArrayAdapter

/**
 * 自定义RecyclerView,实现上拉加载
 */
class CRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
    private var lastVisibleItem = 0//最后显示的位置

    /**
     * 加载更多回调，设置回调后需要在改回调中进行下一页数据请求
     */
    var onLoadMoreListener: SCallback.OnLoadMoreListener? = null

    /**
     * 初始化界面
     */
    private fun initView() {
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE) {
                    val adapter = adapter

                    if (onLoadMoreListener != null) {
                        if (lastVisibleItem + 1 == adapter?.itemCount) {
                            onLoadMoreListener?.onMoreShow()
                        }
                    } else if (adapter is SRecyclerArrayAdapter<*>) {
                        Logger.d("load more onScrollStateChanged:$lastVisibleItem | ${adapter.itemCount}")
                        if (lastVisibleItem + adapter.footerCount + 1 >= adapter.getItemCount()) {
                            adapter.startMore()
                        }
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val manager = layoutManager
                if (manager != null) {
                    if (manager is LinearLayoutManager) {
                        lastVisibleItem = manager.findLastVisibleItemPosition()
                    }
                }
                Logger.d("load more onScrolled:$lastVisibleItem")
            }
        })
    }

    init {
        initView()
    }
}