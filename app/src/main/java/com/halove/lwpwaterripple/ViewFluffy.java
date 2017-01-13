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
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;

public class ViewFluffy extends ViewBase {

	private long mAnimDuration;
	private long mAnimTime;
	private FloatBuffer mBufferVertices;
	private float[] mGravitySource = new float[3];
	private float[] mGravityTarget = new float[3];
	private float[] mMatrixProjection = new float[16];
	private float[] mMatrixRotation = new float[16];
	private float[] mMatrixView = new float[16];
	private float[] mPositionSource = new float[3];
	private float[] mPositionTarget = new float[3];
	private float[] mRotationSource = new float[3];
	private float[] mRotationTarget = new float[3];
	private boolean[] mShaderCompilerSupport = new boolean[1];
	private EffectsShader mShaderFluffy = new EffectsShader();

	public ViewFluffy(Context context) {
		super(context);

		mBufferVertices = ByteBuffer.allocateDirect(4 * 20)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		for (int i = 0; i < 20; ++i) {
			mBufferVertices.put(i);
		}
		mBufferVertices.position(0);

		setEGLContextClientVersion(2);
		setRenderer(this);
		setRenderMode(RENDERMODE_CONTINUOUSLY);
	}

	@Override
	public void onDrawFrame(GL10 unused) {
		GLES20.glClearColor(0f, 0f, 0f, 1f);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		if (mShaderCompilerSupport[0] == false) {
			return;
		}

		long time = SystemClock.uptimeMillis();
		if (time - mAnimTime > mAnimDuration) {
			for (int i = 0; i < 3; ++i) {
				mRotationSource[i] = mRotationTarget[i];
				mRotationTarget[i] = (float) (Math.random() * 10.0 - 5.0);
				mPositionSource[i] = mPositionTarget[i];
				mPositionTarget[i] = (float) (Math.random() * 2.0 - 1.0);
				mGravitySource[i] = mGravityTarget[i];
				mGravityTarget[i] = (mPositionSource[i] - mPositionTarget[i]) * 0.1f;
			}
			mAnimTime = time;
			mAnimDuration = (long) (Math.random() * 1000 + 500);
		}
		float t = (time - mAnimTime) / (float) mAnimDuration;
		t = t * t * (3 - 2 * t);
		float rx = mRotationSource[0]
				+ (mRotationTarget[0] - mRotationSource[0]) * t;
		float ry = mRotationSource[1]
				+ (mRotationTarget[1] - mRotationSource[1]) * t;
		float rz = mRotationSource[2]
				+ (mRotationTarget[2] - mRotationSource[2]) * t;
		float px = mPositionSource[0]
				+ (mPositionTarget[0] - mPositionSource[0]) * t;
		float py = mPositionSource[1]
				+ (mPositionTarget[1] - mPositionSource[1]) * t;
		float pz = mPositionSource[2]
				+ (mPositionTarget[2] - mPositionSource[2]) * t;
		float gx = mGravitySource[0] + (mGravityTarget[0] - mGravitySource[0])
				* t;
		float gy = mGravitySource[1] + (mGravityTarget[1] - mGravitySource[1])
				* t;
		float gz = mGravitySource[2] + (mGravityTarget[2] - mGravitySource[2])
				* t;

		Matrix.setRotateM(mMatrixRotation, 0, 10, rx, ry, rz);

		mShaderFluffy.useProgram();

		GLES20.glUniformMatrix4fv(mShaderFluffy.getHandle("uRotationM"), 1,
				false, mMatrixRotation, 0);
		GLES20.glUniformMatrix4fv(mShaderFluffy.getHandle("uViewM"), 1, false,
				mMatrixView, 0);
		GLES20.glUniformMatrix4fv(mShaderFluffy.getHandle("uProjectionM"), 1,
				false, mMatrixProjection, 0);

		GLES20.glUniform3f(mShaderFluffy.getHandle("uPosition"), px, py, pz);
		GLES20.glUniform3f(mShaderFluffy.getHandle("uGravity"), gx, gy, gz);

		GLES20.glVertexAttribPointer(mShaderFluffy.getHandle("aPosition"), 1,
				GLES20.GL_FLOAT, false, 0, mBufferVertices);
		GLES20.glEnableVertexAttribArray(mShaderFluffy.getHandle("aPosition"));

		GLES20.glLineWidth(5f);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		for (int xy = 0; xy < 20; ++xy) {
			float sinXY = (float) Math.sin(rx * 0.1 * Math.PI + xy * Math.PI
					/ 10.0);
			float cosXY = (float) Math.cos(ry * 0.1 * Math.PI + xy * Math.PI
					/ 10.0);

			for (int z = 0; z < 20; ++z) {
				float sinZ = (float) Math.cos(rz * 0.1 * Math.PI + z * Math.PI
						/ 10.0);

				GLES20.glUniform3f(mShaderFluffy.getHandle("uDirection"),
						sinXY, cosXY, sinZ);
				GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, 20);
			}
		}
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		float aspect = (float) width / height;
		Matrix.perspectiveM(mMatrixProjection, 0, 60f, aspect, .1f, 10f);
		Matrix.setLookAtM(mMatrixView, 0, 0, 0, 2, 0, 0, 0, 0, 1, 0);
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
			String vertexSource = loadRawString(R.raw.fluffy_vs);
			String fragmentSource = loadRawString(R.raw.fluffy_fs);
			mShaderFluffy.setProgram(vertexSource, fragmentSource);
		} catch (Exception ex) {
			showError(ex.getMessage());
		}
	}

}
