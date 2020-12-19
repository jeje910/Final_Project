package com.example.finalproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Context cThis;
    String logTT="[STT]";

    // 음성 인식용
    Intent SttIntent;
    SpeechRecognizer mRecognizer;

    //음성 출력용
    TextToSpeech tts;

    // 화면 처리용
    Button btnSttStart;
    EditText txtInMsg;
    EditText txtSystem;
    String language;
    TextView lan_text;

    // Dropdown box 입력용
    String[] items_from = {"한국어", "English"};
    String[] items_to = {"English", "한국어"};
    String[] items_font = {"폰트1", "폰트2", "폰트3"};
    String[] items_fontsize = {"10", "12", "14"};
    String[] items_fontcolor = {"하얀색", "검정색"};
    String[] items_background = {"기본", "검정색", "하얀색"};

    // Dropdown box 출력용
    String changed_font;
    int changed_fontsize;
    String changed_fontcolor;
    String changed_background;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        cThis=this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this.getIntent());
        lan_text = findViewById(R.id.lan_text);
        language = intent.getStringExtra("language");
        lan_text.setText(language);
        Log.d("taggg", language);

        Spinner spinner_from = findViewById(R.id.from_spinner);
        Spinner spinner_to = findViewById(R.id.to_spinner);
        Spinner spinner_font = findViewById(R.id.font_spinner);
        Spinner spinner_fontsize = findViewById(R.id.font_size_spinner);
        Spinner spinner_fontcolor = findViewById(R.id.font_color_spinner);
        Spinner spinner_background = findViewById(R.id.background_color_spinner);

        ArrayAdapter<String> from_adapter = new ArrayAdapter<String>(
                getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, items_from
        );
        ArrayAdapter<String> to_adapter = new ArrayAdapter<String>(
                getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, items_to
        );
        ArrayAdapter<String> font_adapter = new ArrayAdapter<String>(
                getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, items_font
        );
        ArrayAdapter<String> fontsize_adapter = new ArrayAdapter<String>(
                getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, items_fontsize
        );
        ArrayAdapter<String> fontcolor_adapter = new ArrayAdapter<String>(
                getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, items_fontcolor
        );
        ArrayAdapter<String> background_adapter = new ArrayAdapter<String>(
                getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, items_background
        );

        spinner_from.setAdapter(from_adapter);
        spinner_to.setAdapter(to_adapter);
        spinner_font.setAdapter(font_adapter);
        spinner_fontsize.setAdapter(fontsize_adapter);
        spinner_fontcolor.setAdapter(fontcolor_adapter);
        spinner_background.setAdapter(background_adapter);

        spinner_fontsize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                changed_fontsize = Integer.parseInt(items_fontsize[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinner_fontcolor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                changed_fontcolor = items_fontcolor[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinner_background.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                changed_background = items_background[i];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

         */

        // 음성인식
        SttIntent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        SttIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getApplicationContext().getPackageName());
        Log.d("tagggg", language);
        if (language.equals("Korean to English")) {
            SttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR"); // 한국어 사용
        }
        else {
            SttIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US"); // 영어 사용
        }
        mRecognizer=SpeechRecognizer.createSpeechRecognizer(cThis);
        mRecognizer.setRecognitionListener(listener);

        // 음성출력 생성, 리스너 초기화
        tts = new TextToSpeech(cThis, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR){
                    if (language.equals("Korean to English")) {
                        tts.setLanguage(Locale.KOREAN);
                    }
                    else {
                        tts.setLanguage(Locale.ENGLISH);
                    }
                }
            }
        });
        // 버튼 설정
        btnSttStart=(Button) findViewById(R.id.btn_stt_start);
        btnSttStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("음성인식 시작!");
                //권한이 허용되어있지 않을 경우
                if (ContextCompat.checkSelfPermission(cThis, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                }
                //권한을 허용한 경우
                else {
                    try {
                        mRecognizer.startListening(SttIntent);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        txtInMsg=(EditText)findViewById(R.id.txtInMsg);
        txtSystem=(EditText)findViewById(R.id.txtSystem);
        //어플이 실행되면 자동으로 1초뒤에 음성 인식 시작
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                btnSttStart.performClick();
            }
        },1000);//바로 실행을 원하지 않으면 지워주시면 됩니다
    }

    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            txtSystem.setText("Ready..");
        }
        @Override
        public void onBeginningOfSpeech() {
            txtSystem.setText("지금부터 말을 해주세요");
        }

        @Override
        public void onRmsChanged(float v) {

        }

        @Override
        public void onBufferReceived(byte[] bytes) {
            txtSystem.setText("onBufferReceived");
        }

        @Override
        public void onEndOfSpeech() {
            txtSystem.setText("대화가 종료되었습니다. 다시하려면 버튼을 클릭해주세요");
        }

        @Override
        public void onError(int i) {

        }

        @Override
        public void onResults(Bundle results) {
            String key= "";
            key = SpeechRecognizer.RESULTS_RECOGNITION;
            ArrayList<String> mResult =results.getStringArrayList(key);
            String[] rs = new String[mResult.size()];
            mResult.toArray(rs);
            txtInMsg.setText(rs[0]+"\r\n");
            FuncVoiceOrderCheck(rs[0]);
            mRecognizer.startListening(SttIntent);

            BackgroundTask task = new BackgroundTask();
            task.execute(rs[0]);
        }

        @Override
        public void onPartialResults(Bundle bundle) {
            txtSystem.setText("onPartialResults");
        }

        @Override
        public void onEvent(int i, Bundle bundle) {
            txtSystem.setText("onEvent");
        }
    };

    //입력된 음성 메세지 확인 후 동작 처리
    private void FuncVoiceOrderCheck(String VoiceMsg){
        if(VoiceMsg.length()<1)return;

        VoiceMsg=VoiceMsg.replace(" ","");//공백제거

        if(VoiceMsg.indexOf("카카오톡")>-1 || VoiceMsg.indexOf("카톡")>-1){
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.kakao.talk");
            startActivity(launchIntent);
            onDestroy();
        }//카카오톡 어플로 이동
        if(VoiceMsg.indexOf("전동꺼")>-1 || VoiceMsg.indexOf("불꺼")>-1){
            FuncVoiceOut("전등을 끕니다");//전등을 끕니다 라는 음성 출력
        }
    }

    //음성 메세지 출력용
    private void FuncVoiceOut(String OutMsg){
        if(OutMsg.length()<1)return;

        tts.setPitch(1.0f);//목소리 톤1.0
        tts.setSpeechRate(1.0f);//목소리 속도
        tts.speak(OutMsg,TextToSpeech.QUEUE_FLUSH,null);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(tts!=null){
            tts.stop();
            tts.shutdown();
            tts=null;
        }
        if(mRecognizer!=null){
            mRecognizer.destroy();
            mRecognizer.cancel();
            mRecognizer=null;
        }
    }

    class BackgroundTask extends AsyncTask<String,Void,String> {
        @Override
        protected String doInBackground(String... str) {
            String inputTest = str[0];
            String clientId = "378gjuK8BlJffNWUF0p3";
            String clientSecret = "UIG7F_Uib2";
            String result = "";
            try {
                String text = URLEncoder.encode(inputTest, "UTF-8");
                String apiURL = "https://openapi.naver.com/v1/papago/n2mt";
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("X-Naver-Client-Id", clientId);
                con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
                String postParams;

                if (language.equals("Korean to English")) {
                    postParams = "source=ko&target=en&text=" + text;
                }
                else {
                    postParams = "source=en&target=ko&text=" + text;
                }

                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(postParams);
                wr.flush();
                wr.close();

                int responseCode = con.getResponseCode();
                BufferedReader br;
                if (responseCode == 200) {
                    br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                } else {
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                }
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();
                result = response.toString();
            } catch (Exception e) {
                result = "실패";
                System.out.println(e);
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            String tmp = s.split("\"")[27];
            SpannableStringBuilder translated_text = new SpannableStringBuilder(tmp);

            if(changed_fontsize==12){
                translated_text.setSpan(new RelativeSizeSpan(1.15f), 0, tmp.length(), 0);
            }
            else if(changed_fontsize==14){
                translated_text.setSpan(new RelativeSizeSpan(1.3f), 0, tmp.length(), 0);
            }

            if(changed_fontcolor=="검정색"){
                translated_text.setSpan(new ForegroundColorSpan(Color.BLACK), 0, tmp.length(), 0);
            }

            Toast toast = Toast.makeText(getApplicationContext(), translated_text, Toast.LENGTH_LONG);

            if(changed_background=="검정색"){
                View view = toast.getView();
                view.setBackgroundColor(Color.BLACK);
                toast.setView(view);
            }
            else if(changed_background=="하얀색"){
                View view = toast.getView();
                view.setBackgroundColor(Color.WHITE);
                toast.setView(view);
            }
            toast.show();

        }
    }
}