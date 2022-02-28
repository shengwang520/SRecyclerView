package com.sheng.wang.srecyclerview.adapter

/**
 * 列表数据统一处理
 */
interface SData {
    /**
     * 数据唯一id
     */
    fun dataId(): Int

    /**
     * 数据是否变化
     */
    fun dataChange(): Boolean

    /**
     * 修改数据变化
     */
    fun change(change: Boolean)
}