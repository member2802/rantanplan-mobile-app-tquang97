package com.example.rantanplan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class SecondActivity extends Activity {

    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    public Button button_submit;
    public Button button_cancel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alert_user);

        button_submit = (Button) findViewById(R.id.button_submit);
        button_cancel = (Button) findViewById(R.id.button_cancel);

        button_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("submitInfo");
                EditText editText_name = (EditText) findViewById(R.id.place_name_fill_in);
                String message1 = editText_name.getText().toString();
                EditText editText_address = (EditText) findViewById(R.id.place_address_fill_in);
                String message2 = editText_address.getText().toString();
                intent.putExtra("name", message1);
                intent.putExtra("address", message2);
                startActivity(intent);
            }
        });

        button_cancel.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SecondActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }));
    }

}
