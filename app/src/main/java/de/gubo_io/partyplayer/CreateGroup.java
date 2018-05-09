package de.gubo_io.partyplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

public class CreateGroup extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        Button mCreateQRButton = findViewById(R.id.btnGenerateQR);
        ImageView image = (ImageView) findViewById(R.id.image);
        mCreateQRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                try {
                    BitMatrix bitMatrix = multiFormatWriter.encode("Party!", BarcodeFormat.QR_CODE, 200,200);
                }
                catch (com.google.zxing.WriterException e){

                    e.getMessage();

                }

            }
        });
    }
}
