package de.gubo_io.partyplayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;



public class CreateGroup extends AppCompatActivity {

    ImageView imageView;
    Button btnEnterGroup;
    Bitmap bitmap ;
    Context context = this;
    public final static int QRcodeWidth = 500 ;
    int groupId;
    boolean fromMain=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);


        imageView = findViewById(R.id.image);
        btnEnterGroup = findViewById(R.id.btnEnterGroup);
        final TextView textView = findViewById(R.id.textView);
        SharedPreferences sharedPref = getSharedPreferences("playerPref", Context.MODE_PRIVATE);
        groupId =  sharedPref.getInt("groupId", -1);
        if(groupId != -1){
            try {
                fromMain = true;
                btnEnterGroup.setVisibility(View.VISIBLE);
                btnEnterGroup.setText("Zurück zum Player");
                bitmap = TextToImageEncode(groupId + "");
                imageView.setImageBitmap(bitmap);
            }
            catch (WriterException w){
                Log.e("QR-Code", ""+ w.getMessage());
            }
        }

        else {
            NetworkUtils networkUtils = new NetworkUtils();
            networkUtils.setOnGroupIdRecievedListener(new NetworkUtils.OnGroupIdRecievedListener() {
                @Override
                public void onGroupIdRecieved(int _groupId) {
                    try {
                        CreateGroup.this.groupId = _groupId;
                        SharedPreferences sharedPreferences = getSharedPreferences("playerPref", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("groupId", groupId);
                        editor.apply();
                        bitmap = TextToImageEncode(groupId + "");

                        imageView.setImageBitmap(bitmap);
                        btnEnterGroup.setVisibility(View.VISIBLE);
                        btnEnterGroup.setText("Der Gruppe beitreten");
                    }
                    catch (WriterException w){
                        w.getMessage();
                    }
                }
            });
            networkUtils.createGroup(CreateGroup.this);
        }
        btnEnterGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateGroup.this, MainActivity.class);
                if(fromMain == false) {
                    startActivity(intent);
                    Toast.makeText(CreateGroup.this, "Du bist Gruppe " + groupId + " beigetreten", Toast.LENGTH_LONG).show();
                }
                else
                    finish();

            }
        });
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
                        ContextCompat.getColor(context, R.color.QRCodeBlackColor):ContextCompat.getColor(context, R.color.QRCodeWhiteColor);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }
   }
