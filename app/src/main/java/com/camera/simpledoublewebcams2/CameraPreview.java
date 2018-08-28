package com.camera.simpledoublewebcams2;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Runnable {

	private static final boolean DEBUG=true;
	protected Context context;
	private SurfaceHolder holder;
    Thread mainLoop = null;
	private Bitmap[] bmp=new Bitmap[2];
	private boolean[] cameraExists=new boolean[2];
	
	private boolean shouldStop=false;
	
	// Size of each image. This definition also exists in jni/ImageProc.h
	public static final int IMG_WIDTH=640;
	public static final int IMG_HEIGHT=480;
    
	public int winWidth=0;
	public int winHeight=0;
	public Rect rect1, rect2, rect3, rect4;
	
    public native void pixeltobmp(Bitmap bitmap1, Bitmap bitmap2);
    public native void pixeltobmp4(Bitmap bitmap1, Bitmap bitmap2,Bitmap bitmap3,Bitmap bitmap4);
    
    // prepareCamera selects the device automatically. please set videoid=0
    public native int prepareCamera(int videoid);
    // prepareCameraWithBase is used if you want to specify the device manually.
    // e.g., for /dev/video[1,2], use prepareCameraWithBase(0,1).
    // please set videoid=0
    public native int prepareCameraWithBase(int videoid, int videobase);
    public native void processCamera();
    public native void processRBCamera(int lrmode);
    public native void stopCamera();

	static {
    	System.loadLibrary("jpeg");
        System.loadLibrary("ImageProc");
    }
	public CameraPreview(Context context) {
		super(context);
		this.context = context;
		if(DEBUG) Log.d("TEST","CameraPreview constructed");
		setFocusable(true);

		bmp[0] = null;
		bmp[1] = null;
	//	bmp[2] = null;
	//	bmp[3] = null;

		holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);	
	}
    @Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		Log.d("123", "resX:"+event.getX()+"resY:"+event.getY());
		return super.dispatchTouchEvent(event);
	}

    @Override
    public void run() {
        while (true && (cameraExists[0]||cameraExists[1])) {

        	processCamera();
        	//pixeltobmp4(bmp[0],bmp[1],bmp[2],bmp[3]);
			pixeltobmp(bmp[0],bmp[1]);
        	Canvas canvas = getHolder().lockCanvas();
            if (canvas != null)
            {
            	if(winWidth==0){
            		winWidth=this.getWidth();
            		winHeight=this.getHeight();

            		//rect1 = new Rect(0, 0, winWidth/2-1, winWidth*3/4/2-1);
            		//rect2 = new Rect(winWidth/2,0,winWidth-1, winWidth*3/4/2-1);

            		//rect1 = new Rect(0, 0, 						winWidth/2-1, winWidth*3/4/2/2-1);
            		//rect2 = new Rect(winWidth/2,0,				winWidth-1, winWidth*3/4/2/2-1);
            		//rect3 = new Rect(0  , winWidth*3/4/2/2,		winWidth/2-1, winWidth*3/4/2);
            		//rect4 = new Rect(winWidth/2, winWidth*3/4/2/2 ,	winWidth-1,  winWidth*3/4/2-1);
            		
            		Log.d("123", "resX:"+winWidth+"resY:"+winHeight);
            		
            		rect1 = new Rect(0,           0, winWidth/2-1, winHeight-1);
            		rect2 = new Rect(winWidth/2+1,0,  winWidth-1,  winHeight-1);
            		
            	//	rect3 = new Rect(0  , winHeight/2+1,		winWidth/2-1, winHeight-1);
            	//	rect4 = new Rect(winWidth/2+1, winHeight/2+1 ,	winWidth-1,  winHeight-1);
            	}

        		canvas.drawBitmap(bmp[0],null,rect1,null);
        		canvas.drawBitmap(bmp[1],null,rect2,null);
        	//	canvas.drawBitmap(bmp[2],null,rect3,null);
        	//	canvas.drawBitmap(bmp[3],null,rect4,null);

            	getHolder().unlockCanvasAndPost(canvas);
            }
            if(shouldStop){
            	shouldStop = false;  
            	break;
            }

        }
    }

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if(DEBUG) Log.d("TEST", "surfaceCreated");
		for(int i=0 ; i<2 ; i++){
			if(bmp[i]==null){
				bmp[i] = Bitmap.createBitmap(IMG_WIDTH, IMG_HEIGHT, Bitmap.Config.ARGB_8888);
			}
		}

		// /dev/video[0,1] are used.
		// In some omap devices, /dev/video[0..3] are used by system,
		// and, in such a case, /dev/video[4,5] are selected automatically.
		int ret = prepareCamera(0);
		
		// ret: -3(/dev/video[01] do not exist)
		//      -2(/dev/video1 does not exist)
		//      -1(/dev/video0 does not exist)
		ret = -ret;
	/*	if((ret & 0x8)>>1==1){
			cameraExists[3] = false;
		}else{
			cameraExists[3] = true;
		}
		if((ret & 0x4)>>1==1){
			cameraExists[2] = false;
		}else{
			cameraExists[2] = true;
		} */
		if((ret & 0x2)>>1==1){
			cameraExists[1] = false;
		}else{
			cameraExists[1] = true;
		}
		if((ret & 0x01)==1){
			cameraExists[0] = false;
		}else{
			cameraExists[0] = true;
		}
		
        mainLoop = new Thread(this);
        mainLoop.start();		
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if(DEBUG) Log.d("TEST", "surfaceChanged");
	}
	

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if(DEBUG) Log.d("TEST", "surfaceDestroyed");
		if(cameraExists[0]||cameraExists[1]){
			shouldStop = true;
			for(int i=0 ; i<10 ; i++){
				try{ 
					Thread.sleep(100); // wait for thread stopping
				}catch(Exception e){}
				if(!shouldStop){
					break;
				}
			}
			stopCamera();
		}
	}
}
