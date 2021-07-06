package com.example.rantanplan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class DisplayMessageActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_message);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message1 = intent.getStringExtra("name");
        String message2 = intent.getStringExtra("address");

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textViewName);
        textView.setText(message1);

        TextView textView1 = findViewById(R.id.textViewAddress);
        textView1.setText(message2);

        Button button_correct = (Button) findViewById(R.id.button_correct);
        Button button_wrong = (Button) findViewById(R.id.button_wrong);

        button_correct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        button_wrong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DisplayMessageActivity.this, SecondActivity.class);
                startActivity(intent);
            }
        });
    }
}
