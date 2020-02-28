package com.example.jsontest;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import static java.lang.System.exit;

public class MainActivity extends AppCompatActivity {

    private EditText edtName, edtAge;
    private Button btnResult;
    private TextView tvOrigin, tvResult;
    private final String SERVER_URL = "http://jang.anymobi.kr/android/myinfo.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnResult = findViewById(R.id.btnResult);
        edtName = findViewById(R.id.edtName);
        edtAge = findViewById(R.id.edtAge);
        tvOrigin = findViewById(R.id.tvOrigin);
        tvResult = findViewById(R.id.tvResult);

        btnResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        parsingData();
                    }
                }).start();
            }
        });
    }

    void parsingData() {
        String requestPOST = SERVER_URL + "?name=" + edtName.getText().toString() + "&" + "age=" + edtAge.getText().toString();
        System.out.println(requestPOST);
        URL url = null;
        BufferedReader input = null;
        String line = "";

        try {
            url = new URL(requestPOST);
            InputStreamReader isr = new InputStreamReader(url.openStream());
            input = new BufferedReader(isr);

            while ((line = input.readLine()) != null) {
                System.out.println(line);
                Log.d("Received Data", line);
                final String finalLine = line;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvOrigin.setText(finalLine);
                        try {
                            // JSON 원문
                            /*
                             * {
                             *   "response":
                             *   {"action_result":"success",
                             *    "action_failure_code":"",
                             *    "action_failure_reason":"",
                             *    "action_success_message":"",
                             *    "logout_by_other":"0",
                             *    "logout_by_timeout":"0"
                             *   },
                             *   "content":
                             *   {"name":"hhh",
                             *    "age":"66"
                             *   }
                             * }
                             * */

                            JSONObject jsonObject = new JSONObject(finalLine);
                            JSONObject response = jsonObject.getJSONObject("response");
                            String result = response.getString("action_result");
                            String reason = response.getString("action_failure_reason");
                            if (result.equals("failure")) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("실패")
                                        .setMessage(reason);
                                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        exit(0);
                                    }
                                });
                                AlertDialog dialog = builder.create();
                                dialog.show();

                                return;
                            } else {
                                JSONObject content = jsonObject.getJSONObject("content");
                                String name = content.getString("name");
                                String age = content.getString("age");
                                Toast.makeText(MainActivity.this, "Name: " + name + " Age: " + age, Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}