package com.example.ycb.mywuzi.util

/**
 * Created by biao on 2019/1/25.
 */
class Const{
    companion object {
        /**
         * 一些常量
         */
        //白棋
        val WHITE_CHESS = 1
        //黑棋
        val BLACK_CHESS = 2
        //无棋
        val NO_CHESS = 0
        //白棋赢
        val WHITE_WIN = 101
        //黑棋赢
        val BLACK_WIN = 102
        //平局
        val NO_WIN = 103

        val MODEL_TYPE = "MODEL_TYPE"
        //人人模式
        val MODEL_TYPE_RENREN = 1
        //人机模式
        val MODEL_TYPE_RENJI = 2
        //蓝牙模式
        val MODEL_TYPE_BLUE = 3
        //游戏等级低
        const val LEVEL_LOW = 1;
        //游戏等级中
        const val LEVEL_MIDIUM = 2;
        //游戏等级高
        const val LEVEL_HIGH = 3;

        const val REQUEST_BLUETOOTH = 0X1111
    }
}