package com.kraken.botdashboard;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {

    private EditText  urlInput;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prefs    = getSharedPreferences(MainActivity.PREF_NAME, MODE_PRIVATE);
        urlInput = findViewById(R.id.url_input);

        // Mostrar URL actual
        urlInput.setText(prefs.getString(MainActivity.PREF_URL, MainActivity.DEFAULT_URL));

        Button saveBtn = findViewById(R.id.save_btn);
        Button resetBtn = findViewById(R.id.reset_btn);

        saveBtn.setOnClickListener(v -> {
            String url = urlInput.getText().toString().trim();
            if (TextUtils.isEmpty(url)) {
                urlInput.setError("Introduce una URL");
                return;
            }
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
                urlInput.setText(url);
            }
            prefs.edit().putString(MainActivity.PREF_URL, url).apply();
            Toast.makeText(this, "Guardado: " + url, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        });

        resetBtn.setOnClickListener(v -> {
            urlInput.setText(MainActivity.DEFAULT_URL);
            prefs.edit().putString(MainActivity.PREF_URL, MainActivity.DEFAULT_URL).apply();
            Toast.makeText(this, "URL restaurada por defecto", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
