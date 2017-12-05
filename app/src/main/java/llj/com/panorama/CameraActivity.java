package llj.com.panorama;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import llj.com.panorama.util.GenerFileName;

/*
 * 1.首先调用Camera的open（）方法打开摄像机...
 *   private Camera camera;
 *   camera=Camera.open(0);
 * 2.调用Camera.setParameters()方法获取摄像机的参数...
 *   Parameters param=camera.setParameters();
 * 3.设置摄像机的拍照参数来配置拍照信息...
 *   param.setPreviewSize(display.getWidth(),display.getHeight());设置预览大小..
 *   param.setPreviewFrameRate(4)..以每秒四帧显示图像信息...
 *   param.setPictureFormat(PixelFormat.JPEG);设置图片的格式...
 *   param.set("jpeg-quality",85);设置图片的质量，最高为100...
 *   parameters.setPictureSize(screenWidth,screenHeight);设置照片的大小...
 * 4.param.setParamters(param);将参数传递给相机，使相机可以指定相应的参数来完成拍摄...
 * 5.使用setPreview(SurfaceView)设置使用哪个SurfaceView来显示要预览的景象...
 *   MainActivity.this.cma.setPreView(SurfaceHolder holder)...防止主线程阻塞..因此另外开启线程...
 *   MainActivity.this.cma.startPreView();开始预览...
 *   MainActivity.this.cma.autoFocus(afcb);
 * 6.进行拍照，然后获取拍到的图片进行保存...
 *   cma.takePicture(sc,pc,jpgcall);获取图片...
 * 7.结束预览释放资源...
 *   cma.stopPreView();
 *   cma.release();释放资源..
 * 8.在AndroidManifest设置权限...
 *   <uses-feature android:name="android.hardware.camera" />
 *   <uses-feature android:name="android.hardware.camera.autofocus"/>
 *   <uses-permission android:name="android.permission.CAMERA"/>
 *   <uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS"/>
 *   <uses-permission android:name="android.permission.WRITE_EXTENAL_STORAGE"/>
 * */
public class CameraActivity extends AppCompatActivity implements View.OnClickListener {
    private boolean previewrunning = true;
    private Camera camera = null;
    private SurfaceView suf;
    private SurfaceHolder sufh;
    private Activity activity;
    private ImageView iv_pic;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        super.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//全屏显示...
        super.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//高亮显示...
        setContentView(R.layout.activity_camera);
        activity = CameraActivity.this;

        iv_pic = (ImageView) findViewById(R.id.iv_pic);
        suf = (SurfaceView) findViewById(R.id.sView);
        sufh = suf.getHolder();//获取SurfaceHolder...
        sufh.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//设置一个缓冲区...就是一个缓冲的Surface...这个Surface的数据来源于Camera..
        sufh.setFixedSize(480, 800);//设置分辨率为480*800
        findViewById(R.id.take).setOnClickListener(this);
        sufh.addCallback(new SurfaceHolder.Callback() {//这里就很清晰了，在使用到了SurfaceView时必然要获取SurfaceHolder接口，然后进行回调..

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // TODO Auto-generated method stub

                if (CameraActivity.this.camera != null) {
                    if (CameraActivity.this.previewrunning) {
                        CameraActivity.this.camera.stopPreview();
                        CameraActivity.this.previewrunning = false;
                    }
                    CameraActivity.this.camera.release();
                }
            }

            @SuppressLint("NewApi")
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // TODO Auto-generated method stub
                if (Camera.getNumberOfCameras() == 2) {//获取相机...
                    camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                } else {
                    camera = Camera.open(0);
                }
                camera.setDisplayOrientation(90);//这个就是设置屏幕需要旋转90度，一般没这句话屏幕内的东西都是纵向的...

                WindowManager manager = (WindowManager) CameraActivity.this.getSystemService(Context.WINDOW_SERVICE);//获取窗口服务..
                Display display = manager.getDefaultDisplay();//获取display对象..
                Camera.Parameters param = CameraActivity.this.camera.getParameters();//获取参数
                param.setPreviewSize(display.getWidth(), display.getHeight());//设置预览时图片的大小..
                param.setPictureSize(display.getWidth(), display.getHeight());//设置拍照后图片的大小..
                param.setPreviewFrameRate(5);//设置预览的时候以每秒五帧进行显示...
                param.setPictureFormat(PixelFormat.JPEG);//设置图片的格式为JPEG...
                param.set("jpeg-quality", 80);//设置图片的质量...

//                List<Size> listsize=param.getSupportedPreviewSizes();
//                System.out.println(listsize.size());
//                if(null!=listsize && 0<listsize.size()){
//                    int height[]=new int[listsize.size()];
//                    Map<Integer,Integer>map=new HashMap<Integer,Integer>();
//                    for(int i=0;i<listsize.size();i++){
//                        Size size=(Size)listsize.get(i);
//                        int sizeheight=size.height;
//                        int sizewidth=size.width;
//                        height[i]=sizeheight;
//                        map.put(sizeheight, sizewidth);
//                    }
                //Arrays.sort(height);

                // }
//                for(int j=0;j<listsize.size();j++){
//                    System.out.println(listsize.get(j).height+"  "+listsize.get(j).width);
//                }
                /*
                 * 下面就是进行参数的传递，但是出现了一个极大的问题...在我的手机终端上，这句话无法实现...一直会报fail setParamters..
                 * 这个的原因我也查到了很多..
                 * 一种就是我们的相机没有自动对焦功能..这个一般是不太会出现的...
                 * 还有一个原因就是我们设置:
                 * param.setPreviewSize(display.getWidth(), display.getHeight());
                 * param.setPictureSize(display.getWidth(), display.getHeight());这两个方法传递的参数不一样，导致预览图片时的大小
                 * 和拍照后的图片大小不相同导致的...
                 * 还有就是由于手机型号的不同导致我们设置的预览时的分辨率大小和手机的所支持的分辨率大小不匹配导致的...
                 * 这是以上出现fail setParamters的三种原因..但是这三种情况都没有解决我的手机出现的问题...因此我把setParameters()这句话
                 * 给去掉了...
                 * 如果我们不清楚自己手机所支持的分辨率，那么我们就可以按照上面注释的方法..定义一个List进行动态查找，把所有支持的分辨率
                 * 全部都找出来，最后筛选出一个合适的去设置...
                 * */
                // camera.setParameters(param);
                try {
                    CameraActivity.this.camera.setPreviewDisplay(CameraActivity.this.sufh);//设置我们预览时的SurfaceHolder...
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                CameraActivity.this.camera.startPreview();//开始预览...
                CameraActivity.this.previewrunning = true;

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                       int height) {
                // TODO Auto-generated method stub

            }
        });

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (CameraActivity.this.camera != null) {
            CameraActivity.this.camera.autoFocus(new Camera.AutoFocusCallback() {//这里就是触发按钮来完成事件..这里是自动聚焦函数..内部需要实现三种方法..

                private PictureCallback raw = new PictureCallback() {

                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        // TODO Auto-generated method stub
                        //把图片放入sd卡...
                    }
                };
                private PictureCallback jpeg = new PictureCallback() {

                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        // TODO Auto-generated method stub
                        //获取到图片信息的最原始数据，可以对这些原始数据进行相应操作...
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);//把图片转化成字节数据...

                        prePhoto(bmp);

                        //下面获取sd卡的根目录，设置我们需要保存的路径...
                        String filename = Environment.getExternalStorageState().toString() + File.separator + "CameraPhoto" + File.separator + GenerFileName.generFileName() + ".jpg";
                        File file = new File(filename);
                        if (!file.getParentFile().exists()) {//如果父文件夹不存在则进行新建...
                            file.getParentFile().mkdirs();
                        }

                        try {
                            BufferedOutputStream buf = new BufferedOutputStream(new FileOutputStream(file));//以缓冲流的方式将图片的数据进行写入..
                            buf.flush();
                            buf.close();
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        Toast.makeText(CameraActivity.this, "已保存", Toast.LENGTH_SHORT).show();
                        camera.stopPreview();
                        camera.startPreview();

                    }
                };
                private ShutterCallback shutter = new ShutterCallback() {

                    @Override
                    public void onShutter() {
                        // TODO Auto-generated method stub
                        //在按下快门时进行调用..
                    }
                };

                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    // TODO Auto-generated method stub
                    if (success) {
                        CameraActivity.this.camera.takePicture(shutter, raw, jpeg);//takepicture()方法需要有三个参数...这三个参数就是上面定义的三个方法..
                    }
                }
            });
        }
    }

    /**
     * 预览图片
     *
     * @param bmp
     */
    private void prePhoto(Bitmap bmp) {
        iv_pic.setImageBitmap(bmp);
    }

}
