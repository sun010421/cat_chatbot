package com.sigma.temitest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
        //Robot.ConversationViewAttachesListener,
        //OnDetectionStateChangedListener,
        OnConversationStatusChangedListener,
        //OnUserInteractionChangedListener,
        OnGoToLocationStatusChangedListener {

    private boolean whileTalking; // 사용자와 대화 중인지.
    private boolean atStandby; // 대기 장소인지 여부.
    private boolean atHome; // 홈 베이스인지 여부.
    private boolean moving; // 이동중 여부.
    private boolean interacting; // 상호작용 여부.
    private boolean touching; // 터치 여부.
    int detect_state; // 사람 감지 여부.

    private TextToSpeech ttsSpeak;
    private TextToSpeech ttsAskQuestion;
    private Locale language;

    private Robot robot;

    private SessionsClient sessionsClient;
    private SessionName session;

    int prev_conv_status = 0;

    private static final String TAG = MainActivity.class.getSimpleName();
    private String uuid = UUID.randomUUID().toString();

    private String[] teacherAt319_Name;
    private int indexOfTeacher; // 테미가 안내해주려는 선생님이 배열에서 몇번째인지 나타냄.

    ImageView mic;  // User가 말할 때 나타나는 마이크 모양 아이콘
    Button start_talking;
    Button stop_talking;
    Button language_btn;

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
        //Robot.getInstance().addConversationViewAttachesListenerListener(this);
        //Robot.getInstance().addOnDetectionStateChangedListener(this);
        Robot.getInstance().addOnConversationStatusChangedListener(this);
        Robot.getInstance().addOnGoToLocationStatusChangedListener(this);
        //Robot.getInstance().addOnUserInteractionChangedListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Robot.getInstance().removeAsrListener(this);
        Robot.getInstance().removeWakeupWordListener(this);
        //Robot.getInstance().removeConversationViewAttachesListenerListener(this);
        //Robot.getInstance().removeOnDetectionStateChangedListener(this);
        Robot.getInstance().removeOnConversationStatusChangedListener(this);
        Robot.getInstance().removeOnGoToLocationStatusChangedListener(this);
        //Robot.getInstance().removeOnUserInteractionChangedListener(this);
    }

    Dialog dialog;
    TextView AsrText;
    TextView TtsText;
    TextView Notice;

    public void setLanguage(View view) {
        if (language == Locale.KOREAN) { // 영어로 바뀌는 경우
            setEnglish(view);
            language_btn.setText("Korean");
        }

        else { // 한글로 바뀌는 경우
            setKorean(view);
            language_btn.setText("English");
        }
    }

    public void setKorean(View view) {
        mPager.setAdapter(pagerAdapter);
        language = Locale.KOREAN;
        ttsSpeak.setLanguage(language);
        ttsAskQuestion.setLanguage(language);
        start_talking.setText(R.string.start_talking);
        stop_talking.setText(R.string.stop_talking);
        Notice.setText(R.string.notice);
    }

    public void setEnglish(View view) {
        mPager.setAdapter(pagerAdapter_en);
        language = Locale.ENGLISH;
        ttsSpeak.setLanguage(language);
        ttsAskQuestion.setLanguage(language);
        start_talking.setText(R.string.start_talking_en);
        stop_talking.setText(R.string.stop_talking_en);
        Notice.setText(R.string.notice_en);
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

        // 외부 화면 터치 시 자동으로 dismiss 됨 (대화 종료 1)
        dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(dialogView);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) { // dialog 창이 없어지고, end of conversation 관련한 모든 조취.
                robot.finishConversation();
                ttsSpeak.stop();
                ttsAskQuestion.stop();

                AsrText.setText("");
                TtsText.setText("");
                mic.setVisibility(View.INVISIBLE);

                whileTalking = false;
            }
        });

        AsrText = dialogView.findViewById(R.id.AsrText);
        TtsText = dialogView.findViewById(R.id.TtsText);

        Resources resources = getResources();
        teacherAt319_Name = resources.getStringArray(R.array.teachers_name);

        whileTalking = false;
        moving = false;
        atStandby = true; // 처음 앱이 실행되면, 위치와 관계 없이 사용자를 감지

        mic = dialogView.findViewById(R.id.mic);
        start_talking = findViewById(R.id.start_talking_btn);
        stop_talking = findViewById(R.id.stop_talking_btn);
        Notice = findViewById(R.id.notice);
        Notice.setText(R.string.notice);
        language_btn = findViewById(R.id.language_btn);

        // 대화 시작 버튼
        start_talking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!dialog.isShowing())
                    dialog.show();

                askQuestionWithLan("말씀해주세요.", "I'm listening.");
            }
        });

        // 음성/대화 중지 버튼 (현재 사용되지 않음, 대화 종료 2)
        stop_talking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
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
                speakWithLan("홈 베이스로 이동합니다.", "Moving to home base.");
                robot.goTo("home base");
            }
        });

        ImageButton standbyButton = findViewById(R.id.standby_btn); // 행정실 입구로 이동 버튼
        standbyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakWithLan("행정실 입구로 이동합니다.", "Moving to entrance of office.");
                robot.goTo("행정실 입구");
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
        mIndicator.createIndicators(num_page, 0);
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
                mIndicator.animatePageSelected(position % num_page);
            }

        });

        final float pageMargin = getResources().getDimensionPixelOffset(R.dimen.pageMargin);
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

            // 대화 종료 3
            @Override
            public void onDone(String s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 이동 완료 후에 speaking 할 때는 dialog dismiss 할 것이 없음.
                        if (!dialog.isShowing()) {
                            AsrText.setText("");
                            TtsText.setText("");
                            mic.setVisibility(View.INVISIBLE);
                        }

                        dialog.dismiss();
                    }
                });
            }

            @Override
            public void onError(String s) {}

            @Override
            public void onRangeStart(String utteranceId, int start, int end, int frame) {}
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
                        // conversation status changed 함수가 작동하기 위해서는 (askquestion에서, 말로는 잘 됨) wakeup만으로 안되고, askquestion이 호출되어야 함.
                        // conversation status changed 함수가 필요한 이유 - 실시간 사용자 대답 업데이트를 위해서.
                        robot.askQuestion(".");
                        robot.wakeup();
                    }
                });
            }

            @Override
            public void onError(String s){}

            @Override
            public void onRangeStart(String utteranceId, int start, int end, int frame) {}
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
        ttsAskQuestion.speak(
                question,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "question");
    }

    private void askQuestionWithLan(String korean, String english){
        if (language == Locale.KOREAN)
            askQuestion(korean);
        else
            askQuestion(english);
    }

    private void speak(String sentence) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TtsText.setText(sentence);
            }
        });

        ttsSpeak.speak(
                sentence,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "speak");
    }

    private void speakWithLan(String korean, String english){
        if (language == Locale.KOREAN)
            speak(korean);
        else
            speak(english);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                //데이터 받기
                String teacher = data.getStringExtra("teacher");
                String result = data.getStringExtra("result");
                if (result.equals("Yes")) {
                    Log.d(TAG, "onActivityResult: goTo " + teacher);

                    speakWithLan(teacher + " 자리로 이동합니다.", "Moving to location.");
                    robot.goTo(teacher);
                }
            }
        }
    }

    private void initV2Chatbot() {
        try {
            InputStream stream = getResources().openRawResource(R.raw.test_agent_credentials);
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream);
            String projectId = ((ServiceAccountCredentials) credentials).getProjectId();

            SessionsSettings.Builder settingsBuilder = SessionsSettings.newBuilder();
            SessionsSettings sessionsSettings = settingsBuilder.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();
            sessionsClient = SessionsClient.create(sessionsSettings);
            session = SessionName.of(projectId, uuid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendRequest(String input) {
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
                for (int i = 0; i < num; i++) {
                    String[] contextName = result.getOutputContextsList().get(i).getName().split("/");
                    if (contextName[contextName.length - 1].contains("movetemi")) {
                        check = true;
                        break;
                    }
                }
            }

            // 행정실(319호)에 계신 선생님에 대해서 사용자가 물어본 경우(= movetemi context가 있는 intent의 경우), 선생님 이름 저장.
            if (check) {
                // Log.d(TAG, "319호 선생님에 대한 질문");
                for (int i = 0; i < teacherAt319_Name.length; i++) {
                    if (botReply.contains(teacherAt319_Name[i])) {
                        indexOfTeacher = i;
                        Log.d(TAG, teacherAt319_Name[i]);
                        break;
                    }
                }
            }

            // 테미가 이동하는 경우.
            if (intentName.equals("MoveTemi-Yes")) {
                speakWithLan(teacherAt319_Name[indexOfTeacher] + " 선생님 자리로 이동합니다.", botReply);
                robot.goTo(teacherAt319_Name[indexOfTeacher] + " 선생님");
            }

            else if (intentName.equals("GoToHomeBase")) {
                speak(botReply);
                robot.goTo("home base");
            } else if (intentName.equals("GoToStandBy")) {
                speak(botReply);
                robot.goTo("행정실 입구");
            } else if (intentName.equals("TemperatureCheck")) { // 체온 측정하는 코드, Dialogflow에서 'Temperature_Check' 인텐트에 의한 리스폰스
                ThermoCheck();
            } else if (botReply.contains("온도")) {
                speak(botReply);
                Intent intent = new Intent(MainActivity.this, Web.class);
                intent.putExtra("url", "https://weather.naver.com/today/09620735");
                startActivity(intent);
            } else if (botReply.contains("소반")) {
                speak("오늘의 학식 메뉴를 알려드립니다.");
                Intent intent = new Intent(MainActivity.this, PopupActivity2.class);
                intent.putExtra("text", botReply);
                startActivity(intent);
            }

            else if (intentName.equals("Question_Access")) {
                speakWithLan("출입등록 방법을 안내해드립니다.", "Follow the instructions to register access.");
                Intent intent;
                if (language == Locale.KOREAN)
                    intent = new Intent(MainActivity.this, PopupActivity3.class);
                else
                    intent = new Intent(MainActivity.this, PopupActivity3En.class);
                intent.putExtra("access", botReply);
                startActivity(intent);
            }

            else if (intentName.equals("Question_Certificate")) {
                speakWithLan("증명서 발급 방법을 안내해드립니다.", "Follow the instructions to print your certificate.");
                Intent intent;
                if (language == Locale.KOREAN)
                    intent = new Intent(MainActivity.this, PopupActivity3.class);
                else
                    intent = new Intent(MainActivity.this, PopupActivity3En.class);
                intent.putExtra("mysnu", botReply);
                startActivity(intent);
            }

            else if (intentName.equals("Question_Locker")) {
                speakWithLan("사물함 신청 방법을 안내해드립니다.", "Follow the instructions to apply for a locker.");
                Intent intent;
                if (language == Locale.KOREAN)
                    intent = new Intent(MainActivity.this, PopupActivity3.class);
                else
                    intent = new Intent(MainActivity.this, PopupActivity3En.class);
                intent.putExtra("locker", botReply);
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
        } else
            speakWithLan("전송 오류가 있었습니다. 다시 시도해주시기 바랍니다.",
                    "Error in sending request. Try again.");
    }

    public double send(String data) { // 체온 측정 관련 함수
        int port = 5001;

        try {
            Socket sock = new Socket("192.168.0.56", port);

            ObjectOutputStream outstream = new ObjectOutputStream(sock.getOutputStream());
            outstream.writeObject(data);
            outstream.flush();

            ObjectInputStream instream = new ObjectInputStream(sock.getInputStream());
            String input = (String) instream.readObject();
            Log.d(TAG, "Temperature: " + input);
            sock.close();

            return Double.valueOf(input);

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void ThermoCheck() {
        speakWithLan("체온 측정 중입니다.", "Checking temperature.");

        new Thread(new Runnable() {
            @Override
            public void run() {
                double temp = send("start_measure");
                temp = Math.round(temp * 100.0) / 100.0;
                if (temp < 38.0)
                    speakWithLan("체온이 " + temp + "º로 측정되었습니다.", "You are " + temp + "degrees.");
                else if (temp < 50.0)
                    speakWithLan(temp + "º. 위험 체온입니다. 출입을 불허합니다.",
                            "You are " + temp + "degrees. Please do not enter.");
                else
                    speakWithLan("측정을 방해하는 물체가 있습니다. 다른 각도에서 다시 시도해주시기 바랍니다.",
                            "There is something hindering the process. Please try again in another angle.");
            }
        }).start();
    }

    @Override
    public void onWakeupWord(String wakeupWord, int direction) {
        // 이동 중간에 "헤이 테미"로 부른 경우, 이동 중지.
        robot.stopMovement();

        // "헤이 테미"로 대화 시작하는 경우 고려, 그 이외의 대화 시작은 버튼으로.
        if (!dialog.isShowing())
            dialog.show();

        whileTalking = true;
        mic.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAsrResult(final @NonNull String asrResult) { // https://github.com/robotemi/sdk/wiki/Speech 참고
        sendRequest(asrResult);

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
    public void onConversationStatusChanged(int status, @NotNull String text) { // https://github.com/robotemi/sdk/wiki/Speech 참고
        Log.d(TAG, "Test: Conversation status has turned to number " + status + ".");

        // listening 만 하다가 입력이 없어서 끝난 경우 (대화 종료 4)
        if (prev_conv_status == 1 && status == 0)
            dialog.dismiss();

        if (status == 1) {  // Listening user's voice
            whileTalking = true;
            mic.setVisibility(View.VISIBLE);
            AsrText.setText(text);
        }

        if (status == 2) {  // NLP
            whileTalking = true;
            AsrText.setText(text);
        }

        // if (status == 3)
        prev_conv_status = status;
    }

    // START, CALCULATING, GOING, COMPLETE, ABORT.
    @Override
    public void onGoToLocationStatusChanged(@NotNull String location, @NotNull String status, int descriptionID, @NotNull String description) { //https://github.com/robotemi/sdk/wiki/Locations 참고
        Log.d("Test: ", location + " " + status);

        atStandby = false; // 일단 뭔가 status 변화가 있으면, 인사 안하도록.
        atHome = false; // 이동 상태가 변하면 대기 장소나 홈 베이스에서 벗어나게 됨
        //robot.setAutoReturnOn(true);
        TtsText.setText("");

        if (status.equals(OnGoToLocationStatusChangedListener.START)) moving = true;

        else if (status.equals(OnGoToLocationStatusChangedListener.COMPLETE)) {
            moving = false;

            // Standby에 도착한 경우, 다시 인사 하도록 변수 설정.
            if (location.equals("행정실 입구")) {
                atStandby = true;
            } else if (location.equals("home base")) {
                Log.d("Test: ", "ok");
                atHome = true;
                robot.setAutoReturnOn(false);
                Log.d("Test: ", String.valueOf(robot.isAutoReturnOn())); // 제대로 동작.
                speakWithLan("홈베이스에 도착했습니다.", "Arrived at home base.");

            }

            // 선생님 자리에 도착.
            else
                speakWithLan("도착했습니다. 다른 용무가 있으시면 말을 걸거나 버튼을 눌러주세요.",
                        "Arrived at location. Please call me or press button to continue.");

        } else if (status.equals(OnGoToLocationStatusChangedListener.ABORT)) {
            moving = false;
        }
    }
}