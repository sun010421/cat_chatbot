package com.sigma.temitest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.api.client.util.Sleeper;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;
import com.google.cloud.dialogflow.v2.QueryResult;

import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.constants.SdkConstants;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Locale;
import java.util.UUID;

import com.robotemi.sdk.listeners.OnConversationStatusChangedListener;
import com.robotemi.sdk.listeners.OnDetectionDataChangedListener;
import com.robotemi.sdk.listeners.OnDetectionStateChangedListener;
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;
import com.robotemi.sdk.listeners.OnUserInteractionChangedListener;
import com.robotemi.sdk.model.DetectionData;

import me.relex.circleindicator.CircleIndicator3;

import static android.speech.tts.TextToSpeech.ERROR;

public class MainActivity extends AppCompatActivity implements
        Robot.AsrListener,
        Robot.WakeupWordListener,
        Robot.ConversationViewAttachesListener,
        OnDetectionStateChangedListener,
        OnDetectionDataChangedListener,
        OnConversationStatusChangedListener,
        OnUserInteractionChangedListener,
        OnGoToLocationStatusChangedListener {

    private boolean whileTalking; // 사용자와 대화 중인지.
    private boolean atStandby; // 대기 장소인지 여부.
    private boolean atHome; // 홈 베이스인지 여부
    private boolean moving; // 이동중 여부.
    private boolean interacting; // 상호작용 여부
    private boolean touching; // 터치 여부
    int detect_state; // 사람 감지 여부

    private TextToSpeech ttsSpeak;
    private TextToSpeech ttsAskQuestion;
    private Locale language;

    private Robot robot;

    private SessionsClient sessionsClient;
    private SessionName session;

    private int prev_state; // onDetectionStateChanged 함수에서 사람 있는지 없는지 state에 대한 변수
    int prev_conv_status = 0;
    boolean prev_detect_state = false;
    boolean prev_prev_detect_state = false;
    boolean[] isDetected = new boolean[5];
    DetectionData[] detection = new DetectionData[N];
    final static int N = 6;

    private static final String TAG = MainActivity.class.getSimpleName();
    private String uuid = UUID.randomUUID().toString();

    private String[] teacherAt319_Name;
    private int indexOfTeacher; // 테미가 안내해주려는 선생님이 배열에서 몇번째인지 나타냄.

    ImageView mic;  // User가 말할 때 나타나는 마이크 모양 아이콘
    Button start_talking;
    Button stop_talking;
    Button english_btn;
    Button korean_btn;

    //    ViewPager 관련 변수 선언
    private ViewPager2 mPager;
    private FragmentStateAdapter pagerAdapter;
    private FragmentStateAdapter pagerAdapter_en;
    private int num_page = 2;
    private CircleIndicator3 mIndicator;

    @Override
    protected void onStart() {
        super.onStart();
        Robot.getInstance().addAsrListener(this);
        Robot.getInstance().addWakeupWordListener(this);
        Robot.getInstance().addConversationViewAttachesListenerListener(this);
        Robot.getInstance().addOnDetectionStateChangedListener(this);
        Robot.getInstance().addOnDetectionDataChangedListener(this);
        Robot.getInstance().addOnConversationStatusChangedListener(this);
        Robot.getInstance().addOnGoToLocationStatusChangedListener(this);
        Robot.getInstance().addOnUserInteractionChangedListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Robot.getInstance().removeAsrListener(this);
        Robot.getInstance().removeWakeupWordListener(this);
        Robot.getInstance().removeConversationViewAttachesListenerListener(this);
        Robot.getInstance().removeOnDetectionStateChangedListener(this);
        Robot.getInstance().removeOnDetectionDataChangedListener(this);
        Robot.getInstance().removeOnConversationStatusChangedListener(this);
        Robot.getInstance().removeOnGoToLocationStatusChangedListener(this);
        Robot.getInstance().removeOnUserInteractionChangedListener(this);
    }

    AlertDialog.Builder builder;
    AlertDialog alertDialog;
    Dialog dialog;
    TextView AsrText;
    TextView TtsText;
    TextView Notice;

    @Override
    public void onConversationAttaches(boolean b) {
        Log.d(TAG, "onConversationAttaches: " + b);

        if(b)
            dialog.show();
        else if(!b && !whileTalking)
            dialog.dismiss();
        else
            return;
    }

    public boolean greeting(DetectionData[] detectionData){
        for(int i = 0; i < detectionData.length-1; i++){
            if(!detectionData[i].isDetected())
                return false;
        }
        return true;
    }

    @Override
    public void onDetectionDataChanged(@NotNull DetectionData detectionData) {
        Log.d(TAG, "onDetectionDataChanged: " + detectionData);

//        robot.stopMovement();

        if(atStandby && !whileTalking && !detection[N-1].isDetected()){
            if(greeting(detection)) {
//                dialog.show();
//                if(language == Locale.KOREAN)
//                    askQuestion("안녕하세요.\n무슨 일로 오셨나요?");
//                else
//                    askQuestion("Hi.\nHow can I help you?");
            }
        }

        for(int i = N-1; i > 0; i--)
            detection[i] = detection[i-1];
        detection[0] = detectionData;

//        for(int i = 4; i > 0; i--)
//            isDetected[i] = isDetected[i-1];
//        isDetected[0] = detectionData.isDetected();
    }

    public void setKorean(View view){
        mPager.setAdapter(pagerAdapter);
        language = Locale.KOREAN;
        ttsSpeak.setLanguage(language);
        ttsAskQuestion.setLanguage(language);
        start_talking.setText(R.string.start_talking);
        stop_talking.setText(R.string.stop_talking);
        Notice.setText(R.string.notice);
        korean_btn.setVisibility(View.INVISIBLE);
        english_btn.setVisibility(View.VISIBLE);
    }

    public void setEnglish(View view){
        mPager.setAdapter(pagerAdapter_en);
        language = Locale.ENGLISH;
        ttsSpeak.setLanguage(language);
        ttsAskQuestion.setLanguage(language);
        start_talking.setText(R.string.start_talking_en);
        stop_talking.setText(R.string.stop_talking_en);
        Notice.setText(R.string.notice_en);
        english_btn.setVisibility(View.INVISIBLE);
        korean_btn.setVisibility(View.VISIBLE);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        robot = Robot.getInstance(); // get an instance of the robot in order to begin using its features.

        initV2Chatbot();

        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog, null);
//        builder = new AlertDialog.Builder(this)
//                .setView(dialogView);

//        alertDialog = builder.create();
//        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(dialogView);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                whileTalking = false;
                robot.finishConversation();
                ttsSpeak.stop();
                ttsAskQuestion.stop();
                AsrText.setText("");
                TtsText.setText("");
                mic.setVisibility(View.INVISIBLE);
            }
        });


        for(int i = 0; i < N; i++)
            detection[i] = new DetectionData(0, 0, false);


        AsrText = dialogView.findViewById(R.id.AsrText);
        TtsText = dialogView.findViewById(R.id.TtsText);

        Resources resources = getResources();
//        if(resources.getStringArray(R.array.teachers_name) != null)
        teacherAt319_Name = resources.getStringArray(R.array.teachers_name);

        whileTalking = false;
        moving = false;
        atStandby = true; // 처음 앱이 실행되면, 위치와 관계 없이 사용자를 감지

        mic = dialogView.findViewById(R.id.mic);
        start_talking = findViewById(R.id.start_talking_btn);
        stop_talking = findViewById(R.id.stop_talking_btn);
        Notice = findViewById(R.id.notice);
        Notice.setText(R.string.notice);
        english_btn = findViewById(R.id.english_btn);
        korean_btn = findViewById(R.id.korean_btn);
        korean_btn.setVisibility(View.INVISIBLE);

        // 대화 시작 버튼
        start_talking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 대화를 시작하거나 이어하기
                dialog.show();
                if (language == Locale.KOREAN)
                    askQuestion("말씀해주세요.");
                else
                    askQuestion("I'm listening.");
            }
        });

         // 음성/대화 중지 버튼
        stop_talking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ttsSpeak.stop();
                ttsAskQuestion.stop();
                whileTalking = false;
                robot.finishConversation();
                mic.setVisibility(View.INVISIBLE);
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
                if (language == Locale.KOREAN)
                    speak("홈 베이스로 이동합니다.");
                else
                    speak("Moving to home base.");

                robot.goTo("home base");
            }
        });

        ImageButton standbyButton = findViewById(R.id.standby_btn); // 행정실 입구로 이동 버튼
        standbyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (language == Locale.KOREAN)
                    speak("행정실 입구로 이동합니다.");
                else
                    speak("Moving to entrance of office.");

                robot.goTo("행정실 입구");
//                startActivity(new Intent(MainActivity.this, CameraX.class));
            }
        });

        //ViewPager2
        mPager = findViewById(R.id.viewpager);
        //Adapter
        pagerAdapter = new MyAdapter(this, num_page);
        pagerAdapter_en = new MyAdapterEn(this, num_page);
        mPager.setAdapter(pagerAdapter);
        //Indicator
        mIndicator = findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        mIndicator.createIndicators(num_page,0);
        //ViewPager Setting
        mPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        mPager.setCurrentItem(0);
        mPager.setOffscreenPageLimit(1);

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

        // Android tts
        language = Locale.KOREAN;
        ttsSpeak = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != ERROR) {
                    ttsSpeak.setLanguage(language);
                    ttsSpeak.setPitch(1.0f);
                    ttsSpeak.setSpeechRate(1.0f);

                }
            }
        });

        ttsSpeak.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        whileTalking = true;
                        mic.setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void onDone(String s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        whileTalking = false;
                        robot.finishConversation();
                        dialog.dismiss();
                    }
                });
            }

            @Override
            public void onError(String s) {
            }

            @Override
            public void onRangeStart(String utteranceId, int start, int end, int frame) {
            }
        });

        ttsAskQuestion = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != ERROR) {
                    ttsAskQuestion.setLanguage(language);
                    ttsAskQuestion.setPitch(1.0f);
                    ttsAskQuestion.setSpeechRate(1.0f);
                }
            }
        });

        ttsAskQuestion.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        Log.d("Test: ", "ok2");
                        whileTalking = true;
                        mic.setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void onDone(String s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 말하는 것을 듣지 않도록, onDone에서 Listening 시작.
//                        Log.d("Test: ", "ok3");
//                        robot.askQuestion(".");
                        robot.askQuestion(".");
                        robot.wakeup();
                        whileTalking = true;
                        mic.setVisibility(View.VISIBLE);

                    }
                });
            }

            @Override
            public void onError(String s) {
                Log.d("Test: ", "error");
            }

            @Override
            public void onRangeStart(String utteranceId, int start, int end, int frame) {
            }
        });

    }

    private void askQuestion(String question) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TtsText.setText(question);
            }
        });

        // QUEUE_FLUSH: 초기화하고 새로 넣는 것, QUEUE_ADD: 현재 말하는거 다음으로 대기.
        ttsAskQuestion.speak(question, TextToSpeech.QUEUE_FLUSH, null, "question");
    }

    private void speak(String sentence) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TtsText.setText(sentence);
            }
        });

        ttsSpeak.speak(sentence, TextToSpeech.QUEUE_FLUSH, null,"speak");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1){
            if (resultCode==RESULT_OK){
                //데이터 받기
                String teacher = data.getStringExtra("teacher");
                String result = data.getStringExtra("result");
                if (result.equals("Yes")) {
                    Log.d(TAG, "onActivityResult: goTo " + teacher);
//                    robot.setDetectionModeOn(false);

                    if (language == Locale.KOREAN)
                        speak(teacher + " 자리로 이동합니다.");
                    else
                        speak("Moving to location.");

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
        String lan;
        if (language == Locale.KOREAN) lan = "ko";
        else lan = "en";

        QueryInput queryInput = QueryInput.newBuilder()
                .setText(TextInput.newBuilder()
                        .setText(input)
                        .setLanguageCode(lan))
                .build();

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
                if (language == Locale.KOREAN)
                    speak(teacherAt319_Name[indexOfTeacher] + " 선생님 자리로 이동합니다.");
                else
                    speak(botReply);

                robot.goTo(teacherAt319_Name[indexOfTeacher] + " 선생님");
            }

            else if (intentName.equals("GoToHomeBase")) {
                speak(botReply);
                robot.goTo("home base");
            }

            else if (intentName.equals("GoToStandBy")) {
                speak(botReply);
                robot.goTo("행정실 입구");
            }

            else if (intentName.equals("TemperatureCheck")) { // 체온 측정하는 코드, Dialogflow에서 'Temperature_Check' 인텐트에 의한 리스폰스
                ThermoCheck();
            }

            else if (botReply.contains("온도")){
                speak(botReply);
                Intent intent = new Intent(MainActivity.this, Web.class);
                intent.putExtra("url", "https://weather.naver.com/today/09620735");
                startActivity(intent);
            }

            else if (botReply.contains("소반")){
                speak("오늘의 학식 메뉴를 알려드립니다.");
                Intent intent = new Intent(MainActivity.this, PopupActivity2.class);
                intent.putExtra("text", botReply);
                startActivity(intent);
            }

            // 그 이외는 Dialogflow 답변을 그대로 읽어줌. 물음표 여부를 통해서 다음 대답을 받을지 판단 (추가적인 context로 추후 구분 가능성).
            else {
                if (botReply.contains("?"))
                    askQuestion(botReply);
                else {
                    if (botReply.contains("button"))
                        return;
                    else
                        speak(botReply);
                }
            }
        } else {
            //Log.d(TAG, "Bot Reply: Null");
//            robot.speak(TtsRequest.create("전송 오류가 있었습니다. 다시 시도해주시기 바랍니다.", true));

            if (language == Locale.KOREAN)
                speak("전송 오류가 있었습니다. 다시 시도해주시기 바랍니다.");
            else
                speak("Error in sending request. Try again.");


        }
    }

    public double send(String data){ // 체온 측정 관련 함수
        int port = 5001;

        try{
            Socket sock = new Socket("192.168.0.56", port);

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
        if (language == Locale.KOREAN)
            speak("체온 측정 중입니다.");
        else
            speak("Checking temperature.");

        new Thread(new Runnable() {
            @Override
            public void run() {
                double temp = send("start_measure");
                temp = Math.round(temp * 100.0) / 100.0;
                if (temp < 38.0){
                    if (language == Locale.KOREAN)
                        speak("체온이 " + temp + "º로 측정되었습니다.");
                    else
                        speak("You are " + temp + "degrees.");
                }

                else if (temp < 50.0){
                    if (language == Locale.KOREAN)
                        speak(temp + "º. 위험 체온입니다. 출입을 불허합니다.");
                    else
                        speak("You are " + temp + "degrees. Please do not enter.");
                }

                else {
                    if (language == Locale.KOREAN)
                        speak("측정을 방해하는 물체가 있습니다. 다른 각도에서 다시 시도해주시기 바랍니다.");
                    else
                        speak("There is something hindering the process. Please try again in another angle.");
                }
            }
        }).start();
    }

    @Override
    public void onAsrResult(final @NonNull String asrResult) { // https://github.com/robotemi/sdk/wiki/Speech 참고
//        QueryInput queryInput = QueryInput.newBuilder().setText(TextInput.newBuilder().setText(asrResult).setLanguageCode("ko")).build();
//        new RequestJavaV2Task(MainActivity.this, session, sessionsClient, queryInput).execute();
        chatInitialize(asrResult);
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
//        robot.stopMovement();
    }

    //https://github.com/robotemi/sdk/wiki/Detection-&-Interaction
    // 0 - IDLE, 1 - LOST, 2 - ACTIVE.
    @Override
    public void onDetectionStateChanged(int state) {
        Log.d(TAG, "onDetectionStateChanged: " + state);
        detect_state = state;

        // 새로운 사용자가 앞에 왔을 때.
        if (state == OnDetectionStateChangedListener.DETECTED && prev_state == OnDetectionStateChangedListener.IDLE) {
//            Log.d(TAG, "New user detected at somewhere.");

            // 대기 상태에 있으며, 대화를 하고 있지 않았을때만 인사한다.
            if (!whileTalking && atStandby) {
//                Log.d(TAG, "New user detected at standby.");
//                chatInitialize("greeting");
//                dialog.show();
//                if(language == Locale.KOREAN)
//                    askQuestion("안녕하세요.\n무슨 일로 오셨나요?");
//                else
//                    askQuestion("Hi.\nHow can I help you?");
            }
        }
        prev_state = state;
    }

    @Override
    public void onConversationStatusChanged(int status, @NotNull String text) { //https://github.com/robotemi/sdk/wiki/Speech 참고

        if (prev_conv_status == 1 && status ==0 || prev_conv_status == 2 && !whileTalking && status == 0) {  // "대화가 끝났을 때"
            Log.d(TAG, "onConvStatusChanged " + status);
            mic.setVisibility(View.INVISIBLE);
            AsrText.setText("");
            TtsText.setText("");
            whileTalking = false;
            if(prev_conv_status == 1) dialog.dismiss();

//            // 만약 모든 상호작용이 끝났고, 대기 장소나 홈 베이스가 아니면.
//            if (!moving && !atStandby && !atHome) {
////                Log.d(TAG, "Countdown Started.");
//
//                // 10초 countdown, 0.4초마다 tick 확인.
//                new CountDownTimer(5000, 400) {
//                    @Override
//                    public void onTick(long millisUntilFinished) {
//                        // 새로운 interaction이 시작되거나 대기 장소, 홈 베이스가 되면 취소.
//                        if (whileTalking || moving || atStandby || atHome) {
//                            cancel();
////                            Log.d(TAG, "Countdown canceled because of new interaction.");
//                        }
//                    }
//                    @Override
//                    public void onFinish() { // 정상적으로 타이머가 끝나면, 대기 장소 자동 복귀.
//                        Log.d(TAG, "Countdown finished, return to standby.");
////                        robot.speak(TtsRequest.create("행정실 입구로 돌아갑니다.", true));
//                        robot.goTo("행정실 입구");
//                    }
//                }.start();
//            }


        }

        if (status == 1) {  // Listening user's voice
            Log.d(TAG, "onConversationStatusChanged: " + status + ": " + text);
            whileTalking = true;
            mic.setVisibility(View.VISIBLE);
            AsrText.setText(text);
        }
        if (status == 2) {  // NLP
            Log.d(TAG, "onConvStatusChanged " + status + ": " + text);
            whileTalking = true;
            AsrText.setText(text);
        }
        if (status == 3) {  // Playing TTS
            Log.d(TAG, "onConvStatusChanged " + status + ": " + text);
            whileTalking = true;
            mic.setVisibility(View.INVISIBLE);
        }
        prev_conv_status = status;
    }

    // START, CALCULATING, GOING, COMPLETE, ABORT.
    @Override
    public void onGoToLocationStatusChanged(@NotNull String location, @NotNull String status, int descriptionID, @NotNull String description) { //https://github.com/robotemi/sdk/wiki/Locations 참고
        atStandby = false; // 일단 뭔가 status 변화가 있으면, 인사 안하도록.
//        robot.setDetectionModeOn(false);
//        robot.setAutoReturnOn(true);
        atHome = false; // 이동 상태가 변하면 대기 장소나 홈 베이스에서 벗어나게 됨
        TtsText.setText("");
//        robot.setDetectionModeOn(false);
//        Log.d(TAG, "atStandby is false");

        if (status.equals(OnGoToLocationStatusChangedListener.START)) moving = true;

        else if (status.equals(OnGoToLocationStatusChangedListener.COMPLETE)){
            moving = false;

            // Standby에 도착한 경우, 다시 인사 하도록 변수 설정.
            if (location.equals("행정실 입구")) {
                atStandby = true;
//                robot.setAutoReturnOn(false);
//                robot.setDetectionModeOn(true, 0.5f);

//                robot.setDetectionModeOn(true);
//                Log.d(TAG, "atStandby is true");
            }

            else if (location.equals("home base")) {
                atHome = true;

                if (language == Locale.KOREAN)
                    speak("홈베이스에 도착했습니다.");
                else
                    speak("Arrived at home base.");
            }

            // 선생님 자리에 도착.
            else {
                if (language == Locale.KOREAN)
                    speak("도착했습니다. 다른 용무가 있으시면 말을 걸거나 버튼을 눌러주세요.");
                else
                    speak("Arrived at location. Please call me or press button to continue.");
            }
        }

        else if (status.equals(OnGoToLocationStatusChangedListener.ABORT)) {
            moving = false;
//            robot.askQuestion("네, 부르셨나요?");
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
}