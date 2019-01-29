package com.example.ycb.mywuzi.ai;

import android.support.annotation.IntDef;
import com.example.ycb.mywuzi.util.Const;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.example.ycb.mywuzi.util.Const.Companion;

/**
 * Created by ZhangHao on 2017/7/25.
 * 简单的AI
 */

public class AI implements Runnable {
    //棋盘信息
    private int[][] chessArray;
    //电脑执子（默认黑子）
    private int aiChess = Companion.getBLACK_CHESS();
    //所有无子位置的信息集合
    private List<Point> pointList;
    //ai落子结束回调
    private AICallBack callBack;
    //棋盘宽高（panelLength）
    private int panelLength;
    //游戏等级
    private int mLevelGame = 0;

    /**
     * 评分表（落子优先级评分）
     * FIVE 至少能五子相连
     * LIVE_X 表示X个连在一起的子，两边都没有被堵住
     * DEAD_X 表示X个连在一起的子，一边被堵住
     * DEAD 表示两边被堵住
     */
    private final static int FIVE = 10000;
    private final static int LIVE_FOUR = 4500;
    private final static int DEAD_FOUR = 2000;
    private final static int LIVE_THREE = 900;
    private final static int DEAD_THREE = 400;
    private final static int LIVE_TWO = 150;
    private final static int DEAD_TWO = 70;
    private final static int LIVE_ONE = 30;
    private final static int DEAD_ONE = 15;
    private final static int DEAD = 1;

    public AI(int[][] chessArray, AICallBack callBack) {
        pointList = new ArrayList<>();
        this.chessArray = chessArray;
        this.callBack = callBack;
        this.panelLength = chessArray.length;
    }

    //ai开始落子
    public void aiBout() {
        pointList.clear();
        new Thread(this).start();
    }

    //判断落子的优先级评分
    private void checkPriority(Point p) {
        int aiPriority = checkSelf(p.getX(), p.getY());
        int userPriority = checkUser(p.getX(), p.getY());
        p.setPriority(aiPriority >= userPriority ? aiPriority : userPriority);
    }

    //获取当前点，ai优先级评分
    private int checkSelf(int x, int y) {
        return getHorizontalPriority(x, y, aiChess)
                + getVerticalPriority(x, y, aiChess)
                + getLeftSlashPriority(x, y, aiChess)
                + getRightSlashPriority(x, y, aiChess);
    }

    //获取当前点，玩家优先级评分
    private int checkUser(int x, int y) {
        int userChess;
        if (aiChess == Companion.getWHITE_CHESS()) {
            userChess = Companion.getBLACK_CHESS();
        } else {
            userChess = Companion.getWHITE_CHESS();
        }
        return getHorizontalPriority(x, y, userChess)
                + getVerticalPriority(x, y, userChess)
                + getLeftSlashPriority(x, y, userChess)
                + getRightSlashPriority(x, y, userChess);
    }

    //设置等级
    public void setLevel(int levelGame){
        mLevelGame = levelGame;
    }

    //通过线程选择最佳落点
    @Override
    public synchronized void run() {
        //清空pointList
        pointList.clear();
        int blankCount = 0;
        for (int i = 0; i < panelLength; i++)
            for (int j = 0; j < panelLength; j++) {
                if (chessArray[i][j] == Companion.getNO_CHESS()) {
                    Point p = new Point(i, j);
                    checkPriority(p);
                    pointList.add(p);
                    blankCount++;
                }
            }
        //遍历pointList，找到优先级最高的Point
        bubbleSort(pointList);
        Point max;
        switch (mLevelGame){
            case Const.LEVEL_LOW:
                max = pointList.get(2);
                break;
            case Const.LEVEL_MIDIUM:
                max = pointList.get(1);
                break;
            case Const.LEVEL_HIGH:
                max = pointList.get(0);
                break;
                default:
                    max = pointList.get(0);
        }


//        for (Point point : pointList) {
//            if (max.getPriority() < point.getPriority()) {
//                max = point;
//            }
//        }

        //AI先手或者用户先手第一次落子时
        if (blankCount >= panelLength * panelLength - 1) {
            max = getStartPoint();
        }
        //休眠2秒
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //落子，并将结果回调
        chessArray[max.getX()][max.getY()] = aiChess;
        callBack.aiAtTheBell(max.getX(),max.getY());
    }

    //冒泡排序
    private void bubbleSort(List<Point> list) {
        int len = list.size();
        for (int i = 0; i < len - 1; i++) {
            for (int j = 0; j < len - 1 - i; j++) {
                Point point1 =  list.get(j);
                Point point2 =  list.get(j+1);
                if (point1.getPriority() < point2.getPriority()) {        // 相邻元素两两对比
                    Point point3 = point2;
                    Point point4 = point1;
//                    point2 = point1;
////                    point1 = point3;
                    list.remove(j+1);
                    list.add(j+1,point4);
                    list.remove(j);
                    list.add(j,point3);
                }
            }
        }
    }


        public void setAiChess(int aiChess) {
        this.aiChess = aiChess;
    }

    //AI先手或者用户先手第一次落子时，随机获取一个点落子
    private Point getStartPoint() {
        //该点是否可用标识
        boolean isUse = true;
        //在中间位置随机生成一个点
        Random random = new Random();
        int x = random.nextInt(5) + 5;
        int y = random.nextInt(5) + 5;
        //确保周围不存在其他棋子
        for (int i = x - 1; i <= x + 1; i++)
            for (int j = y - 1; j <= y + 1; j++) {
                if (chessArray[i][j] != Companion.getNO_CHESS()) {
                    isUse = false;
                }
            }
        if (isUse) {
            return new Point(x, y);
        } else {
            return getStartPoint();
        }
    }

    /**
     * 判断指定点chessArray[x][y]横向优先级
     *
     * @param x     数组下标
     * @param y     数组下标
     * @param chess 棋子颜色
     * @return 该点优先级评分
     */
    private int getHorizontalPriority(int x, int y, int chess) {
        //指定棋子相连数
        int connectCount = 1;
        //左边是否被堵住
        boolean isStartStem = false;
        //右边是否被堵住
        boolean isEndStem = false;

        //先向左边计算
        //如果当前位置y = 0,即在棋盘的边缘位置,则左边必然被堵住
        if (y == 0) {
            isStartStem = true;
        } else {
            //遍历左边
            for (int i = y - 1; i >= 0; i--) {
                //如果不是指定棋子
                if (chessArray[x][i] != chess) {
                    //不是自己的棋子，则左边被堵住或者是空位
                    isStartStem = chessArray[x][i] != Companion.getNO_CHESS();
                    break;
                } else {
                    connectCount++;
                    if (i == 0) {
                        //在边缘位置，则被挡住
                        isStartStem = true;
                    }
                }
            }
        }

        //再向右边计算
        //如果当前位置y = panelLength,即在棋盘的边缘位置,则右边必然被堵住
        if (y == panelLength - 1) {
            isEndStem = true;
        } else {
            //遍历右边
            for (int i = y + 1; i < panelLength; i++) {
                //如果不是指定棋子
                if (chessArray[x][i] != chess) {
                    //不是自己的棋子，则左边被堵住或者是空位
                    isEndStem = chessArray[x][i] != Companion.getNO_CHESS();
                    break;
                } else {
                    connectCount++;
                    if (i == panelLength - 1) {
                        //在边缘位置，则被挡住
                        isEndStem = true;
                    }
                }
            }
        }
        //计算优先级评分
        return calcPriority(connectCount, isStartStem, isEndStem);
    }

    /**
     * 判断指定点chessArray[x][y]纵向优先级
     *
     * @param x     数组下标
     * @param y     数组下标
     * @param chess 棋子颜色
     * @return 该点优先级评分
     */
    private int getVerticalPriority(int x, int y, int chess) {
        //指定棋子相连数
        int connectCount = 1;
        //左边是否被堵住
        boolean isStartStem = false;
        //右边是否被堵住
        boolean isEndStem = false;

        //先向上边计算
        //在棋盘的边缘位置,则上边必然被堵住
        if (x == 0) {
            isStartStem = true;
        } else {
            //向上遍历
            for (int i = x - 1; i >= 0; i--) {
                //如果不是指定棋子
                if (chessArray[i][y] != chess) {
                    //不是自己的棋子，则左边被堵住或者是空位
                    isStartStem = chessArray[i][y] != Companion.getNO_CHESS();
                    break;
                } else {
                    connectCount++;
                    if (i == 0) {
                        //在边缘位置，则被挡住
                        isStartStem = true;
                    }
                }
            }
        }

        //再向右边计算
        //如果当前位置y = panelLength,即在棋盘的边缘位置,则下边必然被堵住
        if (x == panelLength - 1) {
            isEndStem = true;
        } else {
            //向下遍历
            for (int i = x + 1; i < panelLength; i++) {
                //如果不是指定棋子
                if (chessArray[i][y] != chess) {
                    //不是自己的棋子，则左边被堵住或者是空位
                    isEndStem = chessArray[i][y] != Companion.getNO_CHESS();
                    break;
                } else {
                    connectCount++;
                    if (i == panelLength - 1) {
                        //在边缘位置，则被挡住
                        isEndStem = true;
                    }
                }
            }
        }
        //计算优先级评分
        return calcPriority(connectCount, isStartStem, isEndStem);
    }

    /**
     * 判断指定点chessArray[x][y]左斜（左上到右下）优先级
     *
     * @param x     数组下标
     * @param y     数组下标
     * @param chess 棋子颜色
     * @return 该点优先级评分
     */
    private int getLeftSlashPriority(int x, int y, int chess) {
        //指定棋子相连数
        int connectCount = 1;
        //左边是否被堵住
        boolean isStartStem = false;
        //右边是否被堵住
        boolean isEndStem = false;

        //先向左上计算
        //在棋盘的边缘位置,则左上必然被堵住
        if (x == 0 || y == 0) {
            isStartStem = true;
        } else {
            //向左上遍历
            for (int i = x - 1, j = y - 1; i >= 0 && j >= 0; i--, j--) {
                //如果不是指定棋子
                if (chessArray[i][j] != chess) {
                    //不是自己的棋子，则左边被堵住或者是空位
                    isStartStem = chessArray[i][j] != Companion.getNO_CHESS();
                    break;
                } else {
                    connectCount++;
                    if (i == 0 || j == 0) {
                        //在边缘位置，则被挡住
                        isStartStem = true;
                    }
                }
            }
        }

        //再向右下计算
        //在棋盘的边缘位置,则右下必然被堵住
        if (x == panelLength - 1 || y == panelLength - 1) {
            isEndStem = true;
        } else {
            //遍历右下
            for (int i = x + 1, j = y + 1; i < panelLength && j < panelLength; i++, j++) {
                //如果不是指定棋子
                if (chessArray[i][j] != chess) {
                    //不是自己的棋子，则左边被堵住或者是空位
                    isEndStem = chessArray[i][j] != Companion.getNO_CHESS();
                    break;
                } else {
                    connectCount++;
                    if (i == panelLength - 1 || j == panelLength - 1) {
                        //在边缘位置，则被挡住
                        isEndStem = true;
                    }
                }
            }
        }
        //计算优先级评分
        return calcPriority(connectCount, isStartStem, isEndStem);
    }

    /**
     * 判断指定点chessArray[x][y]右斜（右上到左下）优先级
     *
     * @param x     数组下标
     * @param y     数组下标
     * @param chess 棋子颜色
     * @return 该点优先级评分
     */
    private int getRightSlashPriority(int x, int y, int chess) {
        //指定棋子相连数
        int connectCount = 1;
        //左边是否被堵住
        boolean isStartStem = false;
        //右边是否被堵住
        boolean isEndStem = false;

        //先向右上计算
        //在棋盘的边缘位置,则右上必然被堵住
        if (x == panelLength - 1 || y == 0) {
            isStartStem = true;
        } else {
            //向左上遍历
            for (int i = x + 1, j = y - 1; i < panelLength && j >= 0; i++, j--) {
                //如果不是指定棋子
                if (chessArray[i][j] != chess) {
                    //不是自己的棋子，则左边被堵住或者是空位
                    isStartStem = chessArray[i][j] != Companion.getNO_CHESS();
                    break;
                } else {
                    connectCount++;
                    if (i == panelLength - 1 || j == 0) {
                        //在边缘位置，则被挡住
                        isStartStem = true;
                    }
                }
            }
        }

        //再向左下计算
        //在棋盘的边缘位置,则左下必然被堵住
        if (x == 0 || y == panelLength - 1) {
            isEndStem = true;
        } else {
            //遍历右边
            for (int i = x - 1, j = y + 1; i >= 0 && j < panelLength; i--, j++) {
                //如果不是指定棋子
                if (chessArray[i][j] != chess) {
                    //不是自己的棋子，则被堵住或者是空位
                    isEndStem = chessArray[i][j] != Companion.getNO_CHESS();
                    break;
                } else {
                    connectCount++;
                    if (i == 0 || j == panelLength - 1) {
                        //在边缘位置，则被挡住
                        isEndStem = true;
                    }
                }
            }
        }
        //计算优先级评分
        return calcPriority(connectCount, isStartStem, isEndStem);
    }


    /**
     * 根据相连数以及开始结束是否被堵住计算优先级评分
     *
     * @param connectCount 相连数
     * @param isStartStem  开始是否被堵住
     * @param isEndStem    结束是否被堵住
     * @return 优先级评分
     */
    private int calcPriority(int connectCount, boolean isStartStem, boolean isEndStem) {
        //优先级评分
        int priority = 0;
        if (connectCount >= 5) {
            //能够五连
            priority = FIVE;
        } else {
            //不能五连
            if (isStartStem && isEndStem) {
                //开始结束都被堵住,死棋
                priority = DEAD;
            } else if (isStartStem == isEndStem) {
                //两边都没被堵住
                if (connectCount == 4) {
                    priority = LIVE_FOUR;
                } else if (connectCount == 3) {
                    priority = LIVE_THREE;
                } else if (connectCount == 2) {
                    priority = LIVE_TWO;
                } else if (connectCount == 1) {
                    priority = LIVE_ONE;
                }
            } else {
                //其中一边被堵住
                if (connectCount == 4) {
                    priority = DEAD_FOUR;
                } else if (connectCount == 3) {
                    priority = DEAD_THREE;
                } else if (connectCount == 2) {
                    priority = DEAD_TWO;
                } else if (connectCount == 1) {
                    priority = DEAD_ONE;
                }
            }
        }
        return priority;
    }

}
