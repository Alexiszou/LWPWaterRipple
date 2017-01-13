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

public class ViewCircles extends ViewBase {

	private final int CIRCLE_SPLITS = 8;
	private final int CIRCLE_VERTICES = 100;

	private FloatBuffer mBufferCircle;
	private ByteBuffer mBufferQuad;
	private Circle[] mCircles = new Circle[3];
	private EffectsShader mShaderBackground = new EffectsShader();
	private EffectsShader mShaderCircle = new EffectsShader();
	private boolean[] mShaderCompilerSupport = new boolean[1];

	public ViewCircles(Context context) {
		super(context);

		final byte[] QUAD = { -1, 1, -1, -1, 1, 1, 1, -1 };
		mBufferQuad = ByteBuffer.allocateDirect(8);
		mBufferQuad.put(QUAD).position(0);

		mBufferCircle = ByteBuffer.allocateDirect(4 * 4 * CIRCLE_VERTICES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		for (int i = 0; i < CIRCLE_VERTICES; ++i) {
			float t = (float) i / (CIRCLE_VERTICES - 1);
			mBufferCircle.put(t).put(1).put(t).put(0);
		}
		mBufferCircle.position(0);

		for (int i = 0; i < mCircles.length; ++i) {
			mCircles[i] = new Circle();
		}

		setEGLContextClientVersion(2);
		setRenderer(this);
		setRenderMode(RENDERMODE_CONTINUOUSLY);
	}

	@Override
	public void onDrawFrame(GL10 unused) {

		if (mShaderCompilerSupport[0] == false) {
			GLES20.glClearColor(0f, 0f, 0f, 0f);
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
			return;
		}

		mShaderBackground.useProgram();
		GLES20.glUniform4f(mShaderBackground.getHandle("uColorInner"), 1.0f,
				0.95f, 0.6f, 1.0f);
		GLES20.glUniform4f(mShaderBackground.getHandle("uColorCenter"), 0.7f,
				0.5f, 0.2f, 1.0f);
		GLES20.glUniform4f(mShaderBackground.getHandle("uColorOuter"), 0.4f,
				0.65f, 1.0f, 1.0f);

		GLES20.glVertexAttribPointer(mShaderBackground.getHandle("aPosition"),
				2, GLES20.GL_BYTE, false, 0, mBufferQuad);
		GLES20.glEnableVertexAttribArray(mShaderBackground
				.getHandle("aPosition"));

		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

		mShaderCircle.useProgram();
		GLES20.glUniform4f(mShaderCircle.getHandle("uColor"), 1f, 1f, 1f, 1f);

		GLES20.glVertexAttribPointer(mShaderCircle.getHandle("aPosition"), 2,
				GLES20.GL_FLOAT, false, 0, mBufferCircle);
		GLES20.glEnableVertexAttribArray(mShaderCircle.getHandle("aPosition"));

		for (Circle circle : mCircles) {
			float[] radiusInner = circle.getRadiusInner();
			float[] radiusOuter = circle.getRadiusOuter();
			int maxLength = Math.min(radiusInner.length, radiusOuter.length);

			GLES20.glUniformMatrix4fv(mShaderCircle.getHandle("uModelMatrix"),
					1, false, circle.getModelMatrix(), 0);

			for (int i = 0; i < maxLength; ++i) {
				float angleSource = (float) (2.0 * Math.PI * i / (maxLength - 1));
				float angleTarget = (float) (2.0 * Math.PI * (i + 1) / (maxLength - 1));

				GLES20.glUniform1f(mShaderCircle.getHandle("uAngleSource"),
						angleSource);
				GLES20.glUniform1f(mShaderCircle.getHandle("uAngleTarget"),
						angleTarget);
				GLES20.glUniform1f(mShaderCircle.getHandle("uRadiusInner"),
						radiusInner[i]);
				GLES20.glUniform1f(mShaderCircle.getHandle("uRadiusOuter"),
						radiusOuter[i]);

				GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0,
						2 * CIRCLE_VERTICES);
			}
		}

	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		float aspect = (float) width / height;
		final float[] projectionMatrix = new float[16];
		Matrix.orthoM(projectionMatrix, 0, -aspect, aspect, -1f, 1f, 0.1f, 10f);

		mShaderCircle.useProgram();
		GLES20.glUniformMatrix4fv(mShaderCircle.getHandle("uProjectionMatrix"),
				1, false, projectionMatrix, 0);
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
			vertexSource = loadRawString(R.raw.background_vs);
			fragmentSource = loadRawString(R.raw.background_fs);
			mShaderBackground.setProgram(vertexSource, fragmentSource);
			vertexSource = loadRawString(R.raw.circle_vs);
			fragmentSource = loadRawString(R.raw.circle_fs);
			mShaderCircle.setProgram(vertexSource, fragmentSource);
		} catch (Exception ex) {
			showError(ex.getMessage());
		}
	}

	private class Circle {
		private final float[] mModelMatrix = new float[16];
		private final float[] mPositionSource = new float[2];
		private final float[] mPositionTarget = new float[2];
		private long mPositionTime, mPositionDuration;
		private final float[] mRadiusInner = new float[CIRCLE_SPLITS];
		private final float[] mRadiusInnerSource = new float[CIRCLE_SPLITS];
		private final float[] mRadiusInnerTarget = new float[CIRCLE_SPLITS];
		private long mRadiusInnerTime, mRadiusInnerDuration;
		private final float[] mRadiusOuter = new float[CIRCLE_SPLITS];
		private final float[] mRadiusOuterSource = new float[CIRCLE_SPLITS];
		private final float[] mRadiusOuterTarget = new float[CIRCLE_SPLITS];
		private long mRadiusOuterTime, mRadiusOuterDuration;
		private float mRotationSource;
		private float mRotationTarget;
		private long mRotationTime, mRotationDuration;

		public float[] getModelMatrix() {
			long time = SystemClock.uptimeMillis();
			if (time - mPositionTime > mPositionDuration) {
				for (int i = 0; i < 2; ++i) {
					mPositionSource[i] = mPositionTarget[i];
					mPositionTarget[i] = (float) (Math.random() * 2.0 - 1.0);
				}
				mPositionTime = time;
				mPositionDuration = (long) (Math.random() * 5000) + 5000;
			}
			if (time - mRotationTime > mRotationDuration) {
				mRotationSource = mRotationTarget;
				mRotationTarget = (float) (360.0 * Math.random());
				mRotationTime = time;
				mRotationDuration = (long) (Math.random() * 5000) + 5000;
			}

			float tPosition = (float) (time - mPositionTime)
					/ mPositionDuration;
			tPosition = tPosition * tPosition * (3 - 2 * tPosition);
			float tRotation = (float) (time - mRotationTime)
					/ mRotationDuration;
			tRotation = tRotation * tRotation * (3 - 2 * tRotation);

			float x = mPositionSource[0]
					+ (mPositionTarget[0] - mPositionSource[0]) * tPosition;
			float y = mPositionSource[1]
					+ (mPositionTarget[1] - mPositionSource[1]) * tPosition;
			Matrix.setIdentityM(mModelMatrix, 0);
			Matrix.translateM(mModelMatrix, 0, x, y, -5);

			float angle = mRotationSource + (mRotationTarget - mRotationSource)
					* tRotation;
			Matrix.rotateM(mModelMatrix, 0, angle, 0, 0, 1);

			return mModelMatrix;
		}

		public float[] getRadiusInner() {
			long time = SystemClock.uptimeMillis();
			if (time - mRadiusInnerTime > mRadiusInnerDuration) {
				for (int i = 0; i < 8; ++i) {
					mRadiusInnerSource[i] = mRadiusInnerTarget[i];
					mRadiusInnerTarget[i] = (float) (Math.random() * 0.2 + 0.4);
				}
				mRadiusInnerTime = time;
				mRadiusInnerDuration = (long) (Math.random() * 5000) + 5000;
			}

			float t = (float) (time - mRadiusInnerTime) / mRadiusInnerDuration;
			t = t * t * (3 - 2 * t);

			for (int i = 0; i < 8; ++i) {
				mRadiusInner[i] = mRadiusInnerSource[i]
						+ (mRadiusInnerTarget[i] - mRadiusInnerSource[i]) * t;
			}

			return mRadiusInner;
		}

		public float[] getRadiusOuter() {
			long time = SystemClock.uptimeMillis();
			if (time - mRadiusOuterTime > mRadiusOuterDuration) {
				for (int i = 0; i < 8; ++i) {
					mRadiusOuterSource[i] = mRadiusOuterTarget[i];
					mRadiusOuterTarget[i] = (float) (Math.random() * 0.2 + 0.6);
				}
				mRadiusOuterTime = time;
				mRadiusOuterDuration = (long) (Math.random() * 5000) + 5000;
			}

			float t = (float) (time - mRadiusOuterTime) / mRadiusOuterDuration;
			t = t * t * (3 - 2 * t);

			for (int i = 0; i < 8; ++i) {
				mRadiusOuter[i] = mRadiusOuterSource[i]
						+ (mRadiusOuterTarget[i] - mRadiusOuterSource[i]) * t;
			}

			return mRadiusOuter;
		}

	}

}
