package com.example.miraj.scannerapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.getStructuringElement;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 99;
    private Button scanButton;
    private Button cameraButton;
    private Button mediaButton;
    private ImageView scannedImageView;

    private MediaPlayer mp;
    int musicsample[] = {R.raw.a5, R.raw.g5, R.raw.f5, R.raw.e5, R.raw.d5, R.raw.c5,
            R.raw.b4, R.raw.a4, R.raw.g4, R.raw.f4, R.raw.e4, R.raw.d4, R.raw.c4, R.raw.b3, R.raw.a3, R.raw.g3};
    List<Integer> music = new ArrayList<>();
    List<Integer> time = new ArrayList<>();
    int l;

    private static int RESULT_LOAD_IMAGE = 1;
    Bitmap forhalfnote;

    private static final String TAG = "MainActivity";

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV not loaded");
        } else {
            Log.d(TAG, "OpenCV loaded");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        Button play = (Button) findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                l = music.size();
                playmusic(0);
            }
        });
    }

    private void init() {
        scanButton = (Button) findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new ScanButtonClickListener());
        cameraButton = (Button) findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new ScanButtonClickListener(ScanConstants.OPEN_CAMERA));
        mediaButton = (Button) findViewById(R.id.mediaButton);
        mediaButton.setOnClickListener(new ScanButtonClickListener(ScanConstants.OPEN_MEDIA));
        scannedImageView = (ImageView) findViewById(R.id.scannedImage);
    }

    private class ScanButtonClickListener implements View.OnClickListener {

        private int preference = ScanConstants.OPEN_CAMERA;

        public ScanButtonClickListener(int preference) {
            this.preference = preference;
        }

        public ScanButtonClickListener() {
        }

        @Override
        public void onClick(View v) {
            startScan(preference);
        }
    }

    protected void startScan(int preference) {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
            Bitmap bitmap = null;
            TextView t = (TextView)findViewById(R.id.textView);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                t.append(bitmap.getWidth()+" "+bitmap.getHeight()+" ");
                bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth()*2/5, bitmap.getHeight()*2/5, true);
                Bitmap b = Bitmap.createBitmap(bitmap, 10, 10, bitmap.getWidth()-20, bitmap.getHeight()-20);
                getContentResolver().delete(uri, null, null);
                t.append(bitmap.getWidth()+ " "+bitmap.getHeight()+" "+b.getWidth()+ " "+b.getHeight()+"\n");
                //scannedImageView.setImageBitmap(b);

                Bitmap b1 = b.copy(b.getConfig(), true);
                //ImageView imageView = (ImageView) findViewById(R.id.imgView);
                //imageView.setImageBitmap(b);
                Mat mat = new Mat(b.getWidth(), b.getHeight(), CvType.CV_8UC1);
                Utils.bitmapToMat(b, mat);

//                Mat mat = imread(picturePath);
                Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
//                Imgproc.GaussianBlur(mat, mat, new Size(3,3), 2);

                Imgproc.adaptiveThreshold(mat,mat,255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY,15,10);
//                Imgproc.GaussianBlur(mat, mat, new Size(3,3), 2);
                b1 = test(b1);
                Core.bitwise_not(mat,mat);
//                Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5,5));
//                Imgproc.morphologyEx(mat, mat, Imgproc.MORPH_CLOSE, kernel);

                Mat kernel1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2));
                Imgproc.morphologyEx(mat, mat, Imgproc.MORPH_CLOSE, kernel1);
                Core.bitwise_not(mat,mat);



                Utils.matToBitmap(mat,b1);
//                scannedImageView.setImageBitmap(b1);

//                Utils.matToBitmap(mat, b1);
//                b1 = test(b1);
                rle(b1);
                Bitmap newphoto = removestave(b1);
                Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5,5));
                Imgproc.morphologyEx(mat, mat, Imgproc.MORPH_CLOSE, kernel);
                stanzacut(newphoto);
                forhalfnote = newphoto;

//
//                Mat afg = new Mat();
//                Utils.bitmapToMat(newphoto,afg);
//                Imgproc.GaussianBlur(afg, afg, new Size(3,1), 2);
//                Bitmap newphoto3 = newphoto.copy(newphoto.getConfig(),true);
//                Utils.matToBitmap(afg,newphoto3);
//
                Bitmap newphoto1 = boundingbox(newphoto);
                scannedImageView.setImageBitmap(newphoto1);

                //////seeing whats gaussian blur detects//////////////////////////////
//                Bitmap newphoto2 = boundingbox(newphoto3);
//                scannedImageView.setImageBitmap(newphoto2);
                //////////////////////////////////////////////////


//////////////////////////////////////////////////////////////////////////////////////////template
//            Bitmap cropped = Bitmap.createBitmap(newphoto, 108, 28, 9, 25);
//            Mat img = new Mat();
//            Utils.bitmapToMat(cropped,img);
//            Bitmap x = BitmapFactory.decodeResource(getResources(),R.drawable.halft);
////            Mat otsu = new Mat();
////            Utils.bitmapToMat(x,otsu);
////            Imgproc.cvtColor(otsu, otsu, Imgproc.COLOR_RGB2GRAY);
////            Imgproc.threshold(otsu, otsu, -1, 255, Imgproc.THRESH_OTSU);
////            Utils.matToBitmap(otsu, x);
//            x = test(x);
//            int scaledheight = x.getHeight()*9/x.getWidth();
//            Bitmap x1 = Bitmap.createScaledBitmap(x, 9, scaledheight, true);
////            imageView.setImageBitmap(x1);
////            imageView1.setImageBitmap(cropped);
//            Mat templ = new Mat();
//            Utils.bitmapToMat(x1,templ);

//
//            int result_cols = img.cols() - templ.cols() + 1;
//            int result_rows = img.rows() - templ.rows() + 1;
//            Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);
//            t.append(" "+result_rows+"\n");
//            // / Do the Matching and Normalize
//            Imgproc.matchTemplate(img, templ, result, Imgproc.TM_SQDIFF_NORMED);
//            //Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
//
//            // / Localizing the best match with minMaxLoc
//            Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
//            Point matchLoc = mmr.minLoc;
//            double max = mmr.minVal;
//            t.append(" "+ max+" "+matchLoc.x+" "+matchLoc.y);
//////////////////////////////////////////////////////////////////////////////////////////////////////



                for (int stanzano = 0; stanzano < musicsheet.size(); stanzano++) {
                    ArrayList<ArrayList<Integer>> stanza = musicsheet.get(stanzano);//list of lists
                    for (int group = 1; group < stanza.size(); group++) {
                        ArrayList<Integer> symbol = stanza.get(group);
                        int p = symbol.get(0) - 1, q = symbol.get(1) - 1, r = symbol.get(2) - symbol.get(0) + 3, s = symbol.get(3) - symbol.get(1) + 3;
                        Bitmap x = Bitmap.createBitmap(forhalfnote, p, q, r, s);

                        pitchdetect(x, p + 1, q + 1, r + p - 2, q + s - 2, stanzano);

                    }
                    t.append("\n");
                }

            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap convertByteArrayToBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    List<Integer> blackstavelist = new ArrayList<Integer>();
    List<Integer> blackstavelist1 = new ArrayList<Integer>();
    List<Double> lofmiddle = new ArrayList<Double>();
    double blackthickness, whitethickness;


    public void rle(Bitmap src) {
        TextView t = (TextView) findViewById(R.id.textView);
        int width = src.getWidth();
        int height = src.getHeight();
        int rle[] = new int[height];
        int pixel, R;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixel = src.getPixel(x, y);
                R = Color.red(pixel);
                if (R == 0)
                    rle[y]++;
            }
        }
        int flag = 0, c = 0, th = 0, pos = 0, reqpos = 0;
//        double mean=getMean(rle);
//        double stdev=getStdDev(rle);
//        double thres=mean+stdev;
//        for(int i=0;i<height;i++) {
//
//            if (rle[i] > thres) {
//                rle[i] = 1;//1 means black
//                blackstavelist.add(i);
//                if (flag==0)
//                    c++;
//                if (c==5)
//                    reqpos=i;
//                if (c==3 && flag == 0)
//                    pos = i ;
//                if (c==3)   ///////////////////////     only for one stanza
//                    th++;
//                flag=1;
//            }
//            else {
//                rle[i] = 0;
//                flag=0;
//            }
//        }
        for (int i = 0; i < height; i++) {
            float ratio = (float) rle[i] / width;
//            t.append(" "+ratio);
            if (ratio >= 0.2)
                blackstavelist1.add(i);
            if (ratio >= 0.4) {
                rle[i] = 1;//1 means black
                blackstavelist.add(i);
//                t.append(" "+i);
                if (flag == 0)
                    c++;
                if (c == 5)
                    reqpos = i;

                if (c % 5 == 3 && flag == 0)
                    pos = i;
                if (c % 5 == 3)   ///////////////////////     only for one stanza
                    th++;
                flag = 1;
            } else {
                if (c % 5 == 3 && flag == 1) {
                    double middle = pos + (th - 1) / 2.0;
                    lofmiddle.add(middle);
                    t.append(middle + " ");
                }
                rle[i] = 0;
                flag = 0;
            }
        }
//        double middle=pos+(th-1)/2.0;
//        lofmiddle.add(middle);
        int l = blackstavelist.size();
        blackthickness = l * 1.0 / c;
        whitethickness = (reqpos - blackstavelist.get(0) - 5.0 * blackthickness + 1) / 4.0;
/////////////////////////////////////////////printing part(debugging)
//        t.append("\n");
//        for(int i=0;i<height;i++){
//
//           t.append(rle[i]+" ");
//        }
//        t.append("\n");
//        for(int i=0;i<blackstavelist.size();i++){
//            t.append(" "+blackstavelist.get(i));
//        }
        t.append("\n" + blackthickness + " " + whitethickness + "\n");
/////////////////////////////////////////////////////////////////
    }

    public Bitmap removestave(Bitmap src)
    {
//        int width = src.getWidth();
//        int height = src.getHeight();
//        Bitmap bmOut = src.copy(src.getConfig(), true);
//        int l = blackstavelist1.size();
//        for (int y = 0; y < l; y++) {
//            for (int x = 0; x < width; x++) {
//                int Y = blackstavelist1.get(y);
//                int pixel = bmOut.getPixel(x, Y - 1);
//                int abovepix = Color.red(pixel);
//                int A = Color.alpha(pixel);
//
////                if (y != l-1  && Y+1!=blackstavelist1.get(y+1)) {//only 1 pixel thick line so look below also
////                    if(Color.red(bmOut.getPixel(x,Y+1))==255)
////                        bmOut.setPixel(x, Y, Color.argb(A, abovepix, abovepix, abovepix));
////                }
////                else
//                    bmOut.setPixel(x, Y, Color.argb(A, abovepix, abovepix, abovepix));
//
////                if (abovepix == 255)
////                    bmOut.setPixel(x, Y, Color.argb(A, abovepix, abovepix, abovepix));
////                bmOut.setPixel(x, Y, Color.argb(A, 255, 255, 255));
//
//            }
//        }

        ////////////////////////////////////////////////////// by erosion
        Bitmap bmOut = src.copy(src.getConfig(),true);
        Mat m = new Mat();
        Utils.bitmapToMat(src,m);
        Mat element = getStructuringElement(MORPH_RECT, new Size(1, blackthickness)) ;
        Mat element1 = getStructuringElement(MORPH_RECT, new Size(1, 2)) ;
//        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(4,4));
//        Imgproc.morphologyEx(m, m, Imgproc.MORPH_CLOSE, kernel);

        Imgproc.dilate(m,m,element1);

        Imgproc.dilate(m,m,element);
        Imgproc.dilate(m,m,element1);
        Utils.matToBitmap(m,bmOut);
        /////////////////////////////////////////////////////////////

        return bmOut;
    }

    List<Integer> stanzastart = new ArrayList<Integer>();
    List<Integer> stanzaend = new ArrayList<Integer>();

    public void stanzacut(Bitmap src) { /// these functions work on a image without staff lines
        int flag = 0;
        int height = src.getHeight();
        int width = src.getWidth();
        //int rle[]=new int[height];
        int pixel, R;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixel = src.getPixel(x, y);
                R = Color.red(pixel);
                if (R == 0) {
                    if (flag == 0)
                        stanzastart.add(y);
                    flag = 1;
                    break;
                } else if (R == 255 && x != width - 1)
                    continue;
                else if (x == width - 1 && flag == 1) {
                    stanzaend.add(y - 1);
                    flag = 0;
                }
            }
        }
    }


    ArrayList<ArrayList<ArrayList<Integer>>> musicsheet = new ArrayList<ArrayList<ArrayList<Integer>>>();

    public Bitmap boundingbox(Bitmap src) {

        TextView t = (TextView) findViewById(R.id.textView);
        t.setBackgroundColor(Color.WHITE);
        t.setMovementMethod(new ScrollingMovementMethod());
        Bitmap bmout = src.copy(src.getConfig(), true);
        int alpha = Color.alpha(src.getPixel(1, 1));//arbitrary

        int len = stanzastart.size();
        int width = src.getWidth();
        for (int i = 0; i < len; i++) {
            int start = stanzastart.get(i);
            int end = stanzaend.get(i);
            ArrayList<ArrayList<Integer>> stanza = new ArrayList<ArrayList<Integer>>();
            int f1 = 0, xmin = width, xmax = -1, ymin = end + 1, ymax = -1, gap = 0;

            for (int x = 0; x < width; x++) {
                int f2 = 0;
                for (int y = start; y <= end; y++) {
                    int pixel = src.getPixel(x, y);
                    int R = Color.red(pixel);
                    if (R == 0) {
                        if (x < xmin) xmin = x;
                        if (x > xmax) xmax = x;
                        if (y < ymin) ymin = y;
                        if (y > ymax) ymax = y;
                        f1 = 1;
                        f2 = 1;
                    }
                    if (y == end && f1 == 1 && f2 == 0) {
                        gap++;
                        if (gap == 10) {
                            if (xmax - xmin >= whitethickness) {
                                ArrayList<Integer> symbol = new ArrayList<>();
                                symbol.add(xmin);
                                symbol.add(ymin);
                                symbol.add(xmax);
                                symbol.add(ymax);
                                stanza.add(symbol);

                                //t.append("\n "+xmin+" "+ymin+" "+xmax+" "+ymax);
                                //////////////////////////////////////////////////////building a box around each symbol
                                for (int w = xmin; w <= xmax; w++) {
                                    bmout.setPixel(w, ymin-1, Color.argb(alpha, 255, 0, 0));
                                    bmout.setPixel(w, ymax+1, Color.argb(alpha, 255, 0, 0));
                                }
                                for (int w = ymin; w <= ymax; w++) {
                                    bmout.setPixel(xmin-1, w, Color.argb(alpha, 255, 0, 0));
                                    bmout.setPixel(xmax+1, w, Color.argb(alpha, 255, 0, 0));
                                }
                            }
                            xmin = width;
                            xmax = -1;
                            ymin = end + 1;
                            ymax = -1;
                            f1 = 0;
                        }
                    } else if ((y == end) && f1 == 1)
                        gap = 0;
                }
            }
                musicsheet.add(stanza);
        }

        return bmout;
    }

    public void pitchdetect(Bitmap src, int p, int q, int r, int s, int midno){
        int n1;
        double n;
        double middle = lofmiddle.get(midno);
        double temp = q + whitethickness/2 - middle -1;
        TextView t = (TextView) findViewById(R.id.textView);
        int width = src.getWidth(),height = (int)whitethickness+1;
        if (s-q <= whitethickness){
            n = temp*2.0/(whitethickness + blackthickness);
            t.append(" w"+Math.round(n));
            n1=(int)Math.round(n);
            music.add(musicsample[n1+6]);
            time.add(2000);
            return;
        }

        int black = 0;
        for (int x = 0;x < width ; x++){
            for (int y=0 ; y<height ; y++){
                int color = Color.red(src.getPixel(x,y));
                if (color == 0) {
                    black++;
                }
            }
        }
        double ratio = black*1.0/(height*width);
        if (ratio < 0.2){
            n = temp*2.0/(whitethickness + blackthickness);
            t.append(" h" + Math.round(n));
            n1 = (int) Math.round(n);
            music.add(musicsample[n1+6]);
            time.add(1000);
        }
        else {
            n = temp*2.0/(whitethickness + blackthickness);
            t.append(" f"+Math.round(n));
            n1 = (int) Math.round(n);
            music.add(musicsample[n1+6]);
            time.add(500);
        }
    }





    public void boundingboxgauss(Bitmap src, int p, int q, int r, int s, int midno) {
        double n;
        int n1,error=0;
        TextView t = (TextView) findViewById(R.id.textView);
        // Bitmap bmout = src.copy(src.getConfig(),true);
        //int alpha=Color.alpha(src.getPixel(1,1));
        int f1 = 0, xmin = src.getWidth(), xmax = -1, ymin = src.getHeight(), ymax = -1;
        int start = 0, end = src.getHeight(), width = src.getWidth();

//        if (midno==3) {
//            t.append((s - q) + " ");
//            n = (q + whitethickness / 2 - lofmiddle.get(midno)) * 2.0 / (whitethickness + blackthickness);
//            t.append("w" + Math.round(n));
//        }


        Bitmap bmO = src.copy(src.getConfig(),true);

        if (r-p > 3*whitethickness){  ////////////////////connected notes


            Mat ma = new Mat();
            Utils.bitmapToMat(bmO,ma);
            Mat elem = getStructuringElement(MORPH_RECT, new Size(1,blackthickness+1)) ;
            Imgproc.dilate(ma,ma,elem);
            Utils.matToBitmap(ma,bmO);

//            imageView1.setImageBitmap(bmO);


            Mat afgaus = new Mat();
            Utils.bitmapToMat(bmO, afgaus);
            Imgproc.GaussianBlur(afgaus, afgaus, new Size(3, 1), 2);
            Utils.matToBitmap(afgaus, bmO);



            for (int x = 0; x < width; x++) {
                int f2 = 0;
                for (int y = start; y < end; y++) {
                    int pixel = src.getPixel(x, y);
                    int R = Color.red(pixel);
                    if (R == 0) {
                        if (x < xmin) xmin = x;
                        if (x > xmax) xmax = x;
                        if (y < ymin) ymin = y;
                        if (y > ymax) ymax = y;
                        f1 = 1;
                        f2 = 1;
                    }
                    if (y == end - 1 && f1 == 1 && f2 == 0) {

                        if (s-q < 2*whitethickness) {
                            error = 1;
                            break;
                        }
//                    if (xmax - xmin >= whitethickness) {
//
//                        //t.append("\n "+xmin+" "+ymin+" "+xmax+" "+ymax);
//                        //////////////////////////////////////////////////////building a box around each symbol
//                        for (int w = xmin; w <= xmax; w++) {
//                            bmout.setPixel(w, ymin, Color.argb(alpha, 255, 0, 0));
//                            bmout.setPixel(w, ymax, Color.argb(alpha, 255, 0, 0));
//                        }
//                        for (int w = ymin; w <= ymax; w++) {
//                            bmout.setPixel(xmin, w, Color.argb(alpha, 255, 0, 0));
//                            bmout.setPixel(xmax, w, Color.argb(alpha, 255, 0, 0));
//                        }
//                        ///////////////////////////////////////////////////////////////
//                    }
                        int g1 = (xmin + xmax)/2, g2 = (ymin + ymax)/2;
                        //t.append(" "+ g1 +" " +g2 + "\n");
                        xmin = width;
                        xmax = -1;
                        ymin = end + 1;
                        ymax = -1;
                        f1 = 0;

                        /////////////////////////////////////////////pitch detection
//                    double n;
//                    if (g2 < (s-q)/2)
//                        n = (q+whitethickness/2-lofmiddle.get(midno))*2/(whitethickness+blackthickness);
//                    else
//                        n = (s-whitethickness/2-lofmiddle.get(midno))*2/(whitethickness+blackthickness);
                        n = (q + g2 - lofmiddle.get(midno))*2.0/(whitethickness + blackthickness);
                        t.append("c" + Math.round(n));
                        n1 = (int) Math.round(n);
//                        music.add(musicsample[n1+6]);
                        time.add(125);


                    }
                }
                if (error == 1){
                    error = 0;
                    break;
                }
            }
            return;
        }




        Mat afgauss1 = new Mat();
        Utils.bitmapToMat(bmO, afgauss1);
        Imgproc.GaussianBlur(afgauss1, afgauss1, new Size(3, 1), 2);
        Utils.matToBitmap(afgauss1, bmO);
        scannedImageView.setImageBitmap(bmO);

        for (int x = 0; x < width; x++) {
            int f2 = 0;
            for (int y = start; y < end; y++) {
                int pixel = src.getPixel(x, y);
                int R = Color.red(pixel);
                if (R == 0) {
                    if (x < xmin) xmin = x;
                    if (x > xmax) xmax = x;
                    if (y < ymin) ymin = y;
                    if (y > ymax) ymax = y;
                    f1 = 1;
                    f2 = 1;
                }
                if (y == end - 1 && f1 == 1 && f2 == 0) {

                    if (s-q < 2*whitethickness) {
                        error = 1;
                        break;
                    }
                    ////further confirmation coz sometimes half notes also detected as quarter ones
                    int checkfurther=0,j=(p+r)/2;//basically a flag
                    for (int i=q;i<=s+1;i++){
                        //t.append(Color.red(forhalfnote.getPixel(j,i))+" ");
                        if (checkfurther==0 && Color.red(forhalfnote.getPixel(j,i)) == 0)
                            checkfurther =1;
                        if (checkfurther==1 && Color.red(forhalfnote.getPixel(j,i)) == 255)
                            checkfurther=2;
                        if (checkfurther==2 && Color.red(forhalfnote.getPixel(j,i)) == 0) {
                            error = 1;
                            break;
                        }
                    }
                    if (error==1)
                        break;
                    int g1 = (xmin + xmax)/2, g2 = (ymin + ymax)/2;
                    //t.append(" "+ g1 +" " +g2 + "\n");
                    xmin = width;
                    xmax = -1;
                    ymin = end + 1;
                    ymax = -1;
                    f1 = 0;

                    /////////////////////////////////////////////pitch detection
//                    double n;
//                    if (g2 < (s-q)/2)
//                        n = (q+whitethickness/2-lofmiddle.get(midno))*2/(whitethickness+blackthickness);
//                    else
//                        n = (s-whitethickness/2-lofmiddle.get(midno))*2/(whitethickness+blackthickness);
                    n = (q + g2 - lofmiddle.get(midno))*2.0/(whitethickness + blackthickness);
                    t.append("f" + Math.round(n));
                    n1 = (int) Math.round(n);
//                    music.add(musicsample[n1+6]);
                    time.add(250);
                    return;

                }
            }
            if (error == 1){
                error = 0;
                break;
            }
        }
        if (s - q > 2* whitethickness){
            for (int x = p; x < p + src.getWidth(); x++) {
                int cb = 0;
                for (int y = q; y < q + src.getHeight(); y++) {
                    if (Color.red(forhalfnote.getPixel(x, y)) == 0)
                        cb++;
                }
                if (cb > src.getHeight() / 2) {
                    cb = 0;
                    for (int y1 = q; y1 < q + src.getHeight() / 2; y1++) {
                        for (int x1 = p; x1 < p + src.getWidth(); x1++) {
                            if (Color.red(forhalfnote.getPixel(x1, y1)) == 0)
                                cb++;
                        }
                    }
                    int cb1 = 0;
                    for (int y1 = q + src.getHeight() / 2; y1 < q + src.getHeight(); y1++) {
                        for (int x1 = p; x1 < p + src.getWidth(); x1++) {
                            if (Color.red(forhalfnote.getPixel(x1, y1)) == 0)
                                cb1++;
                        }
                    }
                    if (cb > cb1 && r - p > whitethickness / 2) {
                        n = (q + whitethickness / 2 - lofmiddle.get(midno)) * 2.0 / (whitethickness + blackthickness);
                        t.append("h" + Math.round(n));
                        n1 = (int) Math.round(n);
//                        music.add(musicsample[n1+6]);
                        time.add(500);
                    } else if (r - p > whitethickness / 2) {
                        n = (s - whitethickness / 2 - lofmiddle.get(midno)) * 2.0 / (whitethickness + blackthickness);
                        t.append("h" + Math.round(n));
                        n1 = (int) Math.round(n);
//                        music.add(musicsample[n1+6]);
                        time.add(500);
                    }
                    return;
                }


            }
        }
        else if(s-q > 0.8*whitethickness){
            n = (q + whitethickness / 2 - lofmiddle.get(midno)) * 2.0 / (whitethickness + blackthickness);
            t.append("w" + Math.round(n));
            n1 = (int) Math.round(n);
//            music.add(musicsample[n1+6]);
            time.add(1000);
        }

        ////////////////////////////whole note
        //return bmout;
    }

    public void playmusic( final int i ) {
        if (!(i == l)) {
            mp = MediaPlayer.create(MainActivity.this, music.get(i));
            CountDownTimer cntr_aCounter = new CountDownTimer(time.get(i), 25) {
                public void onTick(long millisUntilFinished) {
                    mp.start();
                }

                public void onFinish() {
                    //code fire after finish
                    mp.stop();
                    mp.release();
                    mp = null;
                    int j = i + 1;
                    playmusic(j);
                }
            };
            cntr_aCounter.start();
        }
    }

    double getMean(int data[])
    {
        double sum = 0.0;
        for(double a : data)
            sum += a;
        int size=data.length;
        return sum/size;
    }

    double getVariance(int data[])
    {
        double mean = getMean(data);
        double temp = 0;
        for(double a :data)
            temp += (a-mean)*(a-mean);
        int size=data.length;
        return temp/size;
    }

    double getStdDev(int data[])
    {
        return Math.sqrt(getVariance(data));
    }

    public static Bitmap test(Bitmap src){
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;
        int histData[] = new int[256];
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                int gray = (int) (0.2126 * R + 0.7152 * G + 0.0712 * B);
                // use 128 as threshold, above -> white, below -> black
                histData[gray]++;

            }
        }
        int threshold;
        int total=width*height;
        float sum = 0;
        for (int t=0 ; t<256 ; t++)
            sum += t * histData[t];

        float sumB = 0;
        int wB = 0;
        int wF = 0;

        float varMax = 0;
        threshold = 0;

        for (int t=0 ; t<256 ; t++) {
            wB += histData[t];               // Weight Background
            if (wB == 0) continue;

            wF = total - wB;                 // Weight Foreground
            if (wF == 0) break;

            sumB += (float) (t * histData[t]);

            float mB = sumB / wB;            // Mean Background
            float mF = (sum - sumB) / wF;    // Mean Foreground

            // Calculate Between Class Variance
            float varBetween = (float)wB * (float)wF * (mB - mF) * (mB - mF);

            // Check if new maximum found
            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = t;
            }
        }
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);
                int gray = (int) (0.2126 * R + 0.7152 * G + 0.0712 * B);
                // use 128 as threshold, above -> white, below -> black
                if (gray > threshold) {
                    gray = 255;
                }
                else{
                    gray = 0;
                }// set new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, gray, gray, gray));


            }
        }

        return bmOut;

    }

}
