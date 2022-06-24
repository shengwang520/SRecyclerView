package com.sheng.wang.srecyclerview.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * @param mPageMargin 卡片间距
 * @param mLeftPageVisibleWidth 卡片第1条数据左边距离
 */
class CardDecoration(private var mPageMargin: Int, private var mLeftPageVisibleWidth: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view); //获得当前item的position
        val itemCount = parent.adapter?.itemCount ?: 0 //获得item的数量
        val leftMargin = if (position == 0) {
            mLeftPageVisibleWidth
        } else {
            mPageMargin
        }
        val rightMargin = if (position == itemCount - 1) {
            mLeftPageVisibleWidth
        } else {
            mPageMargin
        }
        val lp = view.layoutParams as RecyclerView.LayoutParams
        lp.setMargins(leftMargin, 0, rightMargin, 0)
        view.layoutParams = lp
        super.getItemOffsets(outRect, view, parent, state)
    }
}