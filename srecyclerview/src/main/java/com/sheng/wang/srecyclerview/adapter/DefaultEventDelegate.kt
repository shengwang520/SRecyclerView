package com.sheng.wang.srecyclerview.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout

/**
 * 事件代理实现
 */
class DefaultEventDelegate(private val adapter: SRecyclerArrayAdapter<*>) : EventDelegate {
    private val footer: EventFooter
    private var onLoadMoreListener: SCallback.OnLoadMoreListener? = null
    private var onNoMoreListener: SCallback.OnNoMoreListener? = null
    private var isLoadingMore = false
    private var hasMore = false
    private var hasNoMore = false
    private var hasEmpty = false
    private var status = STATUS_INITIAL

    override fun addData(length: Int) {
        if (hasMore) {
            if (length == 0) {
                //当添加0个时，认为已结束加载到底
                if (status == STATUS_INITIAL || status == STATUS_MORE) {
                    footer.showNoMore()
                    status = STATUS_NO_MORE
                }
            }
        } else {
            if (hasNoMore) {
                footer.showNoMore()
                status = STATUS_NO_MORE
            }
        }

        isLoadingMore = false
    }

    override fun clear() {
        status = STATUS_INITIAL
        footer.hide()
        isLoadingMore = false
    }

    override fun startLoadMore() {
        if (hasMore && !isLoadingMore && status != STATUS_NO_MORE) {
            isLoadingMore = false
            footer.showMore()
            status = STATUS_MORE
            onMoreViewShowed()
        }
    }

    override fun stopLoadMore() {
        if (status != STATUS_NO_MORE) {
            footer.showNoMore()
            status = STATUS_NO_MORE
            isLoadingMore = false
        }
    }

    override fun pauseLoadMore() {
        status = STATUS_ERROR
        isLoadingMore = false
    }

    override fun resumeLoadMore() {
        isLoadingMore = false
        footer.showMore()
        status = STATUS_MORE
        onMoreViewShowed()
    }

    override fun showNoMore() {
        if (hasNoMore) {
            footer.showNoMore()
        }
        isLoadingMore = false
    }

    override fun showEmpty() {
        if (hasEmpty) {
            footer.showEmpty()
        }
    }

    override fun setMore(res: Int, listener: SCallback.OnLoadMoreListener?) {
        footer.setMoreViewRes(res)
        this.onLoadMoreListener = listener
        hasMore = true
        // 为了处理setMore之前就添加了数据的情况
        if (adapter.count > 0) {
            addData(adapter.count)
        }
    }

    override fun setNoMore(res: Int, listener: SCallback.OnNoMoreListener?) {
        footer.setNoMoreViewRes(res)
        this.onNoMoreListener = listener
        hasNoMore = true
    }

    override fun setEmpty(res: Int) {
        footer.setEmptyViewRes(res)
        hasEmpty = true
    }

    /**
     * 显示加载更多布局
     */
    fun onMoreViewShowed() {
        if (!isLoadingMore) {
            isLoadingMore = true
            onLoadMoreListener?.onMoreShow()
        }
    }

    /**
     * 点击加载更多布局
     */
    fun onMoreViewClicked() {
        onLoadMoreListener?.onMoreClick()
    }

    /**
     * 显示没有数据布局
     */
    fun onNoMoreViewShowed() {
        onNoMoreListener?.onNoMoreShow()
    }

    /**
     * 点击没有更多数据
     */
    fun onNoMoreViewClicked() {
        onNoMoreListener?.onNoMoreClick()
    }

    /**
     * 底部布局
     */
    inner class EventFooter : SCallback.ItemView {
        private var moreViewRes = 0
        private var noMoreViewRes = 0
        private var emptyViewRes = 0
        private var flag = Hide
        private var skipNoMore = false
        override fun onCreateView(parent: ViewGroup): View {
            return refreshStatus(parent)
        }

        override fun onBindView(headerView: View) {
            headerView.post {
                when (flag) {
                    ShowMore -> onMoreViewShowed()
                    ShowNoMore -> {
                        if (!skipNoMore) onNoMoreViewShowed()
                        skipNoMore = false
                    }
                }
            }
        }

        /**
         * 刷新状态,显示布局
         */
        private fun refreshStatus(parent: ViewGroup?): View {
            var view: View? = null
            when (flag) {
                ShowMore -> {
                    if (moreViewRes != 0) {
                        view = LayoutInflater.from(parent!!.context)
                            .inflate(moreViewRes, parent, false)
                    }
                    view?.setOnClickListener { onMoreViewClicked() }
                }

                ShowNoMore -> {
                    if (noMoreViewRes != 0) {
                        view = LayoutInflater.from(parent!!.context)
                            .inflate(noMoreViewRes, parent, false)
                    }
                    view?.setOnClickListener { onNoMoreViewClicked() }
                }

                ShowEmpty -> {
                    if (emptyViewRes != 0) {
                        view = LayoutInflater.from(parent!!.context)
                            .inflate(emptyViewRes, parent, false)
                    }
                }
            }
            if (view == null) view = FrameLayout(parent!!.context)
            return view
        }

        /**
         * 显示加载数据
         */
        fun showMore() {
            flag = ShowMore
            if (adapter.itemCount > 0) adapter.notifyItemChanged(adapter.itemCount - 1)
        }

        /**
         * 显示没有数据
         */
        fun showNoMore() {
            skipNoMore = true
            flag = ShowNoMore
            if (adapter.itemCount > 0) adapter.notifyItemChanged(adapter.itemCount - 1)
        }

        /**
         * 显示空布局
         */
        fun showEmpty() {
            flag = ShowEmpty
            if (adapter.itemCount > 0) adapter.notifyItemChanged(adapter.itemCount - 1)
        }

        /**
         * 隐藏布局
         */
        fun hide() {
            flag = Hide
            if (adapter.itemCount > 0) adapter.notifyItemChanged(adapter.itemCount - 1)
        }

        /**
         * 设置加载更多数据布局
         */
        fun setMoreViewRes(moreViewRes: Int) {
            this.moreViewRes = moreViewRes
        }

        /**
         * 设置没有更多数据布局id
         */
        fun setNoMoreViewRes(noMoreViewRes: Int) {
            this.noMoreViewRes = noMoreViewRes
        }

        /**
         * 设置空数据布局id
         */
        fun setEmptyViewRes(emptyViewRes: Int) {
            this.emptyViewRes = emptyViewRes
        }

        override fun hashCode(): Int {
            return flag + 13589
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as EventFooter

            if (moreViewRes != other.moreViewRes) return false
            if (noMoreViewRes != other.noMoreViewRes) return false
            if (emptyViewRes != other.emptyViewRes) return false
            if (flag != other.flag) return false
            if (skipNoMore != other.skipNoMore) return false

            return true
        }

    }

    companion object {
        private const val STATUS_INITIAL = 0
        private const val STATUS_MORE = 1
        private const val STATUS_NO_MORE = 2
        private const val STATUS_ERROR = 3

        private const val Hide = 0
        private const val ShowMore = 1
        private const val ShowNoMore = 2
        private const val ShowEmpty = 3
    }

    init {
        footer = EventFooter()
        adapter.addFooter(footer)
    }
}