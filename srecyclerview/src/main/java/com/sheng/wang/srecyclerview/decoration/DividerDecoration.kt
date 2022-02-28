package com.sheng.wang.srecyclerview.decoration

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.sheng.wang.srecyclerview.adapter.SRecyclerArrayAdapter

/**
 * 分割线
 */
class DividerDecoration : ItemDecoration {
    private var mColorDrawable: ColorDrawable
    private var mHeight: Int
    private var mPaddingLeft = 0
    private var mPaddingRight = 0
    private var mDrawLastItem = true
    private var mDrawHeaderFooter = false

    constructor(color: Int, height: Int) {
        mColorDrawable = ColorDrawable(color)
        mHeight = height
    }

    constructor(color: Int, height: Int, paddingLeft: Int, paddingRight: Int) {
        mColorDrawable = ColorDrawable(color)
        mHeight = height
        mPaddingLeft = paddingLeft
        mPaddingRight = paddingRight
    }

    fun setDrawLastItem(mDrawLastItem: Boolean) {
        this.mDrawLastItem = mDrawLastItem
    }

    fun setDrawHeaderFooter(mDrawHeaderFooter: Boolean) {
        this.mDrawHeaderFooter = mDrawHeaderFooter
    }

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        var orientation = 0
        var headerCount = 0
        var footerCount = 0
        if (parent.adapter is SRecyclerArrayAdapter<*>) {
            headerCount = (parent.adapter as SRecyclerArrayAdapter<*>?)!!.headerCount
            footerCount = (parent.adapter as SRecyclerArrayAdapter<*>?)!!.footerCount
        }
        val layoutManager = parent.layoutManager
        if (layoutManager is StaggeredGridLayoutManager) {
            orientation = layoutManager.orientation
        } else if (layoutManager is GridLayoutManager) {
            orientation = layoutManager.orientation
        } else if (layoutManager is LinearLayoutManager) {
            orientation = layoutManager.orientation
        }
        if (position >= headerCount && position < parent.adapter!!.itemCount - footerCount || mDrawHeaderFooter) {
            if (orientation == OrientationHelper.VERTICAL) {
                outRect.bottom = mHeight
            } else {
                outRect.right = mHeight
            }
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.adapter == null) {
            return
        }
        var orientation = 0
        var headerCount = 0
        var footerCount = 0
        val dataCount: Int
        if (parent.adapter is SRecyclerArrayAdapter<*>) {
            headerCount = (parent.adapter as SRecyclerArrayAdapter<*>?)!!.headerCount
            footerCount = (parent.adapter as SRecyclerArrayAdapter<*>?)!!.footerCount
            dataCount = (parent.adapter as SRecyclerArrayAdapter<*>?)!!.count
        } else {
            dataCount = parent.adapter!!.itemCount
        }
        val dataStartPosition = headerCount
        val dataEndPosition = headerCount + dataCount
        val layoutManager = parent.layoutManager
        if (layoutManager is StaggeredGridLayoutManager) {
            orientation = layoutManager.orientation
        } else if (layoutManager is GridLayoutManager) {
            orientation = layoutManager.orientation
        } else if (layoutManager is LinearLayoutManager) {
            orientation = layoutManager.orientation
        }
        val start: Int
        val end: Int
        if (orientation == OrientationHelper.VERTICAL) {
            start = parent.paddingLeft + mPaddingLeft
            end = parent.width - parent.paddingRight - mPaddingRight
        } else {
            start = parent.paddingTop + mPaddingLeft
            end = parent.height - parent.paddingBottom - mPaddingRight
        }
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val position = parent.getChildAdapterPosition(child)
            if (position >= dataStartPosition && position < dataEndPosition - 1 //数据项除了最后一项
                || position == dataEndPosition - 1 && mDrawLastItem //数据项最后一项
                || position !in dataStartPosition until dataEndPosition && mDrawHeaderFooter //header&footer且可绘制
            ) {
                if (orientation == OrientationHelper.VERTICAL) {
                    val params = child.layoutParams as RecyclerView.LayoutParams
                    val top = child.bottom + params.bottomMargin
                    val bottom = top + mHeight
                    mColorDrawable.setBounds(start, top, end, bottom)
                    mColorDrawable.draw(c)
                } else {
                    val params = child.layoutParams as RecyclerView.LayoutParams
                    val left = child.right + params.rightMargin
                    val right = left + mHeight
                    mColorDrawable.setBounds(left, start, right, end)
                    mColorDrawable.draw(c)
                }
            }
        }
    }
}