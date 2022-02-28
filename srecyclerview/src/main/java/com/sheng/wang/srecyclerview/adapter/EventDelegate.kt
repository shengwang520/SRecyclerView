package com.sheng.wang.srecyclerview.adapter

/**
 * 事件代理
 */
interface EventDelegate {
    /**
     * 新增数据
     *
     * @param length 数据长度
     */
    fun addData(length: Int)

    /**
     * 清除数据
     */
    fun clear()

    /**
     * 开始加载更多
     */
    fun startLoadMore()

    /**
     * 停止加载更多
     */
    fun stopLoadMore()

    /**
     * 暂停加载更多
     */
    fun pauseLoadMore()

    /**
     * 重新加载更多
     */
    fun resumeLoadMore()

    /**
     * 显示没有数据布局
     */
    fun showNoMore()

    /**
     * 设置加载 更多
     *
     * @param res      加载更多布局
     * @param listener 回调
     */
    fun setMore(res: Int, listener: SCallback.OnLoadMoreListener?)

    /**
     * 设置没有数据
     *
     * @param res      没有数据布局
     * @param listener 回调
     */
    fun setNoMore(res: Int, listener: SCallback.OnNoMoreListener?)
}