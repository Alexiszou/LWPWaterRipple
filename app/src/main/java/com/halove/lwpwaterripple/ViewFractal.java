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
import android.graphics.Matrix;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.util.FloatMath;
import android.util.SparseArray;
import android.view.MotionEvent;

public class ViewFractal extends ViewBase {

	private ByteBuffer mBufferQuad;
	private Matrix mMatrixMove = new Matrix();
	private Matrix mMatrixView = new Matrix();
	private SparseArray<StructPointer> mPointers = new SparseArray<StructPointer>();
	private boolean mShaderCompilerSupport[] = new boolean[1];
	private EffectsShader mShaderFractal = new EffectsShader();
	private int mWidth, mHeight;

	public ViewFractal(Context context) {
		super(context);

		// Full view quad buffer.
		final byte[] QUAD = { -1, 1, -1, -1, 1, 1, 1, -1 };
		mBufferQuad = ByteBuffer.allocateDirect(8);
		mBufferQuad.put(QUAD).position(0);

		setEGLContextClientVersion(2);
		setRenderer(this);
		setRenderMode(RENDERMODE_WHEN_DIRTY);
	}

	@Override
	public void onDrawFrame(GL10 unused) {

		if (mShaderCompilerSupport[0] == false) {
			GLES20.glClearColor(0f, 0f, 0f, 0f);
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
			return;
		}

		mShaderFractal.useProgram();

		final float matrix[] = new float[9];
		mMatrixView.getValues(matrix);
		transpose(matrix);
		GLES20.glUniformMatrix3fv(mShaderFractal.getHandle("uViewMatrix"), 1,
				false, matrix, 0);
		mMatrixMove.getValues(matrix);
		transpose(matrix);
		GLES20.glUniformMatrix3fv(mShaderFractal.getHandle("uMoveMatrix"), 1,
				false, matrix, 0);

		GLES20.glVertexAttribPointer(mShaderFractal.getHandle("aPosition"), 2,
				GLES20.GL_BYTE, false, 0, mBufferQuad);
		GLES20.glEnableVertexAttribArray(mShaderFractal.getHandle("aPosition"));
		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		mWidth = width;
		mHeight = height;
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
			String vertexSource = loadRawString(R.raw.fractal_vs);
			String fragmentSource = loadRawString(R.raw.fractal_fs);
			mShaderFractal.setProgram(vertexSource, fragmentSource);
		} catch (Exception ex) {
			showError(ex.getMessage());
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent me) {

		switch (me.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN: {
			int ai = me.getActionIndex();
			StructPointer pointer = new StructPointer();
			pointer.mPositionDown.set(me.getX(ai), me.getY(ai));
			mPointers.put(me.getPointerId(ai), pointer);
			return true;
		}
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP: {
			int pid = me.getPointerId(me.getActionIndex());
			mPointers.remove(pid);
			for (int i = 0; i < mPointers.size(); ++i) {
				StructPointer pointer = mPointers.valueAt(i);
				pointer.mPositionDown.set(pointer.mPositionMove);
			}
			mMatrixView.preConcat(mMatrixMove);
			mMatrixMove.reset();
			return true;
		}
		case MotionEvent.ACTION_MOVE: {
			for (int i = 0; i < me.getPointerCount(); ++i) {
				StructPointer pointer = mPointers.get(me.getPointerId(i));
				pointer.mPositionMove.set(me.getX(i), me.getY(i));
			}

			if (mPointers.size() == 1) {
				PointF ptDown = mPointers.valueAt(0).mPositionDown;
				PointF ptMove = mPointers.valueAt(0).mPositionMove;

				float dx = (ptDown.x - ptMove.x) * 2 / mWidth;
				float dy = (ptMove.y - ptDown.y) * 2 / mHeight;

				mMatrixMove.setTranslate(dx, dy);
			}
			if (mPointers.size() == 2) {
				PointF ptDown1 = mPointers.valueAt(0).mPositionDown;
				PointF ptMove1 = mPointers.valueAt(0).mPositionMove;
				PointF ptDown2 = mPointers.valueAt(1).mPositionDown;
				PointF ptMove2 = mPointers.valueAt(1).mPositionMove;

				float dx1 = ptDown1.x - ptDown2.x;
				float dy1 = ptDown1.y - ptDown2.y;
				float lenOrig = FloatMath.sqrt(dx1 * dx1 + dy1 * dy1);
				float dx2 = ptMove1.x - ptMove2.x;
				float dy2 = ptMove1.y - ptMove2.y;
				float lenMove = FloatMath.sqrt(dx2 * dx2 + dy2 * dy2);

				float scale = lenOrig / lenMove;

				double angleOrig = Math.acos(dx1 / lenOrig);
				angleOrig = dy1 > 0 ? angleOrig : -angleOrig;
				double angleMove = Math.acos(dx2 / lenMove);
				angleMove = dy2 > 0 ? angleMove : -angleMove;
				double angle = angleMove - angleOrig;

				float dx = (ptDown1.x - ptMove1.x) * 2 / mWidth;
				float dy = (ptMove1.y - ptDown1.y) * 2 / mHeight;

				float px = (ptMove1.x / mWidth) * 2 - 1;
				float py = (ptMove1.y / mHeight) * 2 - 1;

				mMatrixMove.setTranslate(dx, dy);
				mMatrixMove.preRotate((float) Math.toDegrees(angle), px, -py);
				mMatrixMove.preScale(scale, scale, px, -py);
			}

			requestRender();

			return true;
		}
		}
		return false;
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
	 * Private pointer position holder class.
	 */
	private class StructPointer {
		PointF mPositionDown = new PointF();
		PointF mPositionMove = new PointF();
	}

}
