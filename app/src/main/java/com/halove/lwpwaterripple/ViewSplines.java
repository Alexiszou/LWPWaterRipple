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
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;

public class ViewSplines extends ViewBase {

	private static final int COUNT_SPLINES = 100;
	private static final int COUNT_SPLITS = 40;
	private static final int COUNT_VERTICES = 2 * COUNT_SPLITS;

	private FloatBuffer mBufferSpline;
	private float[] mMatrixModel = new float[16];
	private float[] mMatrixModelViewProjection = new float[16];
	private float[] mMatrixProjection = new float[16];
	private float[] mMatrixView = new float[16];
	private boolean[] mShaderCompilerSupport = new boolean[1];
	private EffectsShader mShaderSpline = new EffectsShader();
	private float[][] mSplines = new float[COUNT_SPLINES][];

	public ViewSplines(Context context) {
		super(context);

		ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 2 * COUNT_VERTICES);
		mBufferSpline = buffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
		for (int i = 0; i < COUNT_SPLITS; ++i) {
			float x = (float) i / (COUNT_SPLITS - 1);
			mBufferSpline.put(x).put(1).put(x).put(-1);
		}
		mBufferSpline.position(0);

		for (int i = 0; i < COUNT_SPLINES; ++i) {
			mSplines[i] = new float[3 * 4];
			for (int j = 0; j < mSplines[i].length; ++j) {
				mSplines[i][j] = (float) (Math.random() * 2 - 1);
			}
		}

		setEGLContextClientVersion(2);
		setRenderer(this);
		setRenderMode(RENDERMODE_CONTINUOUSLY);
	}

	@Override
	public void onDrawFrame(GL10 unused) {

		GLES20.glClearColor(0f, 0f, 0f, 0f);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		if (mShaderCompilerSupport[0] == false) {
			return;
		}

		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

		float angle = (SystemClock.uptimeMillis() % 5000) / 5000f * 360;
		Matrix.setRotateM(mMatrixModel, 0, angle, 1, 2, 0);

		Matrix.multiplyMM(mMatrixModelViewProjection, 0, mMatrixView, 0,
				mMatrixModel, 0);
		Matrix.multiplyMM(mMatrixModelViewProjection, 0, mMatrixProjection, 0,
				mMatrixModelViewProjection, 0);

		mShaderSpline.useProgram();

		GLES20.glUniformMatrix4fv(
				mShaderSpline.getHandle("uModelViewProjectionM"), 1, false,
				mMatrixModelViewProjection, 0);

		GLES20.glVertexAttribPointer(mShaderSpline.getHandle("aPosition"), 2,
				GLES20.GL_FLOAT, false, 0, mBufferSpline);
		GLES20.glEnableVertexAttribArray(mShaderSpline.getHandle("aPosition"));

		for (float[] ctrl : mSplines) {
			GLES20.glUniform3fv(mShaderSpline.getHandle("uCtrl"), 4, ctrl, 0);
			GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, COUNT_VERTICES);
		}
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		float aspect = (float) width / height;
		Matrix.perspectiveM(mMatrixProjection, 0, 60f, aspect, .1f, 10f);
		Matrix.setLookAtM(mMatrixView, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0);
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
			String vertexSource = loadRawString(R.raw.spline_vs);
			String fragmentSource = loadRawString(R.raw.spline_fs);
			mShaderSpline.setProgram(vertexSource, fragmentSource);
		} catch (Exception ex) {
			showError(ex.getMessage());
		}
	}

}
