/*
   Copyright 2012 Harri Smatt

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.halove.lwpwaterripple;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.os.SystemClock;
import android.util.FloatMath;

public class ViewBlob extends ViewBase {

	private static final int CURVE_COUNT = 20;

	private FloatBuffer mBufferCurves;
	private FloatBuffer mBufferPoints;
	private PointF mPoint0 = new PointF();
	private PointF mPoint1 = new PointF();
	private PointF mPoint2 = new PointF();
	private boolean[] mShaderCompilerSupport = new boolean[1];
	private EffectsShader mShaderCurve = new EffectsShader();
	private EffectsShader mShaderFill = new EffectsShader();
	private EffectsShader mShaderLine = new EffectsShader();
	private Worker mWorker = new Worker();

	public ViewBlob(Context context) {
		super(context);

		ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 2 * 3);
		mBufferPoints = buffer.order(ByteOrder.nativeOrder()).asFloatBuffer();

		buffer = ByteBuffer.allocateDirect(4 * 4 * CURVE_COUNT);
		mBufferCurves = buffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
		for (int i = 0; i < CURVE_COUNT; ++i) {
			float t = (float) i / (CURVE_COUNT - 1);
			mBufferCurves.put(t).put(0);
			mBufferCurves.put(t).put(FloatMath.sin((float) Math.PI * t));
		}
		mBufferCurves.position(0);

		setEGLContextClientVersion(2);
		setRenderer(this);
		setRenderMode(RENDERMODE_WHEN_DIRTY);
		queueEvent(mWorker);
	}

	@Override
	public void onDrawFrame(GL10 unused) {
		GLES20.glClearColor(0, 0, 0, 1);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

		if (!mShaderCompilerSupport[0]) {
			return;
		}

		GLES20.glDisable(GLES20.GL_CULL_FACE);
		GLES20.glDisable(GLES20.GL_DEPTH_TEST);

		//
		// Render curved areas.
		//
		mShaderCurve.useProgram();
		GLES20.glVertexAttribPointer(mShaderCurve.getHandle("aPosition"), 2,
				GLES20.GL_FLOAT, false, 0, mBufferCurves);
		GLES20.glEnableVertexAttribArray(mShaderCurve.getHandle("aPosition"));

		renderCurve(mShaderCurve, mPoint0, mPoint1, mPoint2);
		renderCurve(mShaderCurve, mPoint0, mPoint2, mPoint1);
		renderCurve(mShaderCurve, mPoint1, mPoint2, mPoint0);

		//
		// Fill blob inner area.
		//
		mShaderFill.useProgram();
		GLES20.glVertexAttribPointer(mShaderFill.getHandle("aPosition"), 2,
				GLES20.GL_FLOAT, false, 0, mBufferPoints);
		GLES20.glEnableVertexAttribArray(mShaderFill.getHandle("aPosition"));
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 3);

		//
		// Render blob control point lines.
		//
		mShaderLine.useProgram();
		GLES20.glVertexAttribPointer(mShaderLine.getHandle("aPosition"), 2,
				GLES20.GL_FLOAT, false, 0, mBufferPoints);
		GLES20.glEnableVertexAttribArray(mShaderLine.getHandle("aPosition"));
		GLES20.glLineWidth(5);
		GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, 3);

		queueEvent(mWorker);
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
	}

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		// Check if shader compiler is supported.
		GLES20.glGetBooleanv(GLES20.GL_SHADER_COMPILER, mShaderCompilerSupport,
				0);

		// If not, show user an error message and return immediately.
		if (!mShaderCompilerSupport[0]) {
			String msg = getContext().getString(R.string.error_shader_compiler);
			showError(msg);
			return;
		}

		try {
			String vertexSource, fragmentSource;
			vertexSource = loadRawString(R.raw.blob_line_vs);
			fragmentSource = loadRawString(R.raw.blob_line_fs);
			mShaderLine.setProgram(vertexSource, fragmentSource);
			vertexSource = loadRawString(R.raw.blob_fill_vs);
			fragmentSource = loadRawString(R.raw.blob_fill_fs);
			mShaderFill.setProgram(vertexSource, fragmentSource);
			vertexSource = loadRawString(R.raw.blob_curve_vs);
			fragmentSource = loadRawString(R.raw.blob_curve_fs);
			mShaderCurve.setProgram(vertexSource, fragmentSource);
		} catch (Exception ex) {
			showError(ex.getMessage());
		}
	}

	/**
	 * Renders a curve segment.
	 */
	private void renderCurve(EffectsShader shader, PointF p0, PointF p1,
			PointF p2) {

		final PointF dir = new PointF();
		dir.x = (p0.x + p1.x) * 0.5f - p2.x;
		dir.y = (p0.y + p1.y) * 0.5f - p2.y;
		dir.x *= 0.6f;
		dir.y *= 0.6f;

		GLES20.glUniform2f(shader.getHandle("uNormal0"), dir.x, dir.y);
		GLES20.glUniform2f(shader.getHandle("uNormal1"), dir.x, dir.y);

		GLES20.glUniform2f(shader.getHandle("uPoint0"), p0.x, p0.y);
		GLES20.glUniform2f(shader.getHandle("uPoint1"), p1.x, p1.y);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, CURVE_COUNT * 2);
	}

	/**
	 * Worker runnable.
	 */
	private class Worker implements Runnable {

		private PointF mPoint0Source = new PointF();
		private PointF mPoint0Target = new PointF();
		private PointF mPoint1Source = new PointF();
		private PointF mPoint1Target = new PointF();
		private PointF mPoint2Source = new PointF();
		private PointF mPoint2Target = new PointF();
		private long mRenderTime;

		@Override
		public void run() {

			long time = SystemClock.uptimeMillis();
			if (time - mRenderTime > 4000) {
				mPoint0Source.set(mPoint0Target);
				mPoint0Target.x = (float) (Math.random() * 2 - 1);
				mPoint0Target.y = (float) (Math.random() * 2 - 1);
				mPoint1Source.set(mPoint1Target);
				mPoint1Target.x = (float) (Math.random() * 2 - 1);
				mPoint1Target.y = (float) (Math.random() * 2 - 1);
				mPoint2Source.set(mPoint2Target);
				mPoint2Target.x = (float) (Math.random() * 2 - 1);
				mPoint2Target.y = (float) (Math.random() * 2 - 1);
				mRenderTime = time;
			}

			float t = (time - mRenderTime) / 4000f;
			t = t * t * (3 - 2 * t);

			mPoint0.x = mPoint0Source.x + (mPoint0Target.x - mPoint0Source.x)
					* t;
			mPoint0.y = mPoint0Source.y + (mPoint0Target.y - mPoint0Source.y)
					* t;
			mPoint1.x = mPoint1Source.x + (mPoint1Target.x - mPoint1Source.x)
					* t;
			mPoint1.y = mPoint1Source.y + (mPoint1Target.y - mPoint1Source.y)
					* t;
			mPoint2.x = mPoint2Source.x + (mPoint2Target.x - mPoint2Source.x)
					* t;
			mPoint2.y = mPoint2Source.y + (mPoint2Target.y - mPoint2Source.y)
					* t;

			mBufferPoints.position(0);
			mBufferPoints.put(mPoint0.x).put(mPoint0.y);
			mBufferPoints.put(mPoint1.x).put(mPoint1.y);
			mBufferPoints.put(mPoint2.x).put(mPoint2.y);
			mBufferPoints.position(0);

			requestRender();
		}
	}
}
