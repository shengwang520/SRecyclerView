/*
 * Copyright 2014 Eduardo Barrenechea
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sheng.wang.srecyclerview.decoration

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.sheng.wang.srecyclerview.adapter.SRecyclerArrayAdapter

/**
 * A sticky header decoration for android's RecyclerView.
 */
class StickyHeaderDecoration @JvmOverloads constructor(
    private val mAdapter: IStickyHeaderAdapter<*>, renderInline: Boolean = false
) : ItemDecoration() {
    /**
     * The adapter to assist the [StickyHeaderDecoration] in creating and binding the header views.
     *
     * @param <T> the header view holder
    </T> */
    interface IStickyHeaderAdapter<T : RecyclerView.ViewHolder?> {
        /**
         * Returns the header id for the item at the given position.
         * The item in one group should return the same HeaderId.
         *
         * @param position the item position
         * @return the header id
         */
        fun getHeaderId(position: Int): Long

        /**
         * Creates a new header ViewHolder.
         *
         * @param parent the header's view parent
         * @return a view holder for the created view
         */
        fun onCreateHeaderViewHolder(parent: ViewGroup?): T

        /**
         * Updates the header view to reflect the header data for the given position
         *
         * @param viewholder the header view holder
         * @param position   the header's item position
         */
        fun onBindHeaderViewHolder(viewholder: RecyclerView.ViewHolder, position: Int)
    }

    private val mHeaderCache: MutableMap<Long, RecyclerView.ViewHolder>
    private val mRenderInline: Boolean
    private var mIncludeHeader = false
    fun setIncludeHeader(mIncludeHeader: Boolean) {
        this.mIncludeHeader = mIncludeHeader
    }

    /**
     * {@inheritDoc}
     */
    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
    ) {
        var position = parent.getChildAdapterPosition(view)
        var headerHeight = 0
        if (!mIncludeHeader) {
            if (parent.adapter is SRecyclerArrayAdapter<*>) {
                val headerCount = (parent.adapter as SRecyclerArrayAdapter<*>?)!!.headerCount
                val footerCount = (parent.adapter as SRecyclerArrayAdapter<*>?)!!.footerCount
                val dataCount = (parent.adapter as SRecyclerArrayAdapter<*>?)!!.count
                if (position < headerCount) {
                    return
                }
                if (position >= headerCount + dataCount) {
                    return
                }
                position -= headerCount
            }
        }
        if (position != RecyclerView.NO_POSITION && hasHeader(position) && showHeaderAboveItem(
                position
            )) {
            val header = getHeader(parent, position)!!.itemView
            headerHeight = getHeaderHeightForLayout(header)
        }
        outRect[0, headerHeight, 0] = 0
    }

    private fun showHeaderAboveItem(itemAdapterPosition: Int): Boolean {
        return if (itemAdapterPosition == 0) {
            true
        } else mAdapter.getHeaderId(itemAdapterPosition - 1) != mAdapter.getHeaderId(
            itemAdapterPosition
        )
    }

    /**
     * Clears the header view cache. Headers will be recreated and
     * rebound on list scroll after this method has been called.
     */
    fun clearHeaderCache() {
        mHeaderCache.clear()
    }

    fun findHeaderViewUnder(x: Float, y: Float): View? {
        for (holder in mHeaderCache.values) {
            val child = holder.itemView
            val translationX = ViewCompat.getTranslationX(child)
            val translationY = ViewCompat.getTranslationY(child)
            if (x >= child.left + translationX && x <= child.right + translationX && y >= child.top + translationY && y <= child.bottom + translationY) {
                return child
            }
        }
        return null
    }

    private fun hasHeader(position: Int): Boolean {
        return mAdapter.getHeaderId(position) != NO_HEADER_ID
    }

    private fun getHeader(parent: RecyclerView, position: Int): RecyclerView.ViewHolder? {
        val key = mAdapter.getHeaderId(position)
        return if (mHeaderCache.containsKey(key)) {
            mHeaderCache[key]
        } else {
            val holder = mAdapter.onCreateHeaderViewHolder(parent)!!
            val header = holder.itemView
            mAdapter.onBindHeaderViewHolder(holder, position)
            val widthSpec = View.MeasureSpec.makeMeasureSpec(
                parent.measuredWidth, View.MeasureSpec.EXACTLY
            )
            val heightSpec = View.MeasureSpec.makeMeasureSpec(
                parent.measuredHeight, View.MeasureSpec.UNSPECIFIED
            )
            val childWidth = ViewGroup.getChildMeasureSpec(
                widthSpec, parent.paddingLeft + parent.paddingRight, header.layoutParams.width
            )
            val childHeight = ViewGroup.getChildMeasureSpec(
                heightSpec, parent.paddingTop + parent.paddingBottom, header.layoutParams.height
            )
            header.measure(childWidth, childHeight)
            header.layout(0, 0, header.measuredWidth, header.measuredHeight)
            mHeaderCache[key] = holder
            holder
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.adapter == null) {
            return
        }
        val count = parent.childCount
        var previousHeaderId: Long = -1
        for (layoutPos in 0 until count) {
            val child = parent.getChildAt(layoutPos)
            var adapterPos = parent.getChildAdapterPosition(child)
            if (!mIncludeHeader) {
                if (parent.adapter is SRecyclerArrayAdapter<*>) {
                    val headerCount = (parent.adapter as SRecyclerArrayAdapter<*>?)!!.headerCount
                    val footerCount = (parent.adapter as SRecyclerArrayAdapter<*>?)!!.footerCount
                    val dataCount = (parent.adapter as SRecyclerArrayAdapter<*>?)!!.count
                    if (adapterPos < headerCount) {
                        continue
                    }
                    if (adapterPos >= headerCount + dataCount) {
                        continue
                    }
                    adapterPos -= headerCount
                }
            }
            if (adapterPos != RecyclerView.NO_POSITION && hasHeader(adapterPos)) {
                val headerId = mAdapter.getHeaderId(adapterPos)
                if (headerId != previousHeaderId) {
                    previousHeaderId = headerId
                    val header = getHeader(parent, adapterPos)!!.itemView
                    canvas.save()
                    val left = child.left
                    val top = getHeaderTop(parent, child, header, adapterPos, layoutPos)
                    canvas.translate(left.toFloat(), top.toFloat())
                    header.translationX = left.toFloat()
                    header.translationY = top.toFloat()
                    header.draw(canvas)
                    canvas.restore()
                }
            }
        }
    }

    private fun getHeaderTop(
        parent: RecyclerView, child: View, header: View, adapterPos: Int, layoutPos: Int
    ): Int {
        val headerHeight = getHeaderHeightForLayout(header)
        var top = child.y.toInt() - headerHeight
        if (layoutPos == 0) {
            val count = parent.childCount
            val currentId = mAdapter.getHeaderId(adapterPos)
            // find next view with header and compute the offscreen push if needed
            for (i in 1 until count) {
                val adapterPosHere = parent.getChildAdapterPosition(parent.getChildAt(i))
                if (adapterPosHere != RecyclerView.NO_POSITION) {
                    val nextId = mAdapter.getHeaderId(adapterPosHere)
                    if (nextId != currentId) {
                        val next = parent.getChildAt(i)
                        val offset = next.y.toInt() - (headerHeight + getHeader(
                            parent, adapterPosHere
                        )!!.itemView.height)
                        return if (offset < 0) {
                            offset
                        } else {
                            break
                        }
                    }
                }
            }
            top = Math.max(0, top)
        }
        return top
    }

    private fun getHeaderHeightForLayout(header: View): Int {
        return if (mRenderInline) 0 else header.height
    }

    companion object {
        const val NO_HEADER_ID = -1L
    }
    /**
     * @param mAdapter the sticky header adapter to use
     */
    /**
     * @param adapter the sticky header adapter to use
     */
    init {
        mHeaderCache = HashMap()
        mRenderInline = renderInline
    }
}