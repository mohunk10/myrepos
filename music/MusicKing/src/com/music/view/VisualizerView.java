package com.music.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.audiofx.Visualizer;
import android.media.audiofx.Visualizer.OnDataCaptureListener;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

/**
 * <b>自定义可视化效果(频谱视图)，使用一定要加android.permission.RECORD_AUDIO权限</b></br>
 * 
 */
public class VisualizerView extends View {

	private final int REFRESH_TIME = 100;// 刷新间隔
	private final int TIMES_MAX = 10;// 刷新最大次数
	private final int ACCELERATION = 1;// 下落加速度(感觉最合适的一个数值)

	private Visualizer mVisualizer = null;
	private Handler mHandler = null;

	private int mSpectrumNum = 48;// 截取一部分
	private int times = 0;// 记录刷新次数

	private boolean error = false;// 是否初始化出错
	private boolean canDrawLines = false;// 是否允许画线

	private byte[] mBytes = null;// FFT源数组
	private float[] linesArray = null;// 线数组
	private float[] pointsArray = null;// 点数组
	private float[] tempArray = null;// 临时数组，用于记住点的位置

	private Rect mRect = null;// 矩形区域
	private Paint linesPaint = null;// 频谱线画笔
	private Paint pointsPaint = null;// 频谱点画笔
	private Paint errorPaint = null;// 频谱点画笔

	public VisualizerView(Context context) {
		super(context);
		init();
	}

	public VisualizerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public VisualizerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	/**
	 * 生成一个VisualizerView对象，使音频频谱的波段能够反映到 VisualizerView上
	 * 
	 * @param audioSessionId
	 *            MediaPlayer.getAudioSessionId()
	 */
	public void setupVisualizerFx(int audioSessionId) {
		mHandler.removeCallbacks(runnable);
		canDrawLines = true;// 允许画线
		error = false;// 初始值未出错
		times = 0;

		if (mVisualizer != null) {
			mVisualizer.setEnabled(false);
			mVisualizer.release();
			mVisualizer = null;
		}

		// 4.0的模拟器肯定会报错，必须拋异常处理，可能部分手机也会出错
		try {
			mVisualizer = new Visualizer(audioSessionId);
			// 参数内必须是2的位数
			mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[0]);
			/*
			 * 设置允许波形表示，并且捕获它，注意(true/false)，
			 * 第一个表示是否执行onWaveFormDataCapture，第二个表示是否执行onFftDataCapture
			 */
			mVisualizer.setDataCaptureListener(new OnDataCaptureListener() {

				@Override
				public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
					updateVisualizer(waveform);
				}

				@Override
				public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
					updateVisualizer(fft);
				}
			}, Visualizer.getMaxCaptureRate() / 2, false, true);
			mVisualizer.setEnabled(true);
		} catch (Exception e) {
			errorPaint = new Paint();
			errorPaint.setColor(Color.argb(230, 255, 255, 255));
			errorPaint.setTextSize(20);
			error = true;
			e.printStackTrace();
		}
	}

	/**
	 * 释放并回收VisualizerView对象
	 */
	public void releaseVisualizerFx() {
		if (mVisualizer == null) {
			return;
		}
		canDrawLines = false;// 停止画线
		mVisualizer.setEnabled(false);
		mHandler.postDelayed(runnable, REFRESH_TIME);
	}

	// 初始化画笔
	private void init() {
		mRect = new Rect();
		mHandler = new Handler();

		linesPaint = new Paint();
		linesPaint.setStrokeWidth(5f);
		linesPaint.setAntiAlias(true);
		linesPaint.setColor(Color.argb(230, 255, 255, 255));

		pointsPaint = new Paint();
		pointsPaint.setStrokeWidth(5f);
		pointsPaint.setAntiAlias(true);
		pointsPaint.setColor(Color.argb(130, 255, 255, 255));

		initByte();
	}

	/**
	 * 初始化FFT源数组，赋值为1的目的是能够一开始就绘制频谱，初始化的目地即重新归位
	 */
	private void initByte() {
		mBytes = new byte[mSpectrumNum];
		for (int i = 0; i < mSpectrumNum; i++) {
			mBytes[i] = 0;
		}
	}

	private void updateVisualizer(byte[] mbyte) {

		byte[] model = new byte[mbyte.length / 2 + 1];

		model[0] = (byte) Math.abs(mbyte[0]);
		for (int i = 2, j = 1; j < mSpectrumNum;) {
			model[j] = (byte) Math.hypot(mbyte[i], mbyte[i + 1]);
			i += 2;
			j++;
		}

		mBytes = model;
		invalidate();
	}

	/**
	 * 自由落体运算{h=(1/2)*g*t^2}
	 * 
	 * @param time
	 *            时间
	 * @return 运动的距离
	 */
	private float freeFall(float time) {
		float h = ACCELERATION * time * time / 2;
		return h;
	}

	private Runnable runnable = new Runnable() {

		@Override
		public void run() {
			invalidate();
			if (times < TIMES_MAX) {
				if (times == 1) {
					initByte();// 跳过一次，可能onFftDataCapture会再回调一次，初始化就没意义了
				}
				mHandler.postDelayed(this, REFRESH_TIME);
			}
			times++;
		}
	};

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mBytes == null) {
			return;
		}

		if (error) {
			String s = "可视化效果初始化失败";
			canvas.drawText(s, getWidth() / 2 - errorPaint.measureText(s) / 2, getHeight() / 2, errorPaint);
			return;
		}

		int length = mSpectrumNum * 4;
		if (linesArray == null || linesArray.length < length) {
			linesArray = new float[length];
		}
		length = mSpectrumNum * 2;
		if (pointsArray == null || pointsArray.length < length) {
			pointsArray = new float[length];
		}
		length = mSpectrumNum * 3;
		if (tempArray == null || tempArray.length < length) {
			tempArray = new float[length];
			for (int l = 0; l < length; l++) {
				if (l % 3 == 0) {
					continue;// 数组指针是3的倍数初值必须为0
				}
				tempArray[l] = 1024;// 赋予一个足够大的初始值(应该够大了吧)，否则一开始点会从上面落下来
			}
		}

		mRect.set(0, 0, getWidth(), getHeight());

		/************ 绘制波形 ************/
		// for (int i = 0; i < mBytes.length - 1; i++) {
		// linesArray[i * 4] = mRect.width() * i / (mBytes.length - 1);
		// linesArray[i * 4 + 1] = mRect.height() / 2
		// + ((byte) (mBytes[i] + 128)) * (mRect.height() / 2) / 128;
		// linesArray[i * 4 + 2] = mRect.width() * (i + 1) / (mBytes.length -
		// 1);
		// linesArray[i * 4 + 3] = mRect.height() / 2
		// + ((byte) (mBytes[i + 1] + 128)) * (mRect.height() / 2)
		// / 128;
		// }

		/************ 绘制柱状频谱 ************/
		final int baseX = mRect.width() / mSpectrumNum;
		final int height = mRect.height();

		for (int i = 0; i < mSpectrumNum; i++) {
			if (mBytes[i] < 0) {
				mBytes[i] = 127;
			}

			final int x = baseX * i + baseX / 2;

			if (canDrawLines) {
				// 两点构成一条直线(垂直)
				linesArray[i * 4] = x;// 起始点X坐标
				linesArray[i * 4 + 1] = height;// 起始点Y坐标
				linesArray[i * 4 + 2] = x;// 终点X坐标
				linesArray[i * 4 + 3] = height - mBytes[i];// 终点Y坐标
			}

			// 线终点位置的高度减去一个数就会出现在线的上方，反之在下方
			int y = height - mBytes[i] - 3;
			if (tempArray[i * 3 + 1] > y) {// 记住的位置比现在的大，也就是最新的位置更高了
				tempArray[i * 3] = x;
				tempArray[i * 3 + 1] = y;// 记住现在的最高位置
				tempArray[i * 3 + 2] = 0;// 一定要归0
				pointsArray[i * 2] = x;
				pointsArray[i * 2 + 1] = y;// 更高了就刷新呗
			} else {// 记住的位置比现在的小，说明该自由落体了
				tempArray[i * 3] = x;
				float ti = tempArray[i * 3 + 2];// 取出上次记忆的次数
				float temp = tempArray[i * 3 + 1] + freeFall(ti);// 加上自由落体运算
				tempArray[i * 3 + 2] = ++ti;// 自增一次
				temp = temp > y ? y : temp;// 不能跑到线下挡住了，判断一下让点归位
				tempArray[i * 3 + 1] = temp;
				pointsArray[i * 2] = x;
				pointsArray[i * 2 + 1] = tempArray[i * 3 + 1];// 刷新当前点
			}
		}

		if (canDrawLines) {
			canvas.drawLines(linesArray, linesPaint);
		}
		canvas.drawPoints(pointsArray, pointsPaint);
	}

	@Override
	protected void onDetachedFromWindow() {
		try {
			mHandler.removeCallbacks(runnable);// 确保移除线程
			super.onDetachedFromWindow();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
