package com.camera.simpledoublewebcams2;

import android.app.Activity;
import android.os.Bundle;

public class Main extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(new CameraPreview(this));
	}
}
