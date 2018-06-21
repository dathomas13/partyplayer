package de.gubo_io.partyplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import static de.gubo_io.partyplayer.ActivityDataSource.QRcodeWidth;


public class CreateGroup extends AppCompatActivity {

    ImageView imageView;
    Button button;
    String EditTextValue ;
    Bitmap bitmap ;
    Thread thread ;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        imageView = (ImageView)findViewById(R.id.image);
        button = (Button)findViewById(R.id.btnGenerateQR);
        final TextView textView = (TextView)findViewById(R.id.textView);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //new ActivityDataSource(textView, imageView, context).execute();
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
                Log.e("adss", "asdf");
                networkUtils.createGroup(CreateGroup.this);

                EditTextValue = textView.getText().toString();



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
                        context.getResources().getColor(R.color.QRCodeBlackColor):context.getResources().getColor(R.color.QRCodeWhiteColor);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }
   }
