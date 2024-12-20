package org.tboox.xmake;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

import org.tboox.xmake.app.R;
import org.tboox.xmake.nativelib.Test;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  // Ensure this matches the layout file name
        String content = Test.loadTests();
        if (content != null) {
            TextView textView = findViewById(R.id.content);
            textView.setText(content);
        }
    }
}
