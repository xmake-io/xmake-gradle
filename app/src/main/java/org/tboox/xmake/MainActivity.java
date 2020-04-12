package org.tboox.xmake;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import org.tboox.xmake.nativelib.Test;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadNativeTests();
    }

    private void loadNativeTests() {
    }
}
