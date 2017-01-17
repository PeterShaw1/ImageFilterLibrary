package com.yuneec.imagefilterlibrary;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int REQUEST_PICK_IMAGE = 1;
    private ImageView img1;
    private Button import1;
    private GPUImageFilter mFilter;
    private Bitmap mbitmap = null;
    private SeekBar myseekbar;
    private long current;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        img1=(ImageView)findViewById(R.id.img1);
        myseekbar=(SeekBar)findViewById(R.id.myseekbar);
        myseekbar.setOnSeekBarChangeListener(seekListener);

        import1=(Button) findViewById(R.id.import1);
        findViewById(R.id.original1).setOnClickListener(this);
        findViewById(R.id.xproll1).setOnClickListener(this);
        findViewById(R.id.tonecurve1).setOnClickListener(this);
        findViewById(R.id.lomo1).setOnClickListener(this);
        findViewById(R.id.contrastchange1).setOnClickListener(this);
        findViewById(R.id.hefe1).setOnClickListener(this);
        findViewById(R.id.inkwell1).setOnClickListener(this);
        findViewById(R.id.hudson1).setOnClickListener(this);
        findViewById(R.id.sepiachange1).setOnClickListener(this);
        findViewById(R.id.toaster1).setOnClickListener(this);
        findViewById(R.id.brightness).setOnClickListener(this);
        findViewById(R.id.contrast).setOnClickListener(this);
        findViewById(R.id.saturation).setOnClickListener(this);

        import1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, REQUEST_PICK_IMAGE);
            }
        });
    }
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if(data==null){
            return;//当data为空的时候，不做任何处理
        }
        switch (requestCode) {
            case REQUEST_PICK_IMAGE:
                //获取从相册界面返回的缩略图
                mbitmap = data.getParcelableExtra("data");
                if(mbitmap==null){//如果返回的图片不够大，就不会执行缩略图的代码，因此需要判断是否为null,如果是小图，直接显示原图即可
                    try {
                        //通过URI得到输入流
                        InputStream inputStream = getContentResolver().openInputStream(data.getData());
                        //通过输入流得到bitmap对象
                        mbitmap = BitmapFactory.decodeStream(inputStream);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
        //将选择的图片设置到控件上
        img1.setImageBitmap(mbitmap);
    }
    @Override
    public void onClick(final View v) {

        current=System.currentTimeMillis();
//        int w=mbitmap.getWidth(),h=mbitmap.getHeight();
//        int[] pix=new int[w*h];
//        mbitmap.getPixels(pix,0,w,0,0,w,h);

        switch (v.getId()) {
            case R.id.original1:
                img1.setImageBitmap(mbitmap);
                break;
            //经典xproll
            case R.id.xproll1:
                mFilter=new IFXprollFilter(this);;
                getBitmapWithFilter(mFilter);
                break;
            //光影tonecurve
            case R.id.tonecurve1:
                GPUImageToneCurveFilter toneCurveFilter = new GPUImageToneCurveFilter();
                toneCurveFilter.setFromCurveFileInputStream(
                        this.getResources().openRawResource(R.raw.tone_cuver_sample));
                getBitmapWithFilter(toneCurveFilter);
                break;
            //清新lomo
            case R.id.lomo1:
                PointF centerPoint = new PointF();
                centerPoint.x = 0.5f;
                centerPoint.y = 0.5f;
                mFilter=new GPUImageVignetteFilter(centerPoint, new float[] {0.0f, 0.0f, 0.0f}, 0.3f, 0.75f);;
                getBitmapWithFilter(mFilter);
                break;
            //艳丽contrastchange
            case R.id.contrastchange1:
                mFilter=new GPUImageContrastFilter(2.0f);
                getBitmapWithFilter(mFilter);
                break;
            //柔光hefe
            case R.id.hefe1:
                mFilter=new IFHefeFilter(this);
                getBitmapWithFilter(mFilter);
                break;
            //黑白Inkwell
            case R.id.inkwell1:
                mFilter=new IFInkwellFilter(this);;
                getBitmapWithFilter(mFilter);
                break;
            //蓝调hudson
            case R.id.hudson1:
                mFilter=new IFHudsonFilter(this);;
                getBitmapWithFilter(mFilter);
                break;
            //复古sepiachange
            case R.id.sepiachange1:
                mFilter=new GPUImageSepiaFilter();;
                getBitmapWithFilter(mFilter);
                break;
            //暗角toaster
            case R.id.toaster1:
                mFilter=new IFToasterFilter(this);;
                getBitmapWithFilter(mFilter);
                break;
            //亮度调整
            case R.id.brightness:
                mFilter=new GPUImageBrightnessFilter(0.5f);
                getBitmapWithFilter(mFilter);
                break;
            //对比度调整
            case R.id.contrast:
                mFilter=new GPUImageContrastFilter(2.0f);
                getBitmapWithFilter(mFilter);
                break;
            //饱和度调整
            case R.id.saturation:
                mFilter=new GPUImageSaturationFilter(1.0f);
                getBitmapWithFilter(mFilter);
                break;
            default:
                break;
        }
    }
    private void getBitmapWithFilter(GPUImageFilter mFilter) {
        GPUImageRenderer renderer = new GPUImageRenderer(mFilter);
        PixelBuffer buffer = new PixelBuffer(mbitmap.getWidth(), mbitmap.getHeight());
        buffer.setRenderer(renderer);
        renderer.setImageBitmap(mbitmap, false);
        long t0=System.currentTimeMillis();
        Bitmap result = buffer.getBitmap();
        mFilter.destroy();
        renderer.deleteImage();
        buffer.destroy();
        img1.setImageBitmap(result);
        long performance=System.currentTimeMillis()-current;
        MainActivity.this.setTitle("Time is:"+String.valueOf(performance)+"毫秒");
//        //保存图片
//        File f = new File("/storage/emulated/0/Pictures/"+ System.currentTimeMillis() +".jpg");
//        Log.e("aaa", "baocuntupian");
//        FileOutputStream fOut = null;
//        try {
//            fOut = new FileOutputStream(f);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        result.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
//        try {
//            fOut.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            fOut.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        //
    }
    private SeekBar.OnSeekBarChangeListener seekListener=new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            //暗角调整
//            float mprogress=(float)i/100.0f;
//            PointF centerPoint = new PointF();
//            centerPoint.x = 0.5f;
//            centerPoint.y = 0.5f;
//            mFilter=new GPUImageVignetteFilter(centerPoint, new float[] {0.0f, 0.0f, 0.0f}, mprogress, 0.75f);;
//            getBitmapWithFilter(mFilter);
            //亮度调整
//            float mprogress=(float)i/50.0f-1.0f;
//            mFilter=new GPUImageBrightnessFilter(mprogress);
//            getBitmapWithFilter(mFilter);
            //对比度调整
//            float mprogress=(float)i/50.0f;
//            mFilter=new GPUImageContrastFilter(mprogress);
//            getBitmapWithFilter(mFilter);
            //饱和度调整
            float mprogress=(float)i/50.0f;
            mFilter = new GPUImageSaturationFilter(mprogress);
            getBitmapWithFilter(mFilter);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };
}
