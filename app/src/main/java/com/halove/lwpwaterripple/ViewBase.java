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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.widget.Toast;

abstract class ViewBase extends GLSurfaceView implements GLSurfaceView.Renderer {

	public ViewBase(Context context) {
		super(context);
	}

	/**
	 * Loads String from raw resources with given id.
	 */
	protected String loadRawString(int rawId) throws Exception {
		InputStream is = getContext().getResources().openRawResource(rawId);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int len;
		while ((len = is.read(buf)) != -1) {
			baos.write(buf, 0, len);
		}
		return baos.toString();
	}

	/**
	 * Shows Toast on screen with given message.
	 */
	protected void showError(final String errorMsg) {
		post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG)
						.show();
			}
		});
	}

}
