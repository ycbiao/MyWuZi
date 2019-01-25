package com.example.ycb.mywuzi.widget

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.ycb.mywuzi.imp.OnGameStatusChangeListener
import com.example.ycb.mywuzi.R
import com.example.ycb.mywuzi.ai.AI
import com.example.ycb.mywuzi.ai.AICallBack
import com.example.ycb.mywuzi.util.Const
import com.example.ycb.mywuzi.util.Const.Companion.BLACK_CHESS
import com.example.ycb.mywuzi.util.Const.Companion.BLACK_WIN
import com.example.ycb.mywuzi.util.Const.Companion.NO_WIN
import com.example.ycb.mywuzi.util.Const.Companion.WHITE_CHESS
import com.example.ycb.mywuzi.util.Const.Companion.WHITE_WIN
import com.example.ycb.mywuzi.base.BaseActivity
import com.example.ycb.mywuzi.util.Const.Companion.MODEL_TYPE_RENJI
import com.example.ycb.mywuzi.util.Const.Companion.MODEL_TYPE_RENREN
import com.example.ycb.mywuzi.util.Const.Companion.NO_CHESS
import kotlinx.android.synthetic.main.checker_acticity.*
import java.util.ArrayList

/**
 * Created by biao on 2019/1/18.
 */
class WZQPanel : View{
    private var mPanelWidth: Int = 0       //棋盘宽度
    private var mLineHeight: Float = 0.toFloat()      //棋盘单行间距
    private var MAX_LINE: Int = 0//棋盘行列数

    private val mPaint = Paint()
    private var mPanelLineColor: Int = 0    //棋盘线的颜色

    private lateinit var mWhitePiece: Bitmap     //白棋的图片
    private lateinit var mBlackPiece: Bitmap     //黑棋的图片

    //棋子占行距的比例
    private val RATIO_PIECE_OF_LINE_HEIGHT = 3 * 1.0f / 4

    //是否将要下白棋
    private var mIsWhite = true
    //已下的白棋的列表
    private var mWhitePieceArray: ArrayList<Point> = ArrayList()
    //已下的黑棋的列表
    private var mBlackPieceArray: ArrayList<Point> = ArrayList()

    //游戏是否结束
    private var mIsGameOver: Boolean = false

    private val INIT_WIN = -1            //游戏开始时的状态

    private lateinit var mChessArray : Array<IntArray>

    private var mGameWinResult = INIT_WIN      //初始化游戏结果

    private var listener: OnGameStatusChangeListener? = null//游戏状态监听器

    private var MAX_COUNT_IN_LINE: Int = 0    //多少颗棋子相邻时赢棋

    private var model_type = 0;//模式

    private lateinit var ai:AI

    var bIsAiRun = false//是否轮到ai

    constructor(context: Context?) : this(context,null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs,0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        val a = context?.obtainStyledAttributes(attrs, R.styleable.WuziqiPanel)
        val n = a!!.indexCount
        for (i in 0 until n) {
            val attrName = a.getIndex(i)
            when (attrName) {
                //棋盘背景
                R.styleable.WuziqiPanel_panel_background -> {
                    val panelBackgroundBitmap = a.getDrawable(attrName) as BitmapDrawable
                    background = panelBackgroundBitmap
                }
                //棋盘线的颜色
                R.styleable.WuziqiPanel_panel_line_color -> mPanelLineColor = a.getColor(attrName, -0x78000000)
                //白棋图片
                R.styleable.WuziqiPanel_white_piece_img -> {
                    val whitePieceBitmap = a.getDrawable(attrName) as BitmapDrawable
                    mWhitePiece = whitePieceBitmap.bitmap
                }
                //黑棋图片
                R.styleable.WuziqiPanel_black_piece_img -> {
                    val blackPieceBitmap = a.getDrawable(attrName) as BitmapDrawable
                    mBlackPiece = blackPieceBitmap.bitmap
                }
                R.styleable.WuziqiPanel_max_count_line -> MAX_LINE = a.getInteger(attrName, 10)
                R.styleable.WuziqiPanel_max_win_count_piece -> MAX_COUNT_IN_LINE = a.getInteger(attrName, 5)
            }
        }
        init()
    }

    //初始化游戏数据
    private fun init() {
        mPaint.color = mPanelLineColor
        mPaint.isAntiAlias = true//抗锯齿
        mPaint.isDither = true//防抖动
        mPaint.style = Paint.Style.FILL
        if (mWhitePiece == null) {
            mWhitePiece = BitmapFactory.decodeResource(resources, R.mipmap.stone_w2)
        }
        if (mBlackPiece == null) {
            mBlackPiece = BitmapFactory.decodeResource(resources, R.mipmap.stone_b1)
        }
        mChessArray = Array(MAX_LINE){IntArray(MAX_LINE){it -> 0}}
        ai = AI(getChessArray(), AICallBack { x: Int, y: Int ->
            post{
                //                                    setAiChessArray()
                mBlackPieceArray.add(Point(x,y))
                LogUtil.LogMsg(context.javaClass,"$x,$y")
                bIsAiRun = false
                invalidate()
//                                    mIsWhite = !mIsWhite
            }
        })
    }

    //获取棋盘状态传入ai
    fun getChessArray(): Array<IntArray>{
        for (i in 0..MAX_LINE -1){
            for (j in 0..MAX_LINE - 1){
                mChessArray[i][j] == NO_CHESS
            }
        }
        for (point in  mWhitePieceArray){
            mChessArray[point.x][point.y] = Const.WHITE_CHESS
        }

        for (point in  mBlackPieceArray){
            mChessArray[point.x][point.y] = Const.BLACK_CHESS
        }

        return mChessArray
    }

//    //ai下子后更新状态
//    fun setAiChessArray(){
//        for (i in 0..MAX_LINE -1){
//            for (j in 0..MAX_LINE - 1){
//                if(mChessArray[i][j] == WHITE_CHESS){
//                    var point = Point(i,j)
//                    if(!mWhitePieceArray.contains(point))
//                        mWhitePieceArray.add(point)
//                }
//                if(mChessArray[i][j] == BLACK_CHESS){
//                    var point = Point(i,j)
//                    if(!mBlackPieceArray.contains(point))
//                        mBlackPieceArray.add(point)
//                }
//            }
//        }
//        invalidate()
//        mIsWhite = !mIsWhite
//    }
//
    /**
     * 模式选择
     */
    fun setModel(type:Int){
        model_type = type
    }

    //人机模式下悔棋
    fun retractWhiteRenji():Boolean{
        if(bIsAiRun){
            (context as BaseActivity).showToast("请等待电脑落子结束再悔棋")
            return false
        }
        if(mWhitePieceArray.size == 0){
            (context as BaseActivity).showToast("棋盘上没有白棋")
            return false
        }

        if(mWhitePieceArray.size != 0 && mBlackPieceArray.size != 0){
            mBlackPieceArray.remove(mBlackPieceArray.get(mBlackPieceArray.size -1))
            mWhitePieceArray.remove(mWhitePieceArray.get(mWhitePieceArray.size -1))
            invalidate()
            return true
        }
        return false
    }

    //人人模式下悔棋
    fun retractWhiteRenRen() : Boolean{
        if(mWhitePieceArray.size == 0){
            (context as BaseActivity).showToast("棋盘上没有白棋")
            return false
        }

        if(mWhitePieceArray.size != 0 && !mIsWhite){
            mWhitePieceArray.remove(mWhitePieceArray.get(mWhitePieceArray.size -1))
            mIsWhite = !mIsWhite
            invalidate()
            return true
        }
        return false
    }

    //人人模式下悔棋
    fun retractBlackRenRen() : Boolean{
        if(mBlackPieceArray.size == 0){
            (context as BaseActivity).showToast("棋盘上没有黑棋")
            return false
        }

        if(mBlackPieceArray?.size != 0 && mIsWhite){
            mBlackPieceArray?.remove(mBlackPieceArray.get(mBlackPieceArray.size - 1))
            mIsWhite = !mIsWhite
            invalidate()
            return true
        }
        return false
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)

        val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = View.MeasureSpec.getMode(heightMeasureSpec)

        var width = Math.min(widthSize, heightSize)

        //解决嵌套在ScrollView中时等情况出现的问题
        if (widthMode == View.MeasureSpec.UNSPECIFIED) {
            width = heightSize
        } else if (heightMode == View.MeasureSpec.UNSPECIFIED) {
            width = widthSize
        }

        setMeasuredDimension(width, width)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mPanelWidth = w
        mLineHeight = mPanelWidth * 1.0f / (MAX_LINE + 1)

        val pieceWidth = (mLineHeight * RATIO_PIECE_OF_LINE_HEIGHT).toInt()
        mWhitePiece = Bitmap.createScaledBitmap(mWhitePiece, pieceWidth, pieceWidth, false)
        mBlackPiece = Bitmap.createScaledBitmap(mBlackPiece, pieceWidth, pieceWidth, false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBoard(canvas)
        drawPiece(canvas)
        checkGameOver()
    }

    //重新开始游戏
    fun restartGame() {

        for (i in 0..MAX_LINE -1){
            for (j in 0..MAX_LINE - 1){
                mChessArray[i][j] = NO_CHESS
            }
        }

        mWhitePieceArray!!.clear()
        mBlackPieceArray!!.clear()
        mIsGameOver = false
        mGameWinResult = INIT_WIN
        invalidate()
    }

    //检查游戏是否结束
    private fun checkGameOver() {
        val whiteWin = checkFiveInLine(mWhitePieceArray as List<Point>)
        val blackWin = checkFiveInLine(mBlackPieceArray as List<Point>)
        val noWin = checkNoWin(whiteWin, blackWin)
        //如果游戏结束,获取游戏结果mGameWinResult
        if (whiteWin) {
            mGameWinResult = WHITE_WIN
        } else if (blackWin) {
            mGameWinResult = BLACK_WIN
        } else if (noWin) {
            mGameWinResult = NO_WIN
        }
        if (whiteWin || blackWin || noWin) {
            mIsGameOver = true
            //回调游戏状态接口
            if (listener != null) {
                listener!!.onGameOver(mGameWinResult)
            }
        }
    }

    //检查是否五子连珠
    private fun checkFiveInLine(points: List<Point>): Boolean {
        for (point in points) {
            val x = point.x
            val y = point.y

            val checkHorizontal = checkHorizontalFiveInLine(x, y, points)
            val checkVertical = checkVerticalFiveInLine(x, y, points)
            val checkLeftDiagonal = checkLeftDiagonalFiveInLine(x, y, points)
            val checkRightDiagonal = checkRightDiagonalFiveInLine(x, y, points)
            if (checkHorizontal || checkVertical || checkLeftDiagonal || checkRightDiagonal) {
                return true
            }
        }

        return false
    }

    //检查向右斜的线上有没有相同棋子的五子连珠
    private fun checkRightDiagonalFiveInLine(x: Int, y: Int, points: List<Point>): Boolean {
        var count = 1
        for (i in 1 until MAX_COUNT_IN_LINE) {
            if (points.contains(Point(x - i, y - i))) {
                count++
            } else {
                break
            }
        }
        if (count == MAX_COUNT_IN_LINE) {
            return true
        }
        for (i in 1 until MAX_COUNT_IN_LINE) {
            if (points.contains(Point(x + i, y + i))) {
                count++
            } else {
                break
            }

        }
        return if (count == MAX_COUNT_IN_LINE) {
            true
        } else false
    }

    //检查向左斜的线上有没有相同棋子的五子连珠
    private fun checkLeftDiagonalFiveInLine(x: Int, y: Int, points: List<Point>): Boolean {
        var count = 1
        for (i in 1 until MAX_COUNT_IN_LINE) {
            if (points.contains(Point(x - i, y + i))) {
                count++
            } else {
                break
            }
        }
        if (count == MAX_COUNT_IN_LINE) {
            return true
        }
        for (i in 1 until MAX_COUNT_IN_LINE) {
            if (points.contains(Point(x + i, y - i))) {
                count++
            } else {
                break
            }

        }
        return if (count == MAX_COUNT_IN_LINE) {
            true
        } else false
    }

    //检查竖线上有没有相同棋子的五子连珠
    private fun checkVerticalFiveInLine(x: Int, y: Int, points: List<Point>): Boolean {
        var count = 1
        for (i in 1 until MAX_COUNT_IN_LINE) {
            if (points.contains(Point(x, y + i))) {
                count++
            } else {
                break
            }
        }
        if (count == MAX_COUNT_IN_LINE) {
            return true
        }
        for (i in 1 until MAX_COUNT_IN_LINE) {
            if (points.contains(Point(x, y - i))) {
                count++
            } else {
                break
            }

        }
        return if (count == MAX_COUNT_IN_LINE) {
            true
        } else false
    }

    //检查横线上有没有相同棋子的五子连珠
    private fun checkHorizontalFiveInLine(x: Int, y: Int, points: List<Point>): Boolean {
        var count = 1
        for (i in 1 until MAX_COUNT_IN_LINE) {
            if (points.contains(Point(x - i, y))) {
                count++
            } else {
                break
            }
        }
        if (count == MAX_COUNT_IN_LINE) {
            return true
        }
        for (i in 1 until MAX_COUNT_IN_LINE) {
            if (points.contains(Point(x + i, y))) {
                count++
            } else {
                break
            }

        }
        return if (count == MAX_COUNT_IN_LINE) {
            true
        } else false
    }

    //检查是否和棋
    private fun checkNoWin(whiteWin: Boolean, blackWin: Boolean): Boolean {
        if (whiteWin || blackWin) {
            return false
        }
        val maxPieces = MAX_LINE * MAX_LINE
        //如果白棋和黑棋的总数等于棋盘格子数,说明和棋
        return if (mWhitePieceArray.size + mBlackPieceArray.size == maxPieces) {
            true
        } else false
    }

    //绘制棋子
    private fun drawPiece(canvas: Canvas) {
        run {
            var i = 0
            val n = mWhitePieceArray.size
            while (i < n) {
                val whitePoint = mWhitePieceArray.get(i)
                canvas.drawBitmap(
                    mWhitePiece,
                    (whitePoint.x + (1 - RATIO_PIECE_OF_LINE_HEIGHT/ 2)) * mLineHeight,
                    (whitePoint.y + (1 - RATIO_PIECE_OF_LINE_HEIGHT/ 2) ) * mLineHeight, null
                )
                i++
            }
        }
        var i = 0
        val n = mBlackPieceArray.size
        while (i < n) {
            val blackPoint = mBlackPieceArray.get(i)
            canvas.drawBitmap(
                mBlackPiece,
                (blackPoint.x + (1 - RATIO_PIECE_OF_LINE_HEIGHT / 2)) * mLineHeight,
                (blackPoint.y + (1 - RATIO_PIECE_OF_LINE_HEIGHT / 2)) * mLineHeight, null
            )
            i++
        }
    }

    //绘制棋盘
    private fun drawBoard(canvas: Canvas) {
        val w = mPanelWidth
        val lineHeight = mLineHeight

        for (i in 0 until MAX_LINE) {
            val startX = lineHeight.toInt()
            val endX = (w - lineHeight).toInt()

            val y = ((1 + i) * lineHeight).toInt()
            canvas.drawLine(startX.toFloat(), y.toFloat(), endX.toFloat(), y.toFloat(), mPaint)//画横线
            canvas.drawLine(y.toFloat(), startX.toFloat(), y.toFloat(), endX.toFloat(), mPaint)//画竖线
        }

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mIsGameOver) {
            return false
        }
        if (event.action == MotionEvent.ACTION_UP) {
            val x = event.x.toInt()
            val y = event.y.toInt()

            val p = getValidPoint(x, y)
            if (mWhitePieceArray.contains(p) || mBlackPieceArray.contains(p) || p.x == MAX_LINE || p.y == MAX_LINE) {
                return false
            }

            when(model_type){
                MODEL_TYPE_RENREN -> {
                    if (mIsWhite) {
                        mWhitePieceArray.add(p)
                    } else {
                        mBlackPieceArray.add(p)
                    }
                    mIsWhite = !mIsWhite
                    invalidate()
                }
                MODEL_TYPE_RENJI -> {
                    if (mIsWhite && !bIsAiRun) {
                        mWhitePieceArray.add(p)
                        invalidate()
                        postDelayed({
                            bIsAiRun = true
                            ai.aiBout()
                        },1000)
                    }
                }
            }
            return true
        }
        return true
    }

    //根据触摸点获取最近的格子位置
    private fun getValidPoint(x: Int, y: Int): Point {
        var xP = ((x - (RATIO_PIECE_OF_LINE_HEIGHT/2*mLineHeight)) / mLineHeight).toInt()
        var yP = ((y - (RATIO_PIECE_OF_LINE_HEIGHT/2*mLineHeight)) / mLineHeight).toInt()
        return Point(xP, yP)
    }


    /**
     * 当View被销毁时需要保存游戏数据
     */
    private val INSTANCE = "instance"
    private val INSTANCE_GAME_OVER = "instance_game_over"
    private val INSTANCE_WHITE_ARRAY = "instance_white_array"
    private val INSTANCE_BLACK_ARRAY = "instance_black_array"

    //保存游戏数据
    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        bundle.putParcelable(INSTANCE, super.onSaveInstanceState())
        bundle.putBoolean(INSTANCE_GAME_OVER, mIsGameOver)
        bundle.putParcelableArrayList(INSTANCE_WHITE_ARRAY, mWhitePieceArray)
        bundle.putParcelableArrayList(INSTANCE_BLACK_ARRAY, mBlackPieceArray)
        return bundle
    }

    //恢复游戏数据
    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is Bundle) {
            mIsGameOver = state.getBoolean(INSTANCE_GAME_OVER)
            mWhitePieceArray = state.getParcelableArrayList(INSTANCE_WHITE_ARRAY)
            mBlackPieceArray = state.getParcelableArrayList(INSTANCE_BLACK_ARRAY)
            super.onRestoreInstanceState(state.getParcelable(INSTANCE))
            return
        }
        super.onRestoreInstanceState(state)
    }

    //设置游戏状态监听器
    fun setOnGameStatusChangeListener(listener: OnGameStatusChangeListener) {
        this.listener = listener
    }
}