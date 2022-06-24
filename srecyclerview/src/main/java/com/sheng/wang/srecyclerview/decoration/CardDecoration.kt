package com.sheng.wang.srecyclerview.decoration

import android.graphics.Rect
import android.util.LayoutDirection
import android.view.View
import androidx.core.text.TextUtilsCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.*

/**
 * @param mPageMargin 卡片间距
 * @param mLeftPageVisibleWidth 卡片第1条数据左边距离
 */
class CardDecoration @JvmOverloads constructor(
    private var mPageMargin: Int,
    private var mLeftPageVisibleWidth: Int,
    private var viewWidth: Int,
    private var topMargin: Int = 0,
    private var bottomMargin: Int = 0
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view); //获得当前item的position
        val itemCount = parent.adapter?.itemCount ?: 0 //获得item的数量

        var leftMargin = 0
        var rightMargin = 0
        if (position == 0) {
            if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == LayoutDirection.RTL) {//界面反转
                leftMargin = mPageMargin
                rightMargin = mLeftPageVisibleWidth
            } else {
                leftMargin = mLeftPageVisibleWidth
                rightMargin = mPageMargin
            }
        } else if (position == itemCount - 1) {
            if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == LayoutDirection.RTL) {//界面反转
                leftMargin = mLeftPageVisibleWidth
                rightMargin = mPageMargin
            } else {
                leftMargin = mPageMargin
                rightMargin = mLeftPageVisibleWidth
            }
        } else {
            leftMargin = mPageMargin
            rightMargin = mPageMargin
        }


        val lp = view.layoutParams as RecyclerView.LayoutParams
        lp.width = viewWidth
        lp.setMargins(leftMargin, topMargin, rightMargin, bottomMargin)
        view.layoutParams = lp
        super.getItemOffsets(outRect, view, parent, state)
    }
}