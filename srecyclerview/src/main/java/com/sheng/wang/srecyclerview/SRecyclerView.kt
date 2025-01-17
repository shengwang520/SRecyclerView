package com.sheng.wang.srecyclerview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemAnimator
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.sheng.wang.srecyclerview.adapter.SRecyclerArrayAdapter

/**
 * 简单的RecyclerView
 */
class SRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var mRefreshLayout: SwipeRefreshLayout? = null
    private var mRecycler: RecyclerView? = null
    private var mProgressView: ViewGroup? = null
    private var mEmptyView: ViewGroup? = null
    private var mErrorView: ViewGroup? = null
    private var mProgressId = 0
    private var mEmptyId = 0
    private var mErrorId = 0
    private var mExternalOnScrollListenerList: MutableList<RecyclerView.OnScrollListener> =
        ArrayList()

    /**
     * 初始化属性
     */
    private fun initAttrs(attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.SRecyclerView)
        try {
            mEmptyId = a.getResourceId(R.styleable.SRecyclerView_layout_empty, 0)
            mProgressId = a.getResourceId(R.styleable.SRecyclerView_layout_progress, 0)
            mErrorId = a.getResourceId(R.styleable.SRecyclerView_layout_error, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            a.recycle()
        }
    }

    /**
     * 初始化数据
     */
    private fun initView() {
        val view =
            LayoutInflater.from(context).inflate(R.layout.s_layout_progress_recyclerview, this)
        mRefreshLayout = view.findViewById(R.id.s_refreshLayout)
        mRefreshLayout?.isEnabled = false
        mRecycler = view.findViewById(R.id.s_recyclerView)
        mProgressView = view.findViewById(R.id.s_progress)
        if (mProgressId != 0) LayoutInflater.from(context).inflate(mProgressId, mProgressView)
        mEmptyView = view.findViewById(R.id.s_empty)
        if (mEmptyId != 0) LayoutInflater.from(context).inflate(mEmptyId, mEmptyView)
        mErrorView = view.findViewById(R.id.s_error)
        if (mErrorId != 0) LayoutInflater.from(context).inflate(mErrorId, mErrorView)
        initRecyclerView()
        showProgress()
    }

    /**
     * 初始化RecyclerView
     */
    private fun initRecyclerView() {
        mRecycler?.setHasFixedSize(true)
        val onScrollListener: RecyclerView.OnScrollListener =
            object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    for (listener in mExternalOnScrollListenerList) {
                        listener.onScrollStateChanged(recyclerView, newState)
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    for (listener in mExternalOnScrollListenerList) {
                        listener.onScrolled(recyclerView, dx, dy)
                    }
                }
            }
        mRecycler?.addOnScrollListener(onScrollListener)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        return mRefreshLayout!!.dispatchTouchEvent(ev)
    }

    /**
     * 获取列表
     */
    fun getRecyclerView(): RecyclerView? {
        return mRecycler
    }

    /**
     * 设置管理类
     */
    fun setLayoutManager(manager: RecyclerView.LayoutManager?) {
        mRecycler?.layoutManager = manager
    }

    /**
     * 设置适配器，关闭所有副view。展示recyclerView
     * 适配器有更新，自动关闭所有副view。根据条数判断是否展示EmptyView
     */
    fun setAdapter(adapter: RecyclerView.Adapter<*>?) {
        mRecycler?.adapter = adapter
        adapter?.registerAdapterDataObserver(SDataObserver(this))
        showRecycler()
    }

    /**
     * 设置适配器，关闭所有副view。展示进度条View
     * 适配器有更新，自动关闭所有副view。根据条数判断是否展示EmptyView
     */
    fun setAdapterWithProgress(adapter: RecyclerView.Adapter<*>?) {
        mRecycler?.adapter = adapter
        adapter?.registerAdapterDataObserver(SDataObserver(this))
        //只有Adapter为空时才显示ProgressView
        if (adapter is SRecyclerArrayAdapter<*>) {
            if (adapter.count == 0) {
                showProgress()
            } else {
                showRecycler()
            }
        } else {
            if (adapter?.itemCount == 0) {
                showProgress()
            } else {
                showRecycler()
            }
        }
    }

    /**
     * @return the recycler adapter
     */
    val adapter: RecyclerView.Adapter<*>?
        get() = mRecycler!!.adapter

    /**
     * 隐藏界面
     */
    private fun hideAll() {
        mEmptyView?.visibility = GONE
        mProgressView?.visibility = GONE
        mErrorView?.visibility = GONE
        mRecycler?.visibility = INVISIBLE
    }

    /**
     * 显示错误布局
     */
    fun showError() {
        //显示错误布局-清楚数据
        (mRecycler?.adapter as? SRecyclerArrayAdapter<*>)?.clear()
        if (mErrorView!!.childCount > 0) {
            hideAll()
            mErrorView!!.visibility = VISIBLE
        } else {
            showRecycler()
        }
    }

    /**
     * 显示空布局
     */
    fun showEmpty() {
        if (mEmptyView!!.childCount > 0) {
            hideAll()
            mEmptyView!!.visibility = VISIBLE
        } else {
            showRecycler()
        }
    }

    /**
     * 显示进度
     */
    fun showProgress() {
        if (mProgressView!!.childCount > 0) {
            hideAll()
            mProgressView!!.visibility = VISIBLE
        } else {
            showRecycler()
        }
    }

    /**
     * 显示布局
     */
    fun showRecycler() {
        hideAll()
        mRecycler!!.visibility = VISIBLE
    }

    /**
     * 滚动位置
     */
    fun scrollToPosition(position: Int) {
        mRecycler?.scrollToPosition(position)
    }

    /**
     * Set the listener when refresh is triggered and enable the SwipeRefreshLayout
     */
    fun setRefreshListener(listener: SwipeRefreshLayout.OnRefreshListener?) {
        mRefreshLayout?.isEnabled = true
//        mRefreshLayout?.setOnRefreshListener(listener)
        mRefreshLayout?.setOnRefreshListener {
            val adapter = mRecycler?.adapter
            if (adapter is SRecyclerArrayAdapter<*>) {
                adapter.resetMore()
            }
            listener?.onRefresh()
        }
    }

    /**
     * 刷新
     */
    fun setRefreshing(isRefreshing: Boolean) {
        mRefreshLayout?.post { mRefreshLayout?.isRefreshing = isRefreshing }
    }

    /**
     * Add scroll listener to the recycler
     */
    fun addOnScrollListener(listener: RecyclerView.OnScrollListener) {
        mExternalOnScrollListenerList.add(listener)
    }

    /**
     * Remove scroll listener from the recycler
     */
    fun removeOnScrollListener(listener: RecyclerView.OnScrollListener) {
        mExternalOnScrollListenerList.remove(listener)
    }

    /**
     * Remove all scroll listeners from the recycler
     */
    fun removeAllOnScrollListeners() {
        mExternalOnScrollListenerList.clear()
    }

    /**
     * 设置动画
     */
    fun setItemAnimator(animator: ItemAnimator?) {
        mRecycler?.itemAnimator = animator
    }

    /**
     * 添加分割线
     */
    fun addItemDecoration(itemDecoration: ItemDecoration) {
        mRecycler?.addItemDecoration(itemDecoration)
    }

    /**
     * 添加分割线
     */
    fun addItemDecoration(itemDecoration: ItemDecoration, index: Int) {
        mRecycler?.addItemDecoration(itemDecoration, index)
    }

    /**
     * 添加分割线
     */
    fun removeItemDecoration(itemDecoration: ItemDecoration) {
        mRecycler?.removeItemDecoration(itemDecoration)
    }

    init {
        initAttrs(attrs)
        initView()
    }
}