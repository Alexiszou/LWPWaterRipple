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
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.FloatMath;

/**
 * Particles Renderer and GLSurfaceView.
 */
public class ViewParticles extends ViewBase {

	private FloatBuffer mBufferLine;
	private ByteBuffer mBufferQuad;
	private float mEmitterDir;
	private float mEmitterDirSource;
	private float mEmitterDirTarget;
	private PointF mEmitterPos = new PointF();
	private PointF mEmitterPosCtrl0 = new PointF();
	private PointF mEmitterPosCtrl1 = new PointF();
	private PointF mEmitterPosCtrl2 = new PointF();
	private float[] mMatrixProjection = new float[16];
	private Vector<Particle> mParticles = new Vector<Particle>();
	private boolean[] mShaderCompilerSupport = new boolean[1];
	private EffectsShader mShaderEmitter = new EffectsShader();
	private EffectsShader mShaderParticle = new EffectsShader();
	private Worker mWorker = new Worker();

	public ViewParticles(Context context) {
		super(context);

		// Full view quad buffer.
		final byte[] QUAD = { -1, 1, -1, -1, 1, 1, 1, -1 };
		mBufferQuad = ByteBuffer.allocateDirect(8);
		mBufferQuad.put(QUAD).position(0);

		ByteBuffer buffer = ByteBuffer.allocateDirect(4 * 20);
		mBufferLine = buffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
		for (int i = 0; i < 20; ++i) {
			mBufferLine.put(i / 19f);
		}
		mBufferLine.position(0);

		setEGLContextClientVersion(2);
		setRenderer(this);
		setRenderMode(RENDERMODE_WHEN_DIRTY);
		queueEvent(mWorker);
	}

	@Override
	public void onDrawFrame(GL10 unused) {
		GLES20.glClearColor(0f, 0f, 0f, 0f);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT
				| GLES20.GL_STENCIL_BUFFER_BIT);

		if (mShaderCompilerSupport[0] == false) {
			return;
		}

		GLES20.glDisable(GLES20.GL_DEPTH_TEST);
		GLES20.glDisable(GLES20.GL_CULL_FACE);
		GLES20.glDisable(GLES20.GL_BLEND);

		// Render emitter movement lines.
		{
			mShaderEmitter.useProgram();
			int uProjectionM = mShaderEmitter.getHandle("uProjectionM");
			int uEmitterCtrl0 = mShaderEmitter.getHandle("uEmitterCtrl0");
			int uEmitterCtrl1 = mShaderEmitter.getHandle("uEmitterCtrl1");
			int uEmitterCtrl2 = mShaderEmitter.getHandle("uEmitterCtrl2");
			int aPosition = mShaderEmitter.getHandle("aPosition");

			GLES20.glUniformMatrix4fv(uProjectionM, 1, false,
					mMatrixProjection, 0);

			GLES20.glUniform2f(uEmitterCtrl0, mEmitterPosCtrl0.x,
					mEmitterPosCtrl0.y);
			GLES20.glUniform2f(uEmitterCtrl1, mEmitterPosCtrl1.x,
					mEmitterPosCtrl1.y);
			GLES20.glUniform2f(uEmitterCtrl2, mEmitterPosCtrl2.x,
					mEmitterPosCtrl2.y);

			GLES20.glVertexAttribPointer(aPosition, 1, GLES20.GL_FLOAT, false,
					0, mBufferLine);
			GLES20.glEnableVertexAttribArray(aPosition);

			GLES20.glLineWidth(7);
			GLES20.glDrawArrays(GLES20.GL_LINES, 0, 20);
		}

		GLES20.glEnable(GLES20.GL_BLEND);
		GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

		// Render particles.
		{
			mShaderParticle.useProgram();
			int uPosition = mShaderParticle.getHandle("uPosition");
			int uProjectionM = mShaderParticle.getHandle("uProjectionM");
			int uColor = mShaderParticle.getHandle("uColor");
			int aPosition = mShaderParticle.getHandle("aPosition");

			GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_BYTE, false,
					0, mBufferQuad);
			GLES20.glEnableVertexAttribArray(aPosition);

			GLES20.glUniformMatrix4fv(uProjectionM, 1, false,
					mMatrixProjection, 0);

			for (int i = 0; i < mParticles.size(); ++i) {
				float col = 1f;
				if (i < 1000) {
					col = i / 1000f;
				}
				GLES20.glUniform3f(uColor, col, col, col);

				Particle p = mParticles.get(i);
				GLES20.glUniform4f(uPosition, p.mPosition[0], p.mPosition[1],
						0f, 0.03f);
				GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
			}
		}

		queueEvent(mWorker);
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		float aspect = (float) width / height;
		Matrix.orthoM(mMatrixProjection, 0, -aspect, aspect, -1, 1, -1, 1);
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
			vertexSource = loadRawString(R.raw.emitter_vs);
			fragmentSource = loadRawString(R.raw.emitter_fs);
			mShaderEmitter.setProgram(vertexSource, fragmentSource);
			vertexSource = loadRawString(R.raw.particle_vs);
			fragmentSource = loadRawString(R.raw.particle_fs);
			mShaderParticle.setProgram(vertexSource, fragmentSource);
		} catch (Exception ex) {
			showError(ex.getMessage());
		}
	}

	/**
	 * Particle information container.
	 */
	private class Particle {
		public float[] mDirection = new float[2];
		public float[] mPosition = new float[2];
		public float mSpeed;
	}

	/**
	 * Worker runnable.
	 */
	private class Worker implements Runnable {

		private long mRenderTime;
		private long mRenderTimeLast;

		@Override
		public void run() {

			// First update emitter position and direction.
			long time = SystemClock.uptimeMillis();
			if (time - mRenderTime > 4000) {
				mEmitterPosCtrl0.set(mEmitterPosCtrl2);
				mEmitterPosCtrl1.x = (float) (Math.random() * 2 - 1);
				mEmitterPosCtrl1.y = (float) (Math.random() * 2 - 1);
				mEmitterPosCtrl2.x = (float) (Math.random() * 2 - 1);
				mEmitterPosCtrl2.y = (float) (Math.random() * 2 - 1);
				mEmitterDirSource = mEmitterDirTarget;
				mEmitterDirTarget = (float) (Math.random() * 720);
				mRenderTime = time;
			}

			float t = (time - mRenderTime) / 4000f;
			t = t * t * (3 - 2 * t);

			mEmitterPos.x = (1 - t) * (1 - t) * mEmitterPosCtrl0.x + 2
					* (1 - t) * t * mEmitterPosCtrl1.x + t * t
					* mEmitterPosCtrl2.x;
			mEmitterPos.y = (1 - t) * (1 - t) * mEmitterPosCtrl0.y + 2
					* (1 - t) * t * mEmitterPosCtrl1.y + t * t
					* mEmitterPosCtrl2.y;
			mEmitterDir = mEmitterDirSource
					+ (mEmitterDirTarget - mEmitterDirSource) * t;

			// Emit particles.
			for (int i = 0; i < 100; ++i) {

				Particle p;
				if (mParticles.size() > 10000) {
					p = mParticles.remove(0);
				} else {
					p = new Particle();
				}

				p.mPosition[0] = mEmitterPos.x;
				p.mPosition[1] = mEmitterPos.y;

				float dir = (float) ((Math.PI * 2 * (mEmitterDir
						+ Math.random() * 40 - 20)) / 360);
				float len = (float) (Math.random() * 0.8 + 0.2);
				p.mDirection[0] = FloatMath.sin(dir) * len;
				p.mDirection[1] = FloatMath.cos(dir) * len;
				p.mSpeed = 0.8f;

				mParticles.add(p);
			}

			// Update particle positions.
			t = (time - mRenderTimeLast) / 1000f;
			for (int i = 0; i < mParticles.size(); ++i) {
				Particle p = mParticles.get(i);

				float dx = p.mPosition[0] - mEmitterPos.x;
				float dy = p.mPosition[1] - mEmitterPos.y;
				float len = FloatMath.sqrt(dx * dx + dy * dy);
				if (len > 0.0f && len < 0.2f) {
					p.mDirection[0] = p.mSpeed * p.mDirection[0]
							+ (1.0f - p.mSpeed) * dx / len;
					p.mDirection[1] = p.mSpeed * p.mDirection[1]
							+ (1.0f - p.mSpeed) * dy / len;
					p.mSpeed += 0.2f - len;
					if (p.mSpeed > 0.8f) {
						p.mSpeed = 0.8f;
					}
				}

				p.mPosition[0] += p.mDirection[0] * p.mSpeed * t;
				p.mPosition[1] += p.mDirection[1] * p.mSpeed * t;
				p.mSpeed *= 1.0 - t;

			}
			mRenderTimeLast = time;

			requestRender();
		}

	}

}
