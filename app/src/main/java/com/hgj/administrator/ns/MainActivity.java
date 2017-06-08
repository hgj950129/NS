package com.hgj.administrator.ns;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;

public class MainActivity extends Activity {

    MyView mAnimView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 全屏显示窗口
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //强制横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // 显示自定义的游戏View
        mAnimView = new MyView(this);
        setContentView(mAnimView);
    }

    public class MyView extends SurfaceView implements Callback,Runnable ,SensorEventListener{

        /**每50帧刷新一次屏幕**/
        public static final int TIME_IN_FRAME = 50;
        /** 游戏画笔 **/
        Paint mPaint = null;
        Paint p=new Paint(Color.BLACK);
        Paint mTextPaint = null;
        SurfaceHolder mSurfaceHolder = null;

        /** 控制游戏更新循环 **/
        boolean mRunning = false;

        /** 游戏画布 **/
        Canvas mCanvas = null;


        /**控制游戏循环**/
        boolean mIsRunning = false;

        /**SensorManager管理器**/
        private SensorManager mSensorMgr = null;
        Sensor mSensor = null;

        /**手机屏幕宽高**/
        int mScreenWidth = 0;
        int mScreenHeight = 0;

        float masterX=40;
        float masterY=30;

        /**小球资源文件越界区域**/
        private int mScreenBallWidth = 0;
        private int mScreenBallHeight = 0;

        /**游戏背景文件**/
        private Bitmap mbitmapBg;

        /**小球资源文件**/
        private Bitmap mbitmapBall;
        private Bitmap mbitmapMaster;

        /**小球的坐标位置**/
        private float mPosX = 200;
        private float mPosY = 0;

        /**重力感应X轴 Y轴 Z轴的重力值**/
        private float mGX = 0;
        private float mGY = 0;
        private float mGZ = 0;

        public MyView(Context context) {
            super(context);
            /** 拿到SurfaceHolder对象 **/
            mSurfaceHolder = this.getHolder();
            /** 将mSurfaceHolder添加到Callback回调函数中 **/
            mSurfaceHolder.addCallback(this);
            /** 创建画布 **/
            mCanvas = new Canvas();
            /** 创建曲线画笔 **/
            mPaint = new Paint();

            mPaint.setColor(Color.WHITE);
            /**加载小球资源**/
            mbitmapBall = BitmapFactory.decodeResource(this.getResources(), R.drawable.ball);
            /**加载游戏背景**/
            mbitmapBg = BitmapFactory.decodeResource(this.getResources(), R.drawable.timg);
            /**加载怪物资源**/
            mbitmapMaster=BitmapFactory.decodeResource(this.getResources(),R.drawable.master);
            /**得到SensorManager对象**/
            mSensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
            mSensor = mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            // 注册listener，第三个参数是检测的精确度
            //SENSOR_DELAY_GAME    游戏开发中使用
            mSensorMgr.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
            Log.d("shunxu","A1");
        }

        private void Draw() {



            mCanvas.drawColor(Color.WHITE);

            /**绘制小球**/
            mCanvas.drawBitmap(mbitmapBall, mPosX,mPosY, mPaint);
            /**X轴 Y轴 Z轴的重力值**/
            int Min =-5;
            int Max =5;
            int resultX = Min + (int)(Math.random() * ((Max - Min) + 1));
            int resultY = Min + (int)(Math.random() * ((Max - Min) + 1));
            Log.d("XY","X=: "+resultX+"Y=: "+resultY);
            masterX +=resultX;
            masterY +=resultY;
            if(mScreenWidth<masterX){
                masterX=mScreenWidth;
            }
            if (mScreenHeight<masterY){
                masterY=mScreenHeight;
            }
            Matrix matrix=new Matrix();
            matrix.postRotate(30,masterX,masterY);
            mCanvas.drawBitmap(mbitmapMaster,matrix,mPaint);
            mCanvas.drawText("X轴重力值 ：" + mGX, 0, 20, p);
            mCanvas.drawText("Y轴重力值 ：" + mGY, 0, 40, p);
            mCanvas.drawText("Z轴重力值 ：" + mGZ, 0, 60, p);
            Log.d("shunxu","B1");

//            if(masterY==mPosY&&masterX==mPosX){
////                Matrix matrix=new Matrix();
////                matrix.postRotate(30,bitmap.getWidth()/2,bitmap.getHeight()/2);
////                matrix.postTranslate(width/2-bitmap.getWidth()/2,height/2-bitmap.getHeight()/2);
////                c.drawBitmap(bitmap,matrix,paint);
//
//            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {

        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            /**开始游戏主循环线程**/
            mIsRunning = true;
            new Thread(this).start();
            /**得到当前屏幕宽高**/
            mScreenWidth = this.getWidth();
            mScreenHeight = this.getHeight();
            /**得到小球越界区域**/
            mScreenBallWidth = mScreenWidth - mbitmapBall.getWidth();
            mScreenBallHeight = mScreenHeight - mbitmapBall.getHeight();
            Log.d("shunxu","C1");
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mIsRunning = false;
            Log.d("shunxu","D1");
        }

        @Override
        public void run() {
            while (mIsRunning) {

                /** 取得更新游戏之前的时间 **/
                long startTime = System.currentTimeMillis();

                /** 在这里加上线程安全锁 **/
                synchronized (mSurfaceHolder) {
                    /** 拿到当前画布 然后锁定 **/
                    mCanvas = mSurfaceHolder.lockCanvas();
                    Draw();
                    /** 绘制结束后解锁显示在屏幕上 **/
                    mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                }

                /** 取得更新游戏结束的时间 **/
                long endTime = System.currentTimeMillis();

                /** 计算出游戏一次更新的毫秒数 **/
                int diffTime = (int) (endTime - startTime);

                /** 确保每次更新时间为50帧 **/
                while (diffTime <= TIME_IN_FRAME) {
                    diffTime = (int) (System.currentTimeMillis() - startTime);
                    /** 线程等待 **/
                    Thread.yield();
                }
                Log.d("shunxu","E1");
            }

        }

        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            mGX = event.values[0];
            mGY= event.values[1];
            mGZ = event.values[2];

            //这里乘以2是为了让小球移动的更快
            mPosX -= mGX * 2;
            mPosY += mGY * 2;
            Log.d("shunxu","F1");
            //检测小球是否超出边界
            if (mPosX < 0) {
                mPosX = 0;
            } else
            if (mPosX > mScreenBallWidth) {
                mPosX = mScreenBallWidth;
            }
            if (mPosY < 0) {
                mPosY = 0;
            } else
                if (mPosY > mScreenBallHeight) {
                mPosY = mScreenBallHeight;
            }
        }
    }
}