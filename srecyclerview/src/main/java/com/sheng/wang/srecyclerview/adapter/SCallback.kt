package com.sheng.wang.srecyclerview.adapter

import android.view.View
import android.view.ViewGroup

/**
 * 接口回调
 */
interface SCallback {
    /**
     * 头部/底部布局
     */
    interface ItemView {
        /**
         * 创建布局
         */
        fun onCreateView(parent: ViewGroup): View

        /**
         * 绑定布局
         */
        fun onBindView(headerView: View)
    }

    /**
     * 加载更多数据
     */
    interface OnLoadMoreListener {
        /**
         * 显示更多布局
         */
        fun onMoreShow()

        /**
         * 点击更多布局
         */
        fun onMoreClick()
    }

    /**
     * 没有更多数据
     */
    interface OnNoMoreListener {
        /**
         * 显示更多没有数据
         */
        fun onNoMoreShow()

        /**
         * 点击没有更多数据
         */
        fun onNoMoreClick()
    }
}