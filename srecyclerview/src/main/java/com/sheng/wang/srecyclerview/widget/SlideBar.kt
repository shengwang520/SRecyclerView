package com.sheng.wang.srecyclerview.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.IntDef
import com.sheng.wang.srecyclerview.R
import kotlin.math.max
import kotlin.math.min

/**
 * 索引
 */
class SlideBar<T> @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    /**
     * 画笔
     */
    private val paint = Paint()

    /**
     * 选中的字母索引
     */
    private var index = -1

    /**
     * 字母默认颜色
     */
    private var defaultColor = Color.BLACK

    /**
     * 字母选中颜色
     */
    private var chooseColor = Color.MAGENTA

    /**
     * 选中背景颜色
     */
    private var chooseBackgroundColor = Color.LTGRAY

    /**
     * 是否触摸
     */
    private var isTouch = false

    /**
     * 字母字体大小
     */
    private var textSize = 14

    /**
     * 字母改变监听
     */
    var onTouchLetterChangeListener: OnTouchLetterChangeListener<T>? = null

    /**
     * 选中样式
     */
    @Style.Value
    private var chooseStyle = Style.NONE

    /**
     * 数据
     */
    private val letters: MutableList<T> = ArrayList()

    /**
     * 样式
     */
    object Style {
        const val NONE = 0
        const val OVAL = 1
        const val CIRCLE = 2
        const val STRETCH = 3

        @IntDef(NONE, OVAL, CIRCLE, STRETCH)
        @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
        annotation class Value
    }

    /**
     * 初始化属性
     */
    private fun initAttrs(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SlideBar)
        textSize = typedArray.getDimensionPixelSize(R.styleable.SlideBar_s_text_size, 28)
        defaultColor = typedArray.getColor(
            R.styleable.SlideBar_s_text_color, Color.BLACK
        )
        chooseColor = typedArray.getColor(R.styleable.SlideBar_s_text_select_color, Color.BLUE)
        chooseBackgroundColor =
            typedArray.getColor(R.styleable.SlideBar_s_BackgroundColor, Color.GREEN)
        chooseStyle = typedArray.getInt(R.styleable.SlideBar_s_select_style, 0)
        typedArray.recycle()
    }

    /**
     * 设置字母默认色
     */
    fun setDefaultColor(color: Int) {
        defaultColor = color
    }

    /**
     * 设置字母选中色
     */
    fun setChooseColor(color: Int) {
        chooseColor = color
    }

    /**
     * 设置选中时控件的背景色
     */
    fun setChooseBackgroundColor(color: Int) {
        chooseBackgroundColor = color
    }

    /**
     * 设置选中时控件的风格
     */
    fun setChooseStyle(style: Int) {
        chooseStyle = style
    }

    /**
     * 文本字体大小  单位：sp
     */
    fun setTextSize(size: Int) {
        textSize = size
    }

    /**
     * 设置数据
     */
    fun setData(data: List<T>) {
        letters.clear()
        letters.addAll(data)
        invalidate()
    }

    /**
     * 获取数据
     */
    private fun getItem(index: Int): T {
        return letters[index]
    }

    /**
     * 获取显示的字母
     */
    private fun getLetter(index: Int): String {
        val t = getItem(index)
        return if (t is ILetter) {
            (t as ILetter).getLetter() ?: ""
        } else t.toString()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width
        val height = height

        //字母的个数
        val len = letters.size
        if (len == 0) return
        //单个字母的高度
        val singleHeight = height / len
        if (isTouch && chooseBackgroundColor != Color.TRANSPARENT && chooseStyle != Style.NONE) { //触摸时画出背景色
            paint.isAntiAlias = true
            paint.color = chooseBackgroundColor
            when (chooseStyle) {
                Style.CIRCLE -> { //选中 圆形背景效果
                    val maxValue = max(width, singleHeight).toFloat()
                    val minValue = min(width, singleHeight).toFloat()
                    val offset = singleHeight / 6
                    canvas.drawArc(
                        RectF(
                            (maxValue - minValue) / 2,
                            (singleHeight * index + offset).toFloat(),
                            singleHeight + (maxValue - minValue) / 2,
                            (singleHeight * index + singleHeight + offset).toFloat()
                        ), 0f, 360f, true, paint
                    )
                }
                Style.STRETCH -> { //选中背景拉伸效果
                    canvas.drawArc(
                        RectF(0f, 0f, width.toFloat(), (singleHeight * index).toFloat()),
                        0f,
                        360f,
                        true,
                        paint
                    )
                }
                else -> { //默认：全椭圆背景效果
                    canvas.drawArc(
                        RectF(0f, 0f, width.toFloat(), singleHeight.toFloat()),
                        180f,
                        180f,
                        true,
                        paint
                    )
                    canvas.drawRect(
                        RectF(
                            0f,
                            (singleHeight / 2).toFloat(),
                            width.toFloat(),
                            (height - singleHeight / 2).toFloat()
                        ), paint
                    )
                    canvas.drawArc(
                        RectF(
                            0f, (height - singleHeight).toFloat(), width.toFloat(), height.toFloat()
                        ), 0f, 180f, true, paint
                    )
                }
            }
        }

        //画字母
        for (i in 0 until len) {
            // 设置字体格式
            paint.typeface = Typeface.DEFAULT
            paint.textAlign = Paint.Align.CENTER
            // 抗锯齿
            paint.isAntiAlias = true
            // 设置字体大小
            paint.textSize = textSize.toFloat()
            if (i == index) { //选中时的画笔颜色
                paint.color = chooseColor
            } else { //未选中时的画笔颜色
                paint.color = defaultColor
            }
            //触摸时设为粗体字
            paint.isFakeBoldText = isTouch

            //要画的字母的x,y坐标
            val x = (width / 2).toFloat()
            val y = singleHeight * (i + 1) - paint.measureText(getLetter(i)) / 2
            //画字母
            canvas.drawText(getLetter(i), x, y, paint)
            //重置画笔
            paint.reset()
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (letters.size == 0) return false
        //当前选中字母的索引
        val index = (event.y / height * letters.size).toInt()
        //老的索引
        val oldIndex = this.index
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                isTouch = true
                if (index != oldIndex && index >= 0 && index < letters.size) {
                    this.index = index
                    onTouchLetterChangeListener?.onTouchLetterChange(isTouch, getItem(index))
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                isTouch = false
                if (index >= 0 && index < letters.size) {
                    onTouchLetterChangeListener?.onTouchLetterChange(isTouch, getItem(index))
                }
                this.index = -1
                invalidate()
            }
        }
        return true
    }

    /**
     * 字母改变监听接口
     */
    interface OnTouchLetterChangeListener<T> {
        fun onTouchLetterChange(isTouch: Boolean, letter: T)
    }

    init {
        initAttrs(attrs)
    }
}