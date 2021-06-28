package com.sigma.temitest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.constants.SdkConstants;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.robotemi.sdk.listeners.OnConversationStatusChangedListener;
import com.robotemi.sdk.listeners.OnDetectionDataChangedListener;
import com.robotemi.sdk.listeners.OnDetectionStateChangedListener;
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;
import com.robotemi.sdk.listeners.OnLocationsUpdatedListener;
import com.robotemi.sdk.listeners.OnUserInteractionChangedListener;
import com.robotemi.sdk.model.DetectionData;

import me.relex.circleindicator.CircleIndicator3;

import static android.speech.tts.TextToSpeech.ERROR;

public class MainActivity extends MyWifiBaseActivity implements
        Robot.AsrListener,
        Robot.WakeupWordListener,
        //Robot.ConversationViewAttachesListener,
        //OnDetectionStateChangedListener,
        OnConversationStatusChangedListener,
        //OnUserInteractionChangedListener,
        OnGoToLocationStatusChangedListener {
    
    public static final long ENDOFINTERACTION_TIMEOUT = 20000;
    private final Handler endOfInteractionHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            return true;
        }
    });

    private final Runnable endOfInteractionCallback = new Runnable() {
        @Override
        public void run() {
            if (alertDialog != null && alertDialog.isShowing()) {
                View view = alertDialog.getCurrentFocus();
                closeKeyboard(view);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        alertDialog.dismiss();
                    }
                }, 300);
            }

            // 행정실 입구, 터치 끝난지 5초, 대화중이 아니면 (이건 사실 필요 없음) 사용자를 기다리는 얼굴인식 재개
            if (!whileTalking && atStandby) {
                Log.d("Test: ", "Turn on the camera!");
                runCamera();
            }
        }
    };

    public void resetDisconnectTimer() {
        endOfInteractionHandler.removeCallbacks(endOfInteractionCallback);
        endOfInteractionHandler.postDelayed(endOfInteractionCallback, ENDOFINTERACTION_TIMEOUT);
    }

    public void stopDisconnectTimer() {
        endOfInteractionHandler.removeCallbacks(endOfInteractionCallback);
    }

    @Override
    public void onUserInteraction() {
        Log.d("Test: ", "onUserInteraction");
        resetDisconnectTimer();
        stopCamera();
    }

    public static final String MY_PREFS_NAME = "MY_PREFS"; // 버튼 구성을 저장하는 변수
    public static final String BUTTON_CLICKS = "BUTTON_CLICKS"; // 각 버튼이 눌린 횟수들을 저장하는 변수 

    public static int[] indices; // 현재 화면의 버튼 구성을 나타내는 변수, 길이 8
    
    private boolean whileTalking; // 사용자와 대화 중인지 확인하는 변수
    private boolean atStandby; // 대기 장소(행정실 입구) 인지 확인하는 변수

    // 안드로이드 TTS에 필요한 변수들
    private TextToSpeech ttsSpeak;
    private TextToSpeech ttsAskQuestion;
    private Locale language; // 현재 테미의 언어를 확인하는 변수

    private Robot robot; // 테미 SDK 연동하는 변수
    
    // 다이얼로그플로우 연동에 필요한 변수들
    private SessionsClient sessionsClient;
    private SessionName session;
    
    int prev_conv_status = 0; // OnConversationStatusChanged 함수와 관련된 변수

    private static final String TAG = MainActivity.class.getSimpleName();
    private final String uuid = UUID.randomUUID().toString();

    // 선생님 자리로의 이동에 필요한 변수들
    private String[] teacherAt319_Name;
    private int indexOfTeacher; // 테미가 안내해주려는 선생님이 배열에서 몇 번째인지 나타내는 변수
    
    Dialog dialog; // 대화 화면에 대한 변수
    AlertDialog alertDialog; // 로그인 화면에 대한 변수
    boolean settingIsLocked; // 현재 설정 창이 잠겨있는지 확인하는 변수

    // UI 관련 변수들
    ImageView mic;
    Button start_talking;
    Button stop_talking;
    Button language_btn;
    TextView AsrText;
    TextView TtsText;
    TextView Notice;

    // ViewPager 관련 변수 선언
    private ViewPager2 mPager;
    private MyAdapter pagerAdapter;
    private FragmentStateAdapter pagerAdapter_en;
    private final int num_page = 2;
    private CircleIndicator3 mIndicator;

    // 카메라 제어 및 얼굴인식 관련 변수들
    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;

    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private ProcessCameraProvider cameraProvider;
    private CameraSelector cameraSelector;

    private FaceDetector detector;
    private ImageAnalysis imageAnalysis;
    private ImageAnalysis.Analyzer analyzer;
    private int frameRateBySec = 0;
    
    @Override
    protected void onStart() {
        Log.d("Test: ", "onStart");
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
    protected void onStop() { // 메인 엑티비티에서 벗어나면 호출되는 함수
        Log.d("Test: ", "onStop");
        super.onStop();
        
        Robot.getInstance().removeAsrListener(this);
        Robot.getInstance().removeWakeupWordListener(this);
        //Robot.getInstance().removeConversationViewAttachesListenerListener(this);
        //Robot.getInstance().removeOnDetectionStateChangedListener(this);
        Robot.getInstance().removeOnConversationStatusChangedListener(this);
        Robot.getInstance().removeOnGoToLocationStatusChangedListener(this);
        //Robot.getInstance().removeOnUserInteractionChangedListener(this);

        stopDisconnectTimer();
        stopCamera();
    }

    @Override
    protected void onResume() {
        Log.d("Test: ", "onResume");
        super.onResume();
        
        resetDisconnectTimer();
        // Refresh Fragment for indices, reset to page 1
        mPager.setAdapter(language == Locale.KOREAN? pagerAdapter: pagerAdapter_en);
    }

    @Override
    protected void onPause() {
        Log.d("Test: ", "onPause");
        super.onPause();
        
        stopDisconnectTimer();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Test: ", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        robot = Robot.getInstance(); // Get an instance of the robot in order to begin using its features
        initV2Chatbot();

        // dialog 변수 초기화
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams") final View dialogView = inflater.inflate(R.layout.dialog, null);
        
        dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setDimAmount(0.85f); // 조금 더 짙은 배경을 위해서 필요한 함수
        dialog.setContentView(dialogView); // 외부 화면 터치 시 자동으로 dismiss 됨 (대화 종료 1)
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                stopDisconnectTimer();
                stopCamera();
            }
        });
        
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) { // Dialog dismiss 시 대호 종료를 위해 취할 조취들
                robot.finishConversation();
                ttsSpeak.stop();
                ttsAskQuestion.stop();

                AsrText.setText("");
                TtsText.setText("");
                mic.setVisibility(View.INVISIBLE);

                whileTalking = false;
                Log.d("Test: ", "whileTalking - false");
                resetDisconnectTimer();
            }
        });

        initAlertDialog();
        settingIsLocked = false; // 처음 앱이 실행될 때는 설정 창이 잠긴 상태
        
        mic = dialogView.findViewById(R.id.mic);
        start_talking = findViewById(R.id.start_talking_btn);
        stop_talking = findViewById(R.id.stop_talking_btn);
        language_btn = findViewById(R.id.language_btn);
        AsrText = dialogView.findViewById(R.id.AsrText);
        TtsText = dialogView.findViewById(R.id.TtsText);
        Notice = findViewById(R.id.notice);
        Notice.setText(R.string.notice);
        
        Resources resources = getResources();
        teacherAt319_Name = resources.getStringArray(R.array.teachers_name); // String 파일에서 가져오는 값 (보안을 위해서)
        
        whileTalking = false; // 처음 앱이 실행될 때는 대화 중이 아닌 상태
        Log.d("Test: ", "whileTalking - false");
        
        atStandby = true; // 처음 앱이 실행되는 위치도 대기 장소로 간주, 따라서 타이머도 시작
        resetDisconnectTimer();

        // 설정 버튼
        ImageButton settingsButton = findViewById(R.id.settings_btn);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (settingIsLocked) {
                    stopDisconnectTimer();
                    alertDialog.show();
                } else openSettings();
            }
        });

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
        
        // 학부 최근 공지 버튼
        Button noticeButton = findViewById(R.id.notice_btn);
        noticeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 정보가 수시로 변하므로 다이얼로그플로우와 연동
                QueryInput queryInput = QueryInput.newBuilder()
                        .setText(TextInput.newBuilder()
                                .setText("학부 공지")
                                .setLanguageCode("ko")) // 현재 관련 인텐트는 한글 옵션만 가능
                        .build();

                new RequestJavaV2Task(MainActivity.this, session, sessionsClient, queryInput).execute();
            }
        });

        // 체온 측정 버튼 (현재 사용되지 않음)
        ImageButton FLIR = findViewById(R.id.thermometer_btn); 
        FLIR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThermoCheck();
            }
        });

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        indices = stringToIntArray(prefs.getString("indices", "0,1,2,3,4,5,6,7"), ",", 8).clone(); // 처음 변수가 초기화되는 경우 1에서 8로 순차적으로 저장
        Log.d("Test: ", intArrayToString(indices, " "));

        // ViewPager2
        mPager = findViewById(R.id.viewpager);
        // Adapter (for fragment activity)
        pagerAdapter = new MyAdapter(this, num_page);
        pagerAdapter_en = new MyAdapterEn(this, num_page);
        mPager.setAdapter(pagerAdapter);
        // Indicator
        mIndicator = findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        mIndicator.createIndicators(num_page, 0);
        // ViewPager Setting
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

        language = Locale.KOREAN; // 처음 앱이 실행될 때는 한국어로 설정

        // 안드로이드 TTS 초기화
        // 단순 대답과, 질문에 대한 세팅을 다르게 해야 하기 때문에 두 변수로 설정
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
                        Log.d("Test: ", "whileTalking - true");
                        stopDisconnectTimer();
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
                        // 이동 완료 후에 말하는 경우 Dialog가 나타나지 않음
                        if (!dialog.isShowing()) {
                            AsrText.setText("");
                            TtsText.setText("");
                            mic.setVisibility(View.INVISIBLE);

                            whileTalking = false;
                            Log.d("Test: ", "whileTalking - false");
                        }

                        dialog.dismiss(); // onDismiss 함수에 동작이 이미 추가된 상태
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
                        Log.d("Test: ", "whileTalking - true");
                        stopDisconnectTimer();
                        mic.setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void onDone(String s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // onConversationStatusChanged 함수가 작동하기 위해서는 wakeup만으로 안되고, askquestion이 호출되어야 하기 때문에,
                        // 참고로 onConversationStatusChanged 함수가 필요한 이유 - Dialog 창에 실시간 대화 업데이트가 필요함
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

        // 카메라 제어 및 얼굴인식 초기화
        if (!hasCameraPermission())
            requestPermission();
        else {
            FaceDetectorOptions options =
                    new FaceDetectorOptions.Builder()
                            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                            .build();
            detector = FaceDetection.getClient(options);

            previewView = findViewById(R.id.previewView);
            cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build();
            cameraProviderFuture = ProcessCameraProvider.getInstance(this);

            try { cameraProvider = cameraProviderFuture.get(); }
            catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

            initImageAnalysis();
        }
    }

    // 여기부터는 보조 및 listener 함수들 정의

    @SuppressLint("SetTextI18n")
    public void setLanguage(View view) {
        if (language == Locale.KOREAN) { // 영어로 바꿔야 하는 경우
            setEnglish(view);
            language_btn.setText("Korean");
        }

        else { // 한국어로 바꿔야 하는 경우
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

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(
                this,
                CAMERA_PERMISSION,
                CAMERA_REQUEST_CODE
        );
    }

    // 카메라 제어 드라이버 함수
    private void runCamera() {
        Log.d("Test: ", "runCamera");
        sendRequest("log: runCamera"); // 다이얼로그플로우에 기록
        cameraProvider.unbindAll();
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                runCameraSub();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void runCameraSub() {
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.createSurfaceProvider());
        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), analyzer);

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysis, preview);
        CameraControl control = camera.getCameraControl();
        control.setZoomRatio(2.0f); // 마스크 안끼는 시기가 오면 빼도 되는 함수 (마스크를 낀 사람의 경우 너무 얼굴이 작으면 인식조차 되지 않기 때문에)
    }

    private void stopCamera() {
        Log.d("Test: ", "stopCamera");
        cameraProvider.unbindAll();
    }

    private void initImageAnalysis() {
        Log.d("Test: ", "initImageAnalysis");
        imageAnalysis = new ImageAnalysis.Builder().setTargetResolution(new Size(1920, 1200))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();

        analyzer = new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                frameRateBySec++;
                if (frameRateBySec % 5 == 0) {
                    frameRateBySec = 0;

                    // YUV_420_888 - CameraX image style
                    @SuppressLint("UnsafeExperimentalUsageError") Image mediaImage = image.getImage();
                    InputImage src = InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());

                    Task<List<Face>> task = detector.process(src);
                    task.addOnSuccessListener(
                            new OnSuccessListener<List<Face>>() {
                                @Override
                                public void onSuccess(List<Face> faces) {
                                    Log.d("Test: ", "number of faces = " + String.valueOf(faces.size()));

                                    if (faces.size() == 0)
                                        return;

                                    // 화면에 잡힌 모든 얼굴에 대해서 고려
                                    for (int i = 0; i < faces.size(); i++) {
                                        Face face = faces.get(i);
                                        Rect bounds = face.getBoundingBox();
                                        FaceLandmark leftEye = face.getLandmark(FaceLandmark.LEFT_EYE);
                                        FaceLandmark rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE);

                                        // 얼굴의 위치 확인하는 로그
                                        // Log.d("Test: ", String.valueOf(bounds.top));
                                        // Log.d("Test: ", String.valueOf(bounds.bottom));
                                        // Log.d("Test: ", String.valueOf(bounds.left));
                                        // Log.d("Test: ", String.valueOf(bounds.right));

                                        // 얼굴이 가까이 있고, 정면을 보고 있는 사용자의 경우에만 반기도록
                                        if ((bounds.top - bounds.bottom) * (bounds.left - bounds.right) > 18000 &&
                                                leftEye != null && rightEye != null &&
                                                (rightEye.getPosition().x - leftEye.getPosition().x) > 35) {

                                            stopCamera();
                                            if (!dialog.isShowing())
                                                dialog.show();
                                            sendRequest("greeting"); // 다이얼로그플로우에 얼굴인식 기록
                                        }
                                    }
                                }
                            });

                    task.addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("Test: ", e.toString());
                                }
                            });

                    task.addOnCompleteListener(
                            new OnCompleteListener<List<Face>>() {
                                @Override
                                public void onComplete(@NonNull Task<List<Face>> task) {
                                    image.close();
                                }
                            });
                }

                else image.close();
            }
        };
    }

    private void askQuestion(String question) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TtsText.setText(question);
            }
        });

        ttsAskQuestion.speak(
                question,
                TextToSpeech.QUEUE_FLUSH, // QUEUE_FLUSH: 초기화하고 새로 넣는 경우, QUEUE_ADD: 현재 말하는 것 다음에 하도록 대기하는 경우
                null,
                "question");
    }

    // 언어를 고려하지 않아도 되는 함수
    private void askQuestionWithLan(String korean, String english) {
        askQuestion(language == Locale.KOREAN? korean: english);
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

    // 언어를 고려하지 않아도 되는 함수
    private void speakWithLan(String korean, String english) {
        speak(language == Locale.KOREAN? korean: english);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        Log.d("Test: ", "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) // Timeout 으로 인해 메인 엑티비티로 돌아온 경우
            return;

        if (requestCode == 1) { // 선생님 자리로 이동하는 팝업 이후
            if (resultCode == RESULT_OK) { // 사용자가 OK 한 경우
                String teacher = data.getStringExtra("teacher");
                String result = data.getStringExtra("result");
                if (result.equals("Yes")) {
                    Log.d(TAG, "onActivityResult: goTo " + teacher);

                    speakWithLan(teacher + " 자리로 이동합니다.", "Moving to location.");
                    robot.goTo(teacher);
                }
            }
        }

        // 설정 화면에서 메인 엑티비티로 돌아온 경우
        if (requestCode == 100) {
            Bundle bundle = data.getExtras(); // 가지고 온 값은 아무것도 없기에는 불가능
            settingIsLocked = bundle.getBoolean("settingIsLocked");
        }
    }

    private void initV2Chatbot() {
        try {
            InputStream stream = getResources().openRawResource(R.raw.credential_reception_robot); // 다이얼로그플로우 연동된 구글 클라우드 프로젝트의 키
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

    // 다이얼로그플로우에 대화 요청을 보내는 함수
    public void sendRequest(String input) {
        QueryInput queryInput = QueryInput.newBuilder()
                .setText(TextInput.newBuilder()
                        .setText(input)
                        .setLanguageCode(language == Locale.KOREAN? "ko": "en"))
                .build();

        new RequestJavaV2Task(MainActivity.this, session, sessionsClient, queryInput).execute();
    }

    // RequestJavaV2Task 클래스에 의해 불려지는 콜백 함수
    public void callbackV2(DetectIntentResponse response) {
        if (response != null) {
            QueryResult result = response.getQueryResult();
            String botReply = result.getFulfillmentText();

            // Intent name 저장 (테미 이동과 관련)
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

            // 행정실에 계신 선생님에 대해서 물어본 경우 선생님 이름 저장하는 부분
            if (check) {
                for (int i = 0; i < teacherAt319_Name.length; i++) {
                    if (botReply.contains(teacherAt319_Name[i])) {
                        indexOfTeacher = i;
                        Log.d(TAG, teacherAt319_Name[i]);
                        break;
                    }
                }
            }

            // 테미가 이동해야 하는 경우
            if (intentName.equals("MoveTemi-Yes")) {
                speakWithLan(teacherAt319_Name[indexOfTeacher] + " 선생님 자리로 이동합니다.", botReply);
                robot.goTo(teacherAt319_Name[indexOfTeacher] + " 선생님");
            } else if (intentName.equals("GoToHomeBase")) {
                speak(botReply);
                robot.goTo("home base");
            } else if (intentName.equals("GoToStandBy")) {
                speak(botReply);
                robot.goTo("행정실 입구");
            }

            /* else if (intentName.equals("TemperatureCheck")) {
                ThermoCheck(); */

            // 팝업과 연관된 답변 (즉 답변이 길거나 추가 설명이 필요한 경우)
            else if (intentName.equals("Question_Weather")) {
                speak(botReply);
                Intent intent = new Intent(MainActivity.this, Web.class);
                intent.putExtra("url", "https://weather.naver.com/today/09620735");
                startActivity(intent);
            } else if (intentName.equals("Question_FoodMenu")) {
                speak("오늘의 학식 메뉴를 알려드립니다.");
                Intent intent = new Intent(MainActivity.this, PopupActivity2.class);
                intent.putExtra("text", botReply);
                startActivity(intent);
            } else if (intentName.equals("Question_Access")) {
                speakWithLan("출입등록 방법을 안내해드립니다.", "Follow the instructions to register access.");
                Intent intent = new Intent(MainActivity.this,
                        language == Locale.KOREAN? PopupActivity3.class: PopupActivity3En.class);
                intent.putExtra("access", botReply);
                startActivity(intent);
            } else if (intentName.equals("Question_Certificate")) {
                speakWithLan("증명서 발급 방법을 안내해드립니다.", "Follow the instructions to print your certificate.");
                Intent intent = new Intent(MainActivity.this,
                        language == Locale.KOREAN? PopupActivity3.class: PopupActivity3En.class);
                intent.putExtra("mysnu", botReply);
                startActivity(intent);
            } else if (intentName.equals("Question_Locker")) {
                speakWithLan("사물함 신청 방법을 안내해드립니다.", "Follow the instructions to apply for a locker.");
                Intent intent = new Intent(MainActivity.this,
                        language == Locale.KOREAN? PopupActivity3.class: PopupActivity3En.class);
                intent.putExtra("locker", botReply);
                startActivity(intent);
            }

            else if (intentName.equals("current_scholarship")) {
                speakWithLan("현재 신청 가능한 장학금을 알려드립니다.", "These are the scholarships you can currently apply for.");
                Intent intent = new Intent(MainActivity.this,
                        language == Locale.KOREAN ? PopupActivity2.class : PopupActivity2En.class);
                intent.putExtra("text", botReply);
                startActivity(intent);
            } else if (intentName.equals("Question_Scholarship_Specific")) {
                if (botReply.equals("실패"))
                    speak("현재 신청이 불가한 장학금이거나,\n오류가 발생했습니다.");
                else {
                    speakWithLan("요청하신 장학금 관련 답변입니다.", "Please read the following.");
                    Intent intent = new Intent(MainActivity.this, Web.class);
                    intent.putExtra("url", botReply);
                    startActivity(intent);
                }
            } else if (intentName.contains("Question_Scholarship")) {
                speakWithLan("요청하신 장학금 관련 답변입니다.", "Please read the following.");
                Intent intent = new Intent(MainActivity.this,
                        language == Locale.KOREAN? PopupActivity2.class: PopupActivity2En.class);
                intent.putExtra("text", botReply);
                startActivity(intent);
            } else if (intentName.equals("Question_영문성명변경")) {
                speakWithLan("영문성명변경 관련 답변입니다.", "Please read the following.");
                Intent intent = new Intent(MainActivity.this,
                        language == Locale.KOREAN? PopupActivity2.class: PopupActivity2En.class);
                intent.putExtra("text", botReply);
                startActivity(intent);
            } else if (intentName.equals("Question_전공학습도우미")) {
                speakWithLan("전공학습도우미 스케줄 관련 답변입니다.", "Please read the following.");
                Intent intent = new Intent(MainActivity.this,
                        language == Locale.KOREAN? PopupActivity2.class: PopupActivity2En.class);
                intent.putExtra("text", botReply);
                startActivity(intent);
            } else if (intentName.equals("Question_Notice")) {
                speakWithLan("최근 학부 공지입니다.\n자세한 내용은 학부 홈페이지를 방문해주세요.", "Please read the following.");
                Intent intent = new Intent(MainActivity.this,
                        language == Locale.KOREAN? PopupActivity2.class: PopupActivity2En.class);
                intent.putExtra("text", botReply);
                startActivity(intent);
            }

            // 그 이외는 speak or ask botReply 변수
            else {
                if (botReply.contains("?"))
                    askQuestion(botReply);
                else {
                    if (!botReply.contains("button") && !botReply.contains("log")) // 버튼 인텐트가 불린 경우는 예외
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
        speakWithLan("체온 측정 중입니다.",
                "Checking temperature.");

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

    // 아래 오버라이드하는 Listener 함수들은 https://github.com/robotemi/sdk/wiki/ 를 참고
    @Override
    public void onWakeupWord(String wakeupWord, int direction) { // 테미의 WakeupWord 가 불린 경우
        // "헤이 테미" 로 시작된 대화는 onConversationStatusChanged 함수가 호출됨
        robot.stopMovement(); // 이동 중지

        if (!dialog.isShowing())
            dialog.show();

        whileTalking = true;
        Log.d("Test: ", "whileTalking - true");
        stopDisconnectTimer();
        mic.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAsrResult(final @NonNull String asrResult) {
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
    public void onConversationStatusChanged(int status, @NotNull String text) {
        Log.d(TAG, "Test: Conversation status has turned to number " + status);

        // 듣기만 하다가 말이 없어서 대화가 끝나는 경우 (대화 종료 4)
        if (prev_conv_status == 1 && status == 0)
            dialog.dismiss();

        if (status == 1) {  // 대화를 듣는 상태
            whileTalking = true;
            Log.d("Test: ", "whileTalking - true");
            stopDisconnectTimer();
            mic.setVisibility(View.VISIBLE);
            AsrText.setText(text);
        }

        if (status == 2) {  // NLP 하는 상태
            whileTalking = true;
            Log.d("Test: ", "whileTalking - true");
            stopDisconnectTimer();
            AsrText.setText(text);
        }

        // if (status == 3)
        prev_conv_status = status;
    }

    @Override
    public void onGoToLocationStatusChanged(@NotNull String location, @NotNull String status, int descriptionID, @NotNull String description) { //https://github.com/robotemi/sdk/wiki/Locations 참고
        Log.d("Test: ", location + " " + status);

        atStandby = false; // 일단 뭔가 Status 변화가 있으면, 대기 장소를 벗어낫다고 가정
        robot.setAutoReturnOn(true); // 테미의 설정 권한을 ON 시켜주기
        TtsText.setText("");

        if (status.equals(OnGoToLocationStatusChangedListener.COMPLETE)) {
            if (location.equals("행정실 입구")) {
                atStandby = true; // Standby에 도착한 경우, 대기 장소에 도착했다고 변수 설정
                resetDisconnectTimer();
            } else if (location.equals("home base")) {
                robot.setAutoReturnOn(false);
                speakWithLan("홈베이스에 도착했습니다.", "Arrived at home base.");
            }

            // 선생님 자리에 도착한 경우 (Dialog 는 나타나지 않도록)
            else
                speakWithLan("도착했습니다. 다른 용무가 있으시면 말을 걸거나 버튼을 눌러주세요.",
                        "Arrived at location. Please call me or press button to continue.");

        }
    }

    // 로그인 창 초기화하는 함수
    void initAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.login_activity, null);
        builder.setView(view);

        final Button submit = (Button) view.findViewById(R.id.enter);
        final Button cancel = (Button) view.findViewById(R.id.cancel);
        final EditText password = (EditText) view.findViewById(R.id.password);
        final TextView text = (TextView) view.findViewById(R.id.textView);

        alertDialog = builder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setCanceledOnTouchOutside(false);

        Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.shake);

        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String strPassword = password.getText().toString();

                if (strPassword.equals(getString(R.string.password))) {
                    closeKeyboard(view);

                    // 시간 차의 이유: 키보드가 사라지고, 창이 닫히는 것을 순차적으로 하기 위해 (UI의 간결함)
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            alertDialog.dismiss();
                            openSettings();
                        }
                    }, 300);
                }

                else {
                    view.startAnimation(anim);
                    password.setText("");
                    text.setText(R.string.try_again);
                    text.setTextColor(getResources().getColor(R.color.red, null));
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                closeKeyboard(view);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        alertDialog.dismiss();
                        resetDisconnectTimer();
                    }
                }, 300);
            }
        });

        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                password.setText("");
                text.setText(R.string.enter_password);
                text.setTextColor(getResources().getColor(R.color.black, null));
            }
        });
    }

    private void closeKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void openSettings() {
        sendRequest("log: openSettings"); // 다이얼로그플로우에 기록
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        intent.putExtra("settingIsLocked", settingIsLocked);
        startActivityForResult(intent, 100);
        overridePendingTransition(R.anim.slide_out, 0);
    }

    public static String intArrayToString(int[] arr, String delimiter) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            str.append(arr[i]).append(delimiter);
        }

        return str.toString();
    }

    public static int[] stringToIntArray(String input, String delimiter, int length) {
        int[] arr = new int[length];

        StringTokenizer st = new StringTokenizer(input, delimiter);
        for (int i = 0; i < arr.length; i++) {
            arr[i] = Integer.parseInt(st.nextToken());
        }

        return arr;
    }
}