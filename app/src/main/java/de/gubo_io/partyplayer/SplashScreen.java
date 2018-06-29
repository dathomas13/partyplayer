package de.gubo_io.partyplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class SplashScreen extends AppCompatActivity {
    private Button mJoinGroupButton;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        final EditText editText = findViewById(R.id.txtGroupId);
        editText.setText("1");

        final SharedPreferences sharedPref = getSharedPreferences("playerPref", Context.MODE_PRIVATE);
        int groupId = sharedPref.getInt("groupId", -1);
        if(groupId != -1){
            Intent intent = new Intent(SplashScreen.this, MainActivity.class);
            startActivity(intent);
        }

        Button mCreateGroupButton = findViewById(R.id.btnCreateGroup);
        mCreateGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SplashScreen.this, CreateGroup.class);
                startActivity(intent);
            }
        });
        mJoinGroupButton = findViewById(R.id.btnJoinGroup);
        final Activity activity = this;
        mJoinGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator intentIntegrator = new IntentIntegrator(activity);
                intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                intentIntegrator.setPrompt("");
                intentIntegrator.setCameraId(0);
                intentIntegrator.setBeepEnabled(false);
                intentIntegrator.setBarcodeImageEnabled(false);
                intentIntegrator.setOrientationLocked(true);
                intentIntegrator.initiateScan();
            }
        });
        Button mShowMain = findViewById(R.id.btnShowMain);
        mShowMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("playerPref",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("groupId",Integer.parseInt(editText.getText().toString()));
                editor.apply();
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(intentResult != null){
            if(intentResult.getContents()==null){
                Toast.makeText(this, "You cancelled the scanning", Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(this, "You joined group "+intentResult.getContents(), Toast.LENGTH_LONG).show();
                SharedPreferences sharedPreferences = getSharedPreferences("playerPref",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("groupId",Integer.parseInt(intentResult.getContents()));
                editor.apply();
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(intent);
            }
        }
        else{
        super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
