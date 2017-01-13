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
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;

public class ViewRubber extends ViewBase {

	private static final int COUNT_EDGE = 20;
	private static final int COUNT_INDICES = 6 * (COUNT_EDGE - 1)
			* (COUNT_EDGE - 1);

	private static final float[][][] FACE_DATA = {
			{ { .3f, .5f, 1f }, { 0, 8, 2, 9, 20, 10, 1, 11, 3 } },
			{ { .3f, .5f, 1f }, { 6, 18, 4, 14, 21, 17, 7, 19, 5 } },
			{ { 1f, .5f, .3f }, { 4, 15, 0, 17, 22, 9, 5, 16, 1 } },
			{ { 1f, .5f, .3f }, { 2, 12, 6, 10, 23, 14, 3, 13, 7 } },
			{ { .5f, 1f, .3f }, { 4, 18, 6, 15, 24, 12, 0, 8, 2 } },
			{ { .5f, 1f, .3f }, { 1, 11, 3, 16, 25, 13, 5, 19, 7 } } };
	private static final float[][] FACE_VERTICES = { { -1, 1, 1 },
			{ -1, -1, 1 }, { 1, 1, 1 }, { 1, -1, 1 }, { -1, 1, -1 },
			{ -1, -1, -1 }, { 1, 1, -1 }, { 1, -1, -1 }, { 0, 1, 1 },
			{ -1, 0, 1 }, { 1, 0, 1 }, { 0, -1, 1 }, { 1, 1, 0 }, { 1, -1, 0 },
			{ 1, 0, -1 }, { -1, 1, 0 }, { -1, -1, 0 }, { -1, 0, -1 },
			{ 0, 1, -1 }, { 0, -1, -1 }, { 0, 0, 1 }, { 0, 0, -1 },
			{ -1, 0, 0 }, { 1, 0, 0 }, { 0, 1, 0 }, { 0, -1, 0 } };
	private static final float[][] FACE_VERTICES_SOURCE = new float[FACE_VERTICES.length][];
	private static final float[][] FACE_VERTICES_TARGET = new float[FACE_VERTICES.length][];

	private ShortBuffer mBufferIndices;
	private FloatBuffer mBufferVertices;
	private float[] mEyeSource = { 0, 0, 5 };
	private float[] mEyeTarget = { 0, 0, 5 };

	private float[] mMatrixProjection = new float[16];
	private float[] mMatrixView = new float[16];
	private long mRenderTime;
	private boolean[] mShaderCompilerSupport = new boolean[1];
	private EffectsShader mShaderRubber = new EffectsShader();

	public ViewRubber(Context context) {
		super(context);

		ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 2 * COUNT_EDGE
				* COUNT_EDGE);
		mBufferVertices = buffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
		for (int i = 0; i < COUNT_EDGE; ++i) {
			float y = (float) i / (COUNT_EDGE - 1);
			for (int j = 0; j < COUNT_EDGE; ++j) {
				float x = (float) j / (COUNT_EDGE - 1);
				mBufferVertices.put(x).put(y);
			}
		}
		mBufferVertices.position(0);

		buffer = ByteBuffer.allocateDirect(2 * COUNT_INDICES);
		mBufferIndices = buffer.order(ByteOrder.nativeOrder()).asShortBuffer();
		for (int i = 0; i < COUNT_EDGE - 1; ++i) {
			for (int j = 0; j < COUNT_EDGE - 1; ++j) {
				short index = (short) (j * COUNT_EDGE + i);
				mBufferIndices.put(index).put((short) (index + COUNT_EDGE))
						.put((short) (index + 1));
				mBufferIndices.put((short) (index + COUNT_EDGE))
						.put((short) (index + COUNT_EDGE + 1))
						.put((short) (index + 1));
			}
		}
		mBufferIndices.position(0);

		for (int i = 0; i < FACE_VERTICES.length; ++i) {
			FACE_VERTICES_SOURCE[i] = new float[3];
			FACE_VERTICES_TARGET[i] = new float[3];
		}

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
		if (time - mRenderTime > 2000) {
			for (int i = 0; i < 3; ++i) {
				mEyeSource[i] = mEyeTarget[i];
				mEyeTarget[i] = (float) (Math.random() * 4 - 2);
				mEyeTarget[i] += mEyeTarget[i] > 0 ? 3 : -3;
			}
			for (int i = 0; i < FACE_VERTICES.length; ++i) {
				for (int j = 0; j < 3; ++j) {
					FACE_VERTICES_SOURCE[i][j] = FACE_VERTICES_TARGET[i][j];
					FACE_VERTICES_TARGET[i][j] = FACE_VERTICES[i][j]
							+ FACE_VERTICES[i][j]
							* (float) (Math.random() - 0.5);
				}
			}
			mRenderTime = time;
		}

		float t = (time - mRenderTime) / 2000f;
		t = t * t * (3 - 2 * t);

		final float[] eye = new float[3];
		for (int i = 0; i < 3; ++i) {
			eye[i] = mEyeSource[i] + (mEyeTarget[i] - mEyeSource[i]) * t;
		}

		Matrix.setLookAtM(mMatrixView, 0, eye[0], eye[1], eye[2], 0, 0, 0, 0,
				1, 0);

		mShaderRubber.useProgram();

		GLES20.glEnable(GLES20.GL_CULL_FACE);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

		GLES20.glUniformMatrix4fv(mShaderRubber.getHandle("uViewM"), 1, false,
				mMatrixView, 0);
		GLES20.glUniformMatrix4fv(mShaderRubber.getHandle("uProjectionM"), 1,
				false, mMatrixProjection, 0);

		GLES20.glVertexAttribPointer(mShaderRubber.getHandle("aPosition"), 2,
				GLES20.GL_FLOAT, false, 0, mBufferVertices);
		GLES20.glEnableVertexAttribArray(mShaderRubber.getHandle("aPosition"));

		for (float[][] face : FACE_DATA) {
			GLES20.glUniform3fv(mShaderRubber.getHandle("uColor"), 1, face[0],
					0);

			final float[] lines = new float[27];
			for (int i = 0; i < 9; ++i) {
				float[] verticesSource = FACE_VERTICES_SOURCE[(int) face[1][i]];
				float[] verticesTarget = FACE_VERTICES_TARGET[(int) face[1][i]];
				for (int j = 0; j < 3; ++j) {
					float value = verticesSource[j]
							+ (verticesTarget[j] - verticesSource[j]) * t;
					lines[i * 3 + j] = value;
				}
			}

			GLES20.glUniform3fv(mShaderRubber.getHandle("uCtrl"), 9, lines, 0);
			GLES20.glDrawElements(GLES20.GL_TRIANGLES, COUNT_INDICES,
					GLES20.GL_UNSIGNED_SHORT, mBufferIndices);
		}

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
			String vertexSource = loadRawString(R.raw.rubber_vs);
			String fragmentSource = loadRawString(R.raw.rubber_fs);
			mShaderRubber.setProgram(vertexSource, fragmentSource);
		} catch (Exception ex) {
			showError(ex.getMessage());
		}
	}

}
