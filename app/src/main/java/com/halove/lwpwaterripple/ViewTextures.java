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

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.os.SystemClock;

/**
 * Textures view renderer class.
 */
public class ViewTextures extends ViewBase {

	private ByteBuffer mBufferQuad;
	private Matrix mMatrixBackground = new Matrix();
	private Matrix mMatrixForeground = new Matrix();
	private Matrix mMatrixForegroundAnim = new Matrix();
	private boolean[] mShaderCompilerSupport = new boolean[1];
	private EffectsShader mShaderTextures = new EffectsShader();
	private int[] mTextureIds = new int[2];
	private Worker mWorker = new Worker();

	public ViewTextures(Context context) {
		super(context);

		// Full view quad buffer.
		final byte[] QUAD = { -1, 1, -1, -1, 1, 1, 1, -1 };
		mBufferQuad = ByteBuffer.allocateDirect(8);
		mBufferQuad.put(QUAD).position(0);

		setEGLContextClientVersion(2);
		setRenderer(this);
		setRenderMode(RENDERMODE_WHEN_DIRTY);
		queueEvent(mWorker);
	}

	@Override
	public void onDrawFrame(GL10 unused) {

		if (mShaderCompilerSupport[0] == false) {
			GLES20.glClearColor(0f, 0f, 0f, 0f);
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
			return;
		}

		mShaderTextures.useProgram();

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureIds[0]);
		GLES20.glUniform1i(mShaderTextures.getHandle("sBackground"), 0);
		GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureIds[1]);
		GLES20.glUniform1i(mShaderTextures.getHandle("sForeground"), 1);

		final float[] matrix = new float[9];
		mMatrixBackground.getValues(matrix);
		transpose(matrix);
		GLES20.glUniformMatrix3fv(mShaderTextures.getHandle("uBackgroundM"), 1,
				false, matrix, 0);

		mMatrixForegroundAnim.getValues(matrix);
		transpose(matrix);
		GLES20.glUniformMatrix3fv(mShaderTextures.getHandle("uForegroundM"), 1,
				false, matrix, 0);

		GLES20.glVertexAttribPointer(mShaderTextures.getHandle("aPosition"), 2,
				GLES20.GL_BYTE, false, 0, mBufferQuad);
		GLES20.glEnableVertexAttribArray(mShaderTextures.getHandle("aPosition"));
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

		queueEvent(mWorker);
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		GLES20.glViewport(0, 0, width, height);

		GLES20.glGenTextures(2, mTextureIds, 0);

		// Instantiate background texture.
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureIds[0]);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
				GLES20.GL_REPEAT);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
				GLES20.GL_REPEAT);

		// Instantiate foreground texture.
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureIds[1]);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
				GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
				GLES20.GL_CLAMP_TO_EDGE);

		// Generate background texture.
		final Paint paint = new Paint();
		paint.setTypeface(Typeface.MONOSPACE);
		paint.setFakeBoldText(true);
		paint.setColor(0xFF2040A0);
		paint.setTextSize(32);
		setTextureText("world ", paint, mTextureIds[0], mMatrixBackground);
		mMatrixBackground.postScale(8, 8f * height / width);
		mMatrixBackground.preRotate(-35);

		// Generate foreground texture.
		paint.setColor(0xFFA05030);
		paint.setTextSize(128);
		setTextureText("hello", paint, mTextureIds[1], mMatrixForeground);
		mMatrixForeground.postScale(1f, (float) height / width);
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
			vertexSource = loadRawString(R.raw.textures_vs);
			fragmentSource = loadRawString(R.raw.textures_fs);
			mShaderTextures.setProgram(vertexSource, fragmentSource);
		} catch (Exception ex) {
			showError(ex.getMessage());
		}
	}

	/**
	 * Updates texture with given values plus alters matrix.
	 */
	private void setTextureText(String text, Paint paint, int textureId,
			Matrix matrix) {
		int width = (int) (paint.measureText(text) + 2.5f);
		int height = (int) (paint.descent() - paint.ascent() + 2.5f);

		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);

		final Canvas canvas = new Canvas();
		canvas.setBitmap(bitmap);
		canvas.drawColor(Color.TRANSPARENT);
		canvas.drawText(text, 1, 1 - paint.ascent(), paint);

		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
		bitmap.recycle();

		matrix.reset();
		matrix.postScale(0.5f, -0.5f * width / height);
		matrix.postTranslate(0.5f, 0.5f);
	}

	/**
	 * Transpose 3x3 matrix in place.
	 */
	private void transpose(float[] matrix) {
		for (int i = 0; i < 2; ++i) {
			for (int j = i + 1; j < 3; ++j) {
				float tmp = matrix[j * 3 + i];
				matrix[j * 3 + i] = matrix[i * 3 + j];
				matrix[i * 3 + j] = tmp;
			}
		}
	}

	/**
	 * Worker runnable.
	 */
	private class Worker implements Runnable {

		private PointF mPivotSource = new PointF();
		private PointF mPivotTarget = new PointF();
		private long mRenderTime;
		private float mRotateSource;
		private float mRotateTarget;
		private PointF mTranslateSource = new PointF();
		private PointF mTranslateTarget = new PointF();

		@Override
		public void run() {
			long time = SystemClock.uptimeMillis();
			if (time - mRenderTime > 2000) {
				mTranslateSource.set(mTranslateTarget);
				mTranslateTarget.x = (float) (Math.random() * 0.5 - 0.25);
				mTranslateTarget.y = (float) (Math.random() * 0.5 - 0.25);
				mPivotSource.set(mPivotTarget);
				mPivotTarget.x = (float) (Math.random() * 2.0 - 1.0);
				mPivotTarget.y = (float) (Math.random() * 2.0 - 1.0);
				mRotateSource = mRotateTarget;
				mRotateTarget = (float) (Math.random() * 120 - 60);
				mRenderTime = time;
			}

			float t = (time - mRenderTime) / 2000f;
			t = t * t * (3 - 2 * t);

			float tx = mTranslateSource.x
					+ (mTranslateTarget.x - mTranslateSource.x) * t;
			float ty = mTranslateSource.y
					+ (mTranslateTarget.y - mTranslateSource.y) * t;
			float px = mPivotSource.x + (mPivotTarget.x - mPivotSource.x) * t;
			float py = mPivotSource.y + (mPivotTarget.y - mPivotSource.y) * t;
			float r = mRotateSource + (mRotateTarget - mRotateSource) * t;

			mMatrixForegroundAnim.set(mMatrixForeground);
			mMatrixForegroundAnim.postTranslate(tx, ty);
			mMatrixForegroundAnim.preRotate(r, px, py);

			requestRender();
		}
	}
}
