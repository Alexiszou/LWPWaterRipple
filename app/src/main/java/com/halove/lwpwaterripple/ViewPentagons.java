/*
   Copyright 2013 Harri Smatt

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
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;

public class ViewPentagons extends ViewBase {

	private ByteBuffer mBufferVertices;
	private long mLastAnimTime;
	private float[] mLookAtSource = new float[3];
	private float[] mLookAtTarget = new float[3];
	private float[] mMatrixModel = new float[16];
	private float[] mMatrixProjection = new float[16];
	private float[] mMatrixView = new float[16];
	private Pentagon[] mPentagonArray = new Pentagon[50];
	private boolean[] mShaderCompilerSupport = new boolean[1];
	private EffectsShader mShaderPentagon = new EffectsShader();

	public ViewPentagons(Context context) {
		super(context);

		for (int i = 0; i < mPentagonArray.length; ++i) {
			mPentagonArray[i] = new Pentagon();
		}

		final byte[] VERTICES = { -1, 1, -1, -1, 1, 1, 1, -1 };
		mBufferVertices = ByteBuffer.allocateDirect(2 * 4);
		mBufferVertices.put(VERTICES).position(0);

		setEGLContextClientVersion(2);
		setRenderer(this);
		setRenderMode(RENDERMODE_CONTINUOUSLY);
	}

	@Override
	public void onDrawFrame(GL10 unused) {
		GLES20.glClearColor(0f, 0f, 0f, 1f);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

		if (mShaderCompilerSupport[0] == false) {
			return;
		}

		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

		long time = SystemClock.uptimeMillis();
		if (time - mLastAnimTime > 5000) {
			for (int i = 0; i < 3; ++i) {
				mLookAtSource[i] = mLookAtTarget[i];
				mLookAtTarget[i] = (float) (Math.random() * 2 - 1);
			}
			mLastAnimTime = time;
		}

		float t = (time - mLastAnimTime) / 5000f;
		t = t * t * (3 - 2 * t);
		float eyeX = mLookAtSource[0] + (mLookAtTarget[0] - mLookAtSource[0])
				* t;
		float eyeY = mLookAtSource[1] + (mLookAtTarget[1] - mLookAtSource[1])
				* t;
		float eyeZ = mLookAtSource[2] + (mLookAtTarget[2] - mLookAtSource[2])
				* t;

		Matrix.setLookAtM(mMatrixView, 0, eyeX, eyeY, eyeZ, 0, 0, 0, 0, 1, 0);

		mShaderPentagon.useProgram();

		GLES20.glUniformMatrix4fv(mShaderPentagon.getHandle("uProjectionM"), 1,
				false, mMatrixProjection, 0);
		GLES20.glUniformMatrix4fv(mShaderPentagon.getHandle("uViewM"), 1,
				false, mMatrixView, 0);
		GLES20.glUniform1f(mShaderPentagon.getHandle("uSize"), .2f);

		GLES20.glVertexAttribPointer(mShaderPentagon.getHandle("aPosition"), 2,
				GLES20.GL_BYTE, false, 0, mBufferVertices);
		GLES20.glEnableVertexAttribArray(mShaderPentagon.getHandle("aPosition"));

		for (Pentagon pentagon : mPentagonArray) {
			pentagon.mRotation += pentagon.mRotationSpeed;
			Matrix.setRotateM(mMatrixModel, 0, pentagon.mRotation, 0, 0, 1);
			GLES20.glUniformMatrix4fv(mShaderPentagon.getHandle("uModelM"), 1,
					false, mMatrixModel, 0);
			GLES20.glUniform3fv(mShaderPentagon.getHandle("uPosition"), 1,
					pentagon.mPosition, 0);
			GLES20.glUniform3fv(mShaderPentagon.getHandle("uColor"), 1,
					pentagon.mColor, 0);
			GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
		}

		GLES20.glDisable(GLES20.GL_BLEND);
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		float aspect = (float) width / height;
		Matrix.perspectiveM(mMatrixProjection, 0, 60f, aspect, .1f, 10f);
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
			String vertexSource = loadRawString(R.raw.pentagon_vs);
			String fragmentSource = loadRawString(R.raw.pentagon_fs);
			mShaderPentagon.setProgram(vertexSource, fragmentSource);
		} catch (Exception ex) {
			showError(ex.getMessage());
		}
	}

	private class Pentagon {
		public float[] mColor = new float[3];
		public float[] mPosition = new float[3];
		public float mRotation, mRotationSpeed;

		public Pentagon() {
			for (int i = 0; i < 3; ++i) {
				mPosition[i] = (float) (Math.random() * 2 - 1);
				mColor[i] = (float) (Math.random() * 0.5 + 0.5);
			}
			mRotation = (float) (Math.random() * 360);
			mRotationSpeed = (float) (Math.random() * 1.0 + 1.0);
		}

	}

}
