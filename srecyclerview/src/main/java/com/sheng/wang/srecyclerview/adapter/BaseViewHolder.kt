package com.sheng.wang.srecyclerview.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView

/**
 * 通用布局
 */
abstract class BaseViewHolder<M>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var data: M? = null //数据
    var dataPosition = 0 //数据位置

    constructor(parent: ViewGroup, @LayoutRes res: Int) : this(
        LayoutInflater.from(parent.context).inflate(res, parent, false)
    )

    /**
     * 布局获取
     */
    protected fun <T : View?> findViewById(@IdRes id: Int): T {
        return itemView.findViewById(id)
    }

    /**
     * 绑定数据
     */
    open fun bindData(data: M, position: Int) {
        this.data = data
        this.dataPosition = position
    }

    /**
     * 上下文
     */
    val context: Context
        get() = itemView.context
}