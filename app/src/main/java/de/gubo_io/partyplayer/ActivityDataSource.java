package de.gubo_io.partyplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

public class ActivityDataSource extends AsyncTask<String, Void, String> {
    public static final String AUTHKEY = "test321";
    public static final String POST_PARAM_KEYVALUE_SEPARATOR = "=";
    public static final String POST_PARAM_SEPARATOR = "&";
    private static final String DESTINATION_METHOD = "allEntrys";
    private TextView textView;
    private ImageView imageView;
    Bitmap bitmap ;
    public final static int QRcodeWidth = 500 ;
    Context context;

    private  URLConnection conn;
    public ActivityDataSource(TextView textView, ImageView imageView, Context context) {
        this.context = context;
        this.textView = textView;
        this.imageView = imageView;
    }
    @Override
    protected String doInBackground(String... params) {
        Log.e("ADS", "doInBG");

            String result;
        NetworkUtils networkUtils = new NetworkUtils();
        networkUtils.setOnGroupIdRecievedListener(new NetworkUtils.OnGroupIdRecievedListener() {
            @Override
            public void onGroupIdRecieved(int groupId) {
                try {
                    Log.e("ADS", ""+groupId);
                    bitmap = TextToImageEncode(groupId + "");
                }
                catch (WriterException w){
                    w.getMessage();
                }
            }
        });
        networkUtils.createGroup(this.context);


            return "";


    }

    private String readResult()throws IOException{
        String result = null;
//Lesen der Rückgabewerte vom Server
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line = null;
//Solange Daten bereitstehen werden diese gelesen.
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
    @Override
    protected void onPostExecute(String result) {
        if(!isBlank(result)) {
//String[] arr = result.split("\\|");
            this.textView.setText(result);
            this.imageView.setImageBitmap(bitmap);
        }
    }
    private boolean isBlank(String value){
        return value == null || value.trim().isEmpty();
    }
    Bitmap TextToImageEncode(String Value) throws com.google.zxing.WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.DATA_MATRIX.QR_CODE,
                    QRcodeWidth, QRcodeWidth, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        context.getResources().getColor(R.color.QRCodeBlackColor):context.getResources().getColor(R.color.QRCodeWhiteColor);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }
}