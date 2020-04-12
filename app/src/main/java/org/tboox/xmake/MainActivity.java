package org.tboox.xmake;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import org.tboox.xmake.nativelib.Test;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String content = Test.loadTests();
        if (content != null) {
            TextView textView = (TextView) findViewById(R.id.content);
            textView.setText(content);
        }
    }
}
