package io.virtualapp.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private final static int FILE_SELECT_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout main= new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);
        setContentView(main);
        AddButton(main, "File",(v)->{
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/vnd.ms-excel;application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            try {
                startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), FILE_SELECT_CODE);
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
            }
        });
        AddButton(main, "Image",(v)->{
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/jpg");
            try {
                startActivityForResult(Intent.createChooser(intent, "Select a Image to Upload"), FILE_SELECT_CODE);
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
            }
        });
        AddButton(main, "Share",(v)->{
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
            sendIntent.setType("text/plain");
            try {
                startActivityForResult(Intent.createChooser(sendIntent, "Select a File to Share"), FILE_SELECT_CODE);
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void AddButton(ViewGroup main, String text, View.OnClickListener clickListener){
        Button button =new Button(this);
        button.setText(text);
        button.setOnClickListener(clickListener);
        main.addView(button);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Toast.makeText(this, "requestCode="+requestCode+",resultCode="+resultCode+"\n"+data, Toast.LENGTH_LONG).show();
        super.onActivityResult(requestCode, resultCode, data);
    }
}
