package com.sigma.temitest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;
import com.google.cloud.dialogflow.v2.Context;
import com.google.cloud.dialogflow.v2.QueryResult;

import com.robotemi.sdk.BatteryData;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.constants.SdkConstants;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Text;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import com.robotemi.sdk.face.ContactModel;
import com.robotemi.sdk.face.OnFaceRecognizedListener;
import com.robotemi.sdk.listeners.OnBatteryStatusChangedListener;
import com.robotemi.sdk.listeners.OnConversationStatusChangedListener;
import com.robotemi.sdk.listeners.OnDetectionStateChangedListener;
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;
import com.robotemi.sdk.listeners.OnUserInteractionChangedListener;
import com.robotemi.sdk.model.DetectionData;
import com.robotemi.sdk.navigation.listener.OnDistanceToLocationChangedListener;

import me.relex.circleindicator.CircleIndicator3;

import static android.provider.Contacts.PresenceColumns.IDLE;

public class MainActivity extends AppCompatActivity implements
        Robot.AsrListener,
        Robot.WakeupWordListener,
        OnDetectionStateChangedListener,
        OnConversationStatusChangedListener,
        OnUserInteractionChangedListener,
        OnGoToLocationStatusChangedListener,
        OnFaceRecognizedListener,
        OnBatteryStatusChangedListener {

    private boolean whileTalking; // 사용자와 대화 중인지.
    private boolean atStandby; // 대기 장소인지 여부.
    private boolean atHome; // 홈 베이스인지 여부
    private boolean moving; // 이동중 여부.
    private boolean interacting; // 상호작용 여부
    private boolean touching; // 터치 여부
    private boolean detected; // 사람 감지 여부

    private Robot robot;

    private SessionsClient sessionsClient;
    private SessionName session;

    private int prev_state; // onDetectionStateChanged 함수에서 사람 있는지 없는지 state에 대한 변수

    private static final String TAG = MainActivity.class.getSimpleName();
    private String uuid = UUID.randomUUID().toString();

    private final String[] teacherAt319_Name = getResources().getStringArray(R.array.teachers_name);
    private int indexOfTeacher; // 테미가 안내해주려는 선생님이 배열에서 몇번째인지 나타냄.

    Button bar; // 대화시 나타나는 초록색 막대
    ImageView mic;  // User가 말할 때 나타나는 마이크 모양 아이콘
    TextView asr;   // User의 음성 입력 텍스트
    TextView tts;   // 테미의 음성 출력 텍스트
    TextView notice;

    //    ViewPager 관련 변수 선언
    private ViewPager2 mPager;
    private FragmentStateAdapter pagerAdapter;
    private int num_page = 2;
    private CircleIndicator3 mIndicator;

    @Override
    protected void onStart() {
        super.onStart();
        Robot.getInstance().addAsrListener(this);
        Robot.getInstance().addWakeupWordListener(this);
        Robot.getInstance().addOnDetectionStateChangedListener(this);
        Robot.getInstance().addOnConversationStatusChangedListener(this);
        Robot.getInstance().addOnGoToLocationStatusChangedListener(this);
        Robot.getInstance().addOnUserInteractionChangedListener(this);
        Robot.getInstance().addOnBatteryStatusChangedListener(this);
        Robot.getInstance().addOnFaceRecognizedListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Robot.getInstance().removeAsrListener(this);
        Robot.getInstance().removeWakeupWordListener(this);
        Robot.getInstance().removeOnDetectionStateChangedListener(this);
        Robot.getInstance().removeOnConversationStatusChangedListener(this);
        Robot.getInstance().removeOnGoToLocationStatusChangedListener(this);
        Robot.getInstance().removeOnUserInteractionChangedListener(this);
        Robot.getInstance().removeOnBatteryStatusChangedListener(this);
        Robot.getInstance().removeOnFaceRecognizedListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        Log.d(TAG, "onPause");
//        bar.setVisibility(View.INVISIBLE);
//        mic.setVisibility(View.INVISIBLE);
//        asr.setText("");
//        tts.setText("");
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        robot = Robot.getInstance(); // get an instance of the robot in order to begin using its features.
        initV2Chatbot();

        robot.startFaceRecognition();

        whileTalking = false;
        moving = false;
        atStandby = true; // 처음 앱이 실행되면, 위치와 관계 없이 사용자를 감지

        asr = (TextView) findViewById(R.id.asr);
        tts = (TextView) findViewById(R.id.tts);
        notice = (TextView) findViewById(R.id.notice);
        bar = (Button) findViewById(R.id.speak_bar);
        mic = (ImageView) findViewById(R.id.mic);
        bar.setVisibility(View.INVISIBLE);
        mic.setVisibility(View.INVISIBLE);

        Button reinitButton = findViewById(R.id.reinit); // 대화 시작 버튼
        reinitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 대화를 시작하거나 이어하기
                robot.askQuestion("말씀해주세요.");
            }
        });

        ImageButton FLIR = findViewById(R.id.thermometer_btn); // 체온 측정 버튼
        FLIR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThermoCheck();
            }
        });

        ImageButton homeButton = findViewById(R.id.home_btn); // 홈 베이스로 이동 버튼
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                robot.speak(TtsRequest.create("홈 베이스로 이동합니다.", true));
                robot.goTo("home base");
            }
        });

        ImageButton standbyButton = findViewById(R.id.standby_btn); // 행정실 입구로 이동 버튼
        standbyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                robot.speak(TtsRequest.create("행정실 입구로 이동합니다.", true));
                robot.goTo("행정실 입구");
            }
        });

        Button stop_talking = findViewById(R.id.stop_talking_btn); // 음성/대화 중지 버튼
        stop_talking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                robot.finishConversation();
                bar.setVisibility(View.INVISIBLE);
                mic.setVisibility(View.INVISIBLE);
                asr.setText("");
                tts.setText("");
            }
        });

        //ViewPager2
        mPager = findViewById(R.id.viewpager);
        //Adapter
        pagerAdapter = new MyAdapter(this, num_page);
        mPager.setAdapter(pagerAdapter);
        //Indicator
        mIndicator = findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        mIndicator.createIndicators(num_page,0);
        //ViewPager Setting
        mPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        mPager.setCurrentItem(1000);
        mPager.setOffscreenPageLimit(3);

        mPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                if (positionOffsetPixels == 0) {
                    mPager.setCurrentItem(position);
                }
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mIndicator.animatePageSelected(position%num_page);
            }

        });

        final float pageMargin= getResources().getDimensionPixelOffset(R.dimen.pageMargin);
        final float pageOffset = getResources().getDimensionPixelOffset(R.dimen.offset);

        mPager.setPageTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float myOffset = position * -(2 * pageOffset + pageMargin);
                if (mPager.getOrientation() == ViewPager2.ORIENTATION_HORIZONTAL) {
                    if (ViewCompat.getLayoutDirection(mPager) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                        page.setTranslationX(-myOffset);
                    } else {
                        page.setTranslationX(myOffset);
                    }
                } else {
                    page.setTranslationY(myOffset);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1){
            if(resultCode==RESULT_OK){
                //데이터 받기
                String teacher = data.getStringExtra("teacher");
                String result = data.getStringExtra("result");
                if(result.equals("Yes")) {
                    Log.d(TAG, "onActivityResult: goTo " + teacher);
                    robot.speak(TtsRequest.create(teacher + " 자리로 이동합니다.", true));
                    robot.goTo(teacher);
                }
            }
        }
    }

    private void initV2Chatbot() {
        try {
            InputStream stream = getResources().openRawResource(R.raw.test_agent_credentials);
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream);
            String projectId = ((ServiceAccountCredentials)credentials).getProjectId();

            SessionsSettings.Builder settingsBuilder = SessionsSettings.newBuilder();
            SessionsSettings sessionsSettings = settingsBuilder.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();
            sessionsClient = SessionsClient.create(sessionsSettings);
            session = SessionName.of(projectId, uuid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void chatInitialize(String input) { // 대화 초기화하는 함수 - 프래그먼트에서 챗봇을 시작하기위해 사용
        QueryInput queryInput = QueryInput.newBuilder().setText(TextInput.newBuilder().setText(input).setLanguageCode("ko")).build();
        new RequestJavaV2Task(MainActivity.this, session, sessionsClient, queryInput).execute();
    }

    public void callbackV2(DetectIntentResponse response) { // RequestJavaV2Task 함수에 의해 불려지는 callback 함수
        if (response != null) {
            QueryResult result = response.getQueryResult();

            // Process aiResponse here.
            String botReply = result.getFulfillmentText();
            // Log.d(TAG, "callbackV2: botReply - " + botReply);

            // Intent name 저장 (테미 이동과 관련).
            String intentName = result.getIntent().getDisplayName();

            int num = result.getOutputContextsCount();
            boolean check = false;

            if (num > 0) {
                for (int i = 0; i < num; i++){
                    String[] contextName = result.getOutputContextsList().get(i).getName().split("/");
                    if (contextName[contextName.length - 1].contains("movetemi")){
                        check = true;
                        break;
                    }
                }
            }

            // 행정실(319호)에 계신 선생님에 대해서 사용자가 물어본 경우(= movetemi context가 있는 intent의 경우), 선생님 이름 저장.
            if (check){
                // Log.d(TAG, "319호 선생님에 대한 질문");
                for (int i = 0; i < teacherAt319_Name.length; i++ ){
                    if (botReply.contains(teacherAt319_Name[i])){
                        indexOfTeacher = i;
                        Log.d(TAG, teacherAt319_Name[i]);
                        break;
                    }
                }
            }

            // 테미가 이동하는 경우.
            if (intentName.equals("MoveTemi-Yes")){
                robot.speak(TtsRequest.create(teacherAt319_Name[indexOfTeacher] + " 선생님 자리로 " + botReply, true));
                robot.goTo(teacherAt319_Name[indexOfTeacher] + " 선생님");
            }

            else if (botReply.contains("홈베이스")) {
                robot.speak(TtsRequest.create(botReply, true));
                robot.goTo("home base");
            }

            else if (botReply.contains("행정실 입구")) {
                robot.speak(TtsRequest.create(botReply, true));
                robot.goTo("행정실 입구");
            }

            else if (botReply.equals("체온 측정")) { // 체온 측정하는 코드, Dialogflow에서 'Temperature_Check' 인텐트에 의한 리스폰스
                ThermoCheck();
            }

            else if (botReply.contains("날씨")){
                robot.speak(TtsRequest.create("날씨 정보를 알려드립니다.", true));
                Intent intent = new Intent(MainActivity.this, Web.class);
                intent.putExtra("url", "https://weather.naver.com/today/09620735");
                startActivity(intent);
//                startActivity(new Intent(MainActivity.this, Weather.class));
            }

            // 그 이외는 Dialogflow 답변을 그대로 읽어줌. 물음표 여부를 통해서 다음 대답을 받을지 판단 (추가적인 context로 추후 구분 가능성).
            else {
                if (botReply.contains("?"))
                    robot.askQuestion(botReply);
                else {
                    if (botReply.contains("button"))
                        return;
                    else
                        robot.speak(TtsRequest.create(botReply, true));
                }
            }
        } else {
            //Log.d(TAG, "Bot Reply: Null");
            robot.speak(TtsRequest.create("전송 오류가 있었습니다. 다시 시도해주시기 바랍니다.", true));
        }
    }

    public void speak(String s){
        robot.speak(TtsRequest.create(s, true));
    }

    public double send(String data){ // 체온 측정 관련 함수
        int port = 5001;

        try{
            Socket sock = new Socket("192.168.0.6", port);

            ObjectOutputStream outstream = new ObjectOutputStream(sock.getOutputStream());
            outstream.writeObject(data);
            outstream.flush();

            ObjectInputStream instream = new ObjectInputStream(sock.getInputStream());
            String input = (String) instream.readObject();
            Log.d(TAG, "Temperature: " + input);
            sock.close();

            return Double.valueOf(input);

        } catch(Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    public void ThermoCheck(){
        robot.speak(TtsRequest.create("체온 측정 중입니다", true));
        new Thread(new Runnable() {
            @Override
            public void run() {
                double temp = send("start_measure");
                temp = Math.round(temp * 100.0) / 100.0;
                if (temp < 38.0) robot.speak(TtsRequest.create("체온이 "+temp+"º로 측정되었습니다.", true));
                else if (temp < 50.0) robot.speak(TtsRequest.create(temp + "º. 위험 체온입니다. 출입을 불허합니다.", true));
                else robot.speak(TtsRequest.create("측정을 방해하는 물체가 있습니다. 다른 각도에서 다시 시도해주시기 바랍니다.", true));
            }
        }).start();
    }

    @Override
    public void onAsrResult(final @NonNull String asrResult) { // https://github.com/robotemi/sdk/wiki/Speech 참고
        QueryInput queryInput = QueryInput.newBuilder().setText(TextInput.newBuilder().setText(asrResult).setLanguageCode("ko")).build();
        new RequestJavaV2Task(MainActivity.this, session, sessionsClient, queryInput).execute();
        try {
            Bundle metadata = getPackageManager()
                    .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA)
                    .metaData;
            if (metadata == null) return;
            if (!robot.isSelectedKioskApp()) return;
            if (!metadata.getBoolean(SdkConstants.METADATA_OVERRIDE_NLU)) return;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void onWakeupWord(@NotNull String wakeupWord, int direction) { // https://github.com/robotemi/sdk/wiki/Speech 참고
        Log.d(TAG, "onWakeupWord: " + wakeupWord);
        robot.askQuestion("네?");
    }

    //https://github.com/robotemi/sdk/wiki/Detection-&-Interaction
    // 0 - IDLE, 1 - LOST, 2 - ACTIVE.
    @Override
    public void onDetectionStateChanged(int state) {
//        Log.d(TAG, "onDetectionStateChanged: " + state);
        if(state == DETECTED) detected = true;
        else if(state == OnDetectionStateChangedListener.IDLE) detected = false;

        // 새로운 사용자가 앞에 왔을 때.
        if (state == OnDetectionStateChangedListener.DETECTED && prev_state == OnDetectionStateChangedListener.IDLE) {
//            Log.d(TAG, "New user detected at somewhere.");

            // 대기 상태에 있으며, 대화를 하고 있지 않았을때만 인사한다.
            if (!whileTalking && atStandby) {
//                Log.d(TAG, "New user detected at standby.");
                chatInitialize("greeting");
            }
        }
        prev_state = state;
    }

    @Override
    public void onConversationStatusChanged(int status, @NotNull String text) { //https://github.com/robotemi/sdk/wiki/Speech 참고

        if (status == 0) {  // "대화가 끝났을 때"
            Log.d(TAG, "onConvStatusChanged " + status);
            whileTalking = false;
            bar.setVisibility(View.INVISIBLE);
            mic.setVisibility(View.INVISIBLE);
            notice.setVisibility(View.VISIBLE);
            asr.setText("");
            tts.setText("");

            // 만약 모든 상호작용이 끝났고, 대기 장소나 홈 베이스가 아니면.
            if (!moving && !atStandby && !atHome) {
//                Log.d(TAG, "Countdown Started.");

                // 10초 countdown, 0.4초마다 tick 확인.
                new CountDownTimer(5000, 400) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        // 새로운 interaction이 시작되거나 대기 장소, 홈 베이스가 되면 취소.
                        if (whileTalking || moving || atStandby || atHome) {
                            cancel();
//                            Log.d(TAG, "Countdown canceled because of new interaction.");
                        }
                    }
                    @Override
                    public void onFinish() { // 정상적으로 타이머가 끝나면, 대기 장소 자동 복귀.
                        Log.d(TAG, "Countdown finished, return to standby.");
                        robot.speak(TtsRequest.create("행정실 입구로 돌아갑니다.", true));
                        robot.goTo("행정실 입구");
                    }
                }.start();
            }
        }

        if (status == 1) {  // Listening user's voice
            whileTalking = true;
            notice.setVisibility(View.INVISIBLE);
            asr.setText("");
            mic.setVisibility(View.VISIBLE);
            bar.setVisibility(View.VISIBLE);
            asr.setText(text);
        }
        if (status == 2) {  // NLP
            Log.d(TAG, "onConvStatusChanged " + status + ": " + text);
            whileTalking = true;
            asr.setText(text);
        }
        if (status == 3) {  // Playing TTS
            Log.d(TAG, "onConvStatusChanged " + status + ": " + text);
            whileTalking = true;
            notice.setVisibility(View.INVISIBLE);
            asr.setText("");
            mic.setVisibility(View.INVISIBLE);
            bar.setVisibility(View.INVISIBLE);
            tts.setText(text);
        }
    }

    // START, CALCULATING, GOING, COMPLETE, ABORT.
    @Override
    public void onGoToLocationStatusChanged(@NotNull String location, @NotNull String status, int descriptionID, @NotNull String description) { //https://github.com/robotemi/sdk/wiki/Locations 참고
        atStandby = false; // 일단 뭔가 status 변화가 있으면, 인사 안하도록.
        atHome = false; // 이동 상태가 변하면 대기 장소나 홈 베이스에서 벗어나게 됨
//        robot.setDetectionModeOn(false);
//        robot.stopFaceRecognition();
//        Log.d(TAG, "atStandby is false");

        if (status.equals(OnGoToLocationStatusChangedListener.START)) moving = true;

        else if (status.equals(OnGoToLocationStatusChangedListener.COMPLETE)){
            moving = false;

            // Standby에 도착한 경우, 다시 인사 하도록 변수 설정.
            if (location.equals("행정실 입구")) {
                atStandby = true;
//                robot.setDetectionModeOn(true);
//                robot.startFaceRecognition();
//                Log.d(TAG, "atStandby is true");
            }

            else if (location.equals("home base")) {
                atHome = true;
                robot.speak(TtsRequest.create("홈베이스에 도착했습니다.", true));
            }

            // 선생님 자리에 도착.
            else
                robot.speak(TtsRequest.create("도착했습니다. 다른 용무가 있으시면 대화하기 버튼을 눌러주세요.", true));
        }

        else if (status.equals(OnGoToLocationStatusChangedListener.ABORT)) {
            moving = false;
            robot.askQuestion("네, 부르셨나요?");
        }
    }

    @Override
    public void onUserInteraction(boolean b) {  // 상호작용(화면 터치, 사람 감지, 이동)이 일어나면 실행되는 함수 - 실행이 안됨

        Log.d(TAG, "onUserInteraction: " + b);
//        interacting = b;
//        if(detected) interacting = false;
//        if(!interacting && !atStandby && !atHome){
//            // 10초 countdown, 0.4초마다 tick 확인.
//            new CountDownTimer(10000, 400) {
//                @Override
//                public void onTick(long millisUntilFinished) {
//                    // 새로운 interaction이 시작되거나 대기 장소, 홈 베이스가 되면 취소.
//                    if (interacting || atStandby || atHome) {
//                        cancel();
//                        Log.d(TAG, "Countdown canceled because of new interaction.");
//                    }
//                }
//
//                @Override
//                public void onFinish() { // 정상적으로 타이머가 끝나면, 대기 장소 자동 복귀.
//                    Log.d(TAG, "Countdown finished, return to standby.");
//                    robot.goTo("행정실 입구");
//                }
//
//            }.start();
//
//        }
    }

    @Override
    public void onBatteryStatusChanged(@Nullable BatteryData batteryData) { // 배터리 효율이 안좋아서 사용할 일 없을듯
//        Log.d(TAG, "onBatteryStatusChanged: " + batteryData);
//        if (batteryData.component2() == true && batteryData.component1() == 100) {
//            robot.speak(TtsRequest.create("충전이 완료되었습니다.", true));
//            robot.goTo("행정실 입구");
//        }
    }

    @Override
    public void onFaceRecognized(@Nullable List<ContactModel> list) {
        Log.d(TAG, "onFaceRecognized: " + list);

    }
}
