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

public class CreateGroup extends AppCompatActivity {

    ImageView imageView;
    Button button;
    String EditTextValue ;
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
                new ActivityDataSource(textView, imageView, context).execute("Hello");



                EditTextValue = textView.getText().toString();



            }
        });
    }
   }
