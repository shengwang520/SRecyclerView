package com.sheng.wang.srecyclerview.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.orhanobut.logger.Logger


/**
 * 数据通用适配器
 * @param pageSize 列表分页时的每页参数，当数据小于pageSize时，replace()更新数据会显示数据加载完毕布局，默认20每页
 */
abstract class SRecyclerArrayAdapter<T> @JvmOverloads constructor(
    var context: Context? = null, var pageSize: Int = 20
) : RecyclerView.Adapter<BaseViewHolder<T>>() {
    private val mLock = Any()
    private val mData: MutableList<T> = ArrayList()
    private var headers = ArrayList<SCallback.ItemView>()
    private var footers = ArrayList<SCallback.ItemView>()
    private var mEventDelegate: EventDelegate? = null

    /**
     * Indicates whether or not [.notifyDataSetChanged] must be called whenever
     * [.mData] is modified.
     */
    private val mNotifyOnChange = true

    /**
     * 获取事件代理类
     */
    private val eventDelegate: EventDelegate
        get() {
            if (mEventDelegate == null) mEventDelegate = DefaultEventDelegate(this)
            return mEventDelegate!!
        }

    open fun hasEventFooter(): Boolean {
        return mEventDelegate != null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T> {
        val view = createSpViewByType(parent, viewType)
        return if (view != null) {
            StateViewHolder(view)
        } else {
            onCreateItemViewHolder(parent, viewType)
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<T>, position: Int) {
        if (headerCount != 0 && position < headerCount) {
            headers[position].onBindView(holder.itemView)
            return
        }
        val i = position - headerCount - mData.size
        if (footerCount != 0 && i >= 0) {
            footers[i].onBindView(holder.itemView)
            return
        }
        onBindItemViewHolder(holder, position - headerCount)
    }

    override fun getItemCount(): Int {
        return mData.size + headerCount + footerCount
    }

    /**
     * 应该使用这个获取item个数
     */
    val count: Int
        get() = mData.size

    /**
     * 获取位置数据
     */
    open fun getItem(position: Int): T {
        return mData[position]
    }

    /**
     * 获取所有数据
     */
    fun getData(): List<T> {
        return mData
    }

    /**
     * 获取位置
     */
    open fun getPosition(item: T): Int {
        return mData.indexOf(item)
    }

    /**
     * 创建布局
     */
    abstract fun onCreateItemViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<T>

    /**
     * 绑定数据
     */

    private fun onBindItemViewHolder(holder: BaseViewHolder<T>, position: Int) {
        val t = getItem(position)
        if (t is SData) {
            t.change(false)
        }
        holder.bindData(t, position)
    }

    /**
     * 创建额外布局
     */
    private fun createSpViewByType(parent: ViewGroup, viewType: Int): View? {
        for (headerView in headers) {
            if (headerView.hashCode() == viewType) {
                val view = headerView.onCreateView(parent)
                val layoutParams: StaggeredGridLayoutManager.LayoutParams =
                    if (view.layoutParams != null) StaggeredGridLayoutManager.LayoutParams(view.layoutParams) else StaggeredGridLayoutManager.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                layoutParams.isFullSpan = true
                view.layoutParams = layoutParams
                return view
            }
        }
        for (footerView in footers) {
            if (footerView.hashCode() == viewType) {
                val view = footerView.onCreateView(parent)
                val layoutParams: StaggeredGridLayoutManager.LayoutParams =
                    if (view.layoutParams != null) StaggeredGridLayoutManager.LayoutParams(view.layoutParams) else StaggeredGridLayoutManager.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                layoutParams.isFullSpan = true
                view.layoutParams = layoutParams
                return view
            }
        }
        return null
    }

    override fun getItemViewType(position: Int): Int {
        if (headers.size != 0) {
            if (position < headers.size) return headers[position].hashCode()
        }
        if (footers.size != 0) {
            val i = position - headers.size - mData.size
            if (i >= 0) {
                return footers[i].hashCode()
            }
        }
        return getViewType(position - headers.size)
    }

    /**
     * item类型
     */
    open fun getViewType(position: Int): Int {
        return 0
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        //增加对SRecyclerArrayAdapter奇葩操作的修复措施
        registerAdapterDataObserver(FixDataObserver(recyclerView))
    }

    /**
     * 添加底部布局，用于显示加载更多
     */
    fun addFooter(view: SCallback.ItemView) {
        footers.add(view)
        notifyItemInserted(count + footers.size - 1)
    }

    /**
     * 添加头部布局
     */
    fun addHeader(view: SCallback.ItemView?) {
        if (view == null) throw NullPointerException("ItemView can't be null")
        headers.add(view)
        notifyItemInserted(headers.size - 1)
    }

    /**
     * 移除头部布局
     */
    fun removeAllHeader() {
        val count = headers.size
        headers.clear()
        notifyItemRangeRemoved(0, count)
    }

    /**
     * 移除底部布局
     */
    fun removeAllFooter() {
        val count = footers.size
        footers.clear()
        notifyItemRangeRemoved(itemCount - count, itemCount)
    }

    /**
     * 移除布局
     */
    fun removeFooter(view: SCallback.ItemView?) {
        if (view != null) {
            val index = footers.indexOf(view)
            footers.remove(view)
            notifyItemRemoved(itemCount + index)
        }
    }

    inner class GridSpanSizeLookup(private val mMaxCount: Int) : SpanSizeLookup() {

        override fun getSpanSize(position: Int): Int {
            if (headers.size != 0) {
                if (position < headers.size) return mMaxCount
            }
            if (footers.size != 0) {
                val i: Int = position - headers.size - mData.size
                if (i >= 0) {
                    return mMaxCount
                }
            }
            return 1
        }
    }

    /**
     * 适配网格Header和Footer 布局
     */
    open fun obtainGridSpanSizeLookUp(maxCount: Int): GridSpanSizeLookup {
        return GridSpanSizeLookup(maxCount)
    }

    /**
     * 开始加载更多
     */
    fun startMore() {
        mEventDelegate?.startLoadMore()
    }

    /**
     * 停止加载更多
     */
    fun stopMore() {
        mEventDelegate?.stopLoadMore()
    }

    /**
     * 暂停加载更多
     */
    fun pauseMore() {
        mEventDelegate?.pauseLoadMore()
    }

    /**
     * 重新加载更多
     */
    fun resumeMore() {
        mEventDelegate?.resumeLoadMore()
    }

    /**
     * 显示空布局
     */
    fun showEmpty() {
        mEventDelegate?.showEmpty()
    }

    /**
     * 设置更多布局
     */
    fun setMore(res: Int, listener: SCallback.OnLoadMoreListener?) {
        eventDelegate.setMore(res, listener)
    }

    /**
     * 设置没有数据
     */
    fun setNoMore(res: Int) {
        eventDelegate.setNoMore(res, null)
    }

    /**
     * 设置没有数据布局
     */
    fun setNoMore(res: Int, listener: SCallback.OnNoMoreListener?) {
        eventDelegate.setNoMore(res, listener)
    }

    /**
     * 设置空数据布局，设置后在没有数据时会自动显示
     */
    fun setEmpty(res: Int) {
        eventDelegate.setEmpty(res)
    }


    /**
     * 添加单个数据/刷新界面
     */
    fun add(data: T?) {
        mEventDelegate?.addData(if (data == null) 0 else 1)
        if (data != null) {
            synchronized(mLock) { mData.add(data) }
        }
        if (mNotifyOnChange) notifyItemInserted(headers.size + count)
    }

    /**
     * 添加所有数据
     */
    fun addAll(collection: Collection<T>?) {
        mEventDelegate?.addData(collection?.size ?: 0)
        if (collection != null && collection.isNotEmpty()) {
            synchronized(mLock) { mData.addAll(collection) }
        }
        val dataCount = collection?.size ?: 0
        if (mNotifyOnChange) notifyItemRangeInserted(headers.size + count - dataCount, dataCount)
    }

    /**
     * 替换数据更新
     * @param collection T :SData 数据实体需要继承SData
     * @param detectMoves 如果您的旧列表和新列表按相同的约束排序并且项目从不移动（交换位置），您可以禁用移动检测，这需要 O(N^2) 时间，其中 N 是添加、移动、删除项目的数量。
     */
    @SuppressLint("NotifyDataSetChanged")
    @JvmOverloads
    fun replace(collection: Collection<T>?, detectMoves: Boolean = false) {
        Logger.d("SDataObserver replace")
        mEventDelegate?.addData(collection?.size ?: 0)

        var elements = collection
        if (elements == null) {
            elements = ArrayList()
        }

        val diffResult = DiffUtil.calculateDiff(DataDiffCallBack(mData, ArrayList(elements)), detectMoves)
        mData.clear()
        mData.addAll(elements)
        diffResult.dispatchUpdatesTo(this)

        if (mEventDelegate != null) {
            if (elements.isEmpty()) {//当没有数据时，自动显示空布局
                showEmpty()
                if (mNotifyOnChange) notifyDataSetChanged()
            } else {
                if (elements.size < pageSize) {//当数据小于每页时，会显示数据加载完毕布局
                    mEventDelegate?.showNoMore()
                }
            }
        } else {
            if (elements.isEmpty()) {//当数据为0时，更新适配器显示显示没有数据布局
                if (mNotifyOnChange) notifyItemChanged(0)
            }
        }
    }

    /**
     * 插入，不会触发任何事情
     *
     * @param data The object to insert into the array.
     * @param index  The index at which the object must be inserted.
     */
    fun insert(data: T, index: Int) {
        synchronized(mLock) { mData.add(index, data) }
        if (mNotifyOnChange) notifyItemInserted(headers.size + index)
    }

    /**
     * 更新数据
     */
    fun update(data: T, pos: Int) {
        synchronized(mLock) { mData.set(pos, data) }
        if (mNotifyOnChange) notifyItemChanged(pos)
    }

    /**
     * 删除，不会触发任何事情
     *
     * @param data The object to remove.
     */
    fun remove(data: T) {
        val position = mData.indexOf(data)
        remove(position)
    }

    /**
     * 删除，不会触发任何事情
     *
     * @param position The position of the object to remove.
     */
    fun remove(position: Int) {
        synchronized(mLock) { mData.removeAt(position) }
        if (mNotifyOnChange) notifyItemRemoved(headers.size + position)
    }

    /**
     * 触发清空
     */
    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        mEventDelegate?.clear()
        synchronized(mLock) { mData.clear() }
        if (mNotifyOnChange) notifyDataSetChanged()
    }

    /**
     * 重置加载更多状态
     */
    fun resetMore() {
        mEventDelegate?.clear()
    }

    val headerCount: Int
        get() = headers.size
    val footerCount: Int
        get() = footers.size

    private class StateViewHolder<T>(itemView: View) : BaseViewHolder<T>(
        itemView
    )

    /**
     * 数据变化逻辑
     */
    private inner class DataDiffCallBack(var oldList: List<T>?, var newList: List<T>?) : DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldList?.size ?: 0
        }

        override fun getNewListSize(): Int {
            return newList?.size ?: 0
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val old = oldList?.get(oldItemPosition)
            val new = newList?.get(newItemPosition)
            if (old is SData && new is SData) {
                return old.dataId() == new.dataId()
            }
            return old == new
        }

        /**
         * @return 返回false表示数据不同需要进行更新
         */
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val new = newList?.get(newItemPosition)
            if (new is SData) {
                return !new.dataChange()
            }
            return false
        }
    }
}