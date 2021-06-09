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
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
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
import com.google.mlkit.vision.face.FaceContour;
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

    public static final String MY_PREFS_NAME = "MY_PREFS";
    public static final String BUTTON_CLICKS = "BUTTON_CLICKS";

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

            // 행정실 입구, 터치 끝난지 5초, 대화중이 아니면(이건 사실 필요 없음)
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

    // ViewPager 관련 변수 선언
    private ViewPager2 mPager;
    private MyAdapter pagerAdapter;
    private FragmentStateAdapter pagerAdapter_en;
    private int num_page = 2;
    private CircleIndicator3 mIndicator;

    public static int[] indices;

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

    //Canvas canvas;
    //Bitmap bitmap;

    //int width;
    //int height;

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
    protected void onStop() { // 테미가 대기모드로 전환시, 혹은 다른 activity가 호출되면 이 함수가 불림.
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
        super.onResume();
        Log.d("Test: ", "onResume");
        resetDisconnectTimer();

        // Refresh Fragment for indices, reset to page 1
        mPager.setAdapter(language == Locale.KOREAN? pagerAdapter: pagerAdapter_en);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Test: ", "onPause");
        stopDisconnectTimer();
    }

    Dialog dialog;
    AlertDialog alertDialog;
    boolean settingIsLocked;

    TextView AsrText;
    TextView TtsText;
    TextView Notice;

    @SuppressLint("SetTextI18n")
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
        Log.d("Test: ", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        robot = Robot.getInstance(); // get an instance of the robot in order to begin using its features.

        initV2Chatbot();

        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams") final View dialogView = inflater.inflate(R.layout.dialog, null);

        // 외부 화면 터치 시 자동으로 dismiss 됨 (대화 종료 1)
        dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(dialogView);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                stopDisconnectTimer();
                stopCamera();
            }
        });

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
                Log.d("Test: ", String.valueOf(false));
                resetDisconnectTimer();
            }
        });

        AsrText = dialogView.findViewById(R.id.AsrText);
        TtsText = dialogView.findViewById(R.id.TtsText);

        Resources resources = getResources();
        teacherAt319_Name = resources.getStringArray(R.array.teachers_name);

        whileTalking = false;
        Log.d("Test: ", String.valueOf(false));
        resetDisconnectTimer();

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

        settingIsLocked = false;

        // Alert dialog build
        initAlertDialog();

        ImageButton settingsButton = findViewById(R.id.settings_btn);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (settingIsLocked) {
                    stopDisconnectTimer();
                    alertDialog.show();
                }
                else openSettings();
            }
        });

        Button noticeButton = findViewById(R.id.notice_btn);
        noticeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 버튼이지만 계속 정보가 바뀌므로 agent와 연결할 수 밖에 없음
                QueryInput queryInput = QueryInput.newBuilder()
                        .setText(TextInput.newBuilder()
                                .setText("학부 공지")
                                .setLanguageCode("ko")) // 현재 웹훅은 한글만 가능
                        .build();

                new RequestJavaV2Task(MainActivity.this, session, sessionsClient, queryInput).execute();
            }
        });

        // 버튼 구성 편집 관련한 array 초기화, 처음 Install 이면 1 ~ 8로.
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        indices = stringToIntArray(prefs.getString("indices", "0,1,2,3,4,5,6,7"), ",", 8).clone();
        Log.d("Test: ", intArrayToString(indices, " "));

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
                        Log.d("Test: ", String.valueOf(true));
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
                        // 이동 완료 후에 speaking 할 때는 dialog dismiss 할 것이 없음.
                        if (!dialog.isShowing()) {
                            AsrText.setText("");
                            TtsText.setText("");
                            mic.setVisibility(View.INVISIBLE);

                            whileTalking = false;
                            Log.d("Test: ", String.valueOf(false));
                            //resetDisconnectTimer();
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
                        Log.d("Test: ", String.valueOf(true));
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

        /*Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;*/

        //bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        //canvas = new Canvas(bitmap);
        //canvas.drawColor(Color.TRANSPARENT);
        //((ImageView) findViewById(R.id.imageView)).setImageBitmap(bitmap);
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

    private void runCamera() {
        Log.d("Test: ", "runCamera");
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
        control.setZoomRatio(2.0f);
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

                    // YUV_420_888 - CameraX image style.
                    @SuppressLint("UnsafeExperimentalUsageError") Image mediaImage = image.getImage();
                    InputImage src = InputImage.fromMediaImage(mediaImage, image.getImageInfo().getRotationDegrees());

                    Task<List<Face>> task = detector.process(src);
                    task.addOnSuccessListener(
                            new OnSuccessListener<List<Face>>() {
                                @Override
                                public void onSuccess(List<Face> faces) {
                                    Log.d("Test: ", "number of faces = " + String.valueOf(faces.size()));

                                    if (faces.size() == 0) {
                                        // canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                                        // ((ImageView) findViewById(R.id.imageView)).setImageBitmap(bitmap);
                                        return;
                                    }

                                    // 화면에 잡힌 모든 얼굴에 대해서.
                                    for (int i = 0; i < faces.size(); i++) {

                                        Face face = faces.get(i);
                                        Rect bounds = face.getBoundingBox();
                                        FaceLandmark leftEye = face.getLandmark(FaceLandmark.LEFT_EYE);
                                        FaceLandmark rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE);

                                    /*List<PointF> contour = null;
                                    if (face.getContour(FaceContour.FACE) != null)
                                        contour = face.getContour(FaceContour.FACE).getPoints();
                                    */

                                        Log.d("Test: ", String.valueOf(bounds.top));
                                        Log.d("Test: ", String.valueOf(bounds.bottom));
                                        Log.d("Test: ", String.valueOf(bounds.left));
                                        Log.d("Test: ", String.valueOf(bounds.right));
                                        /////////////////////////////
                                    /*Paint paint = new Paint();
                                    paint.setColor(Color.RED);
                                    paint.setStyle(Paint.Style.STROKE);
                                    paint.setStrokeWidth(5f);

                                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                                    canvas.drawRect(new Rect(width - changeWidth(bounds.right), changeHeight(bounds.top), width - changeWidth(bounds.left), changeHeight(bounds.bottom)), paint);
                                    */
                                        /////////////////////////////

                                    /*paint.setColor(Color.GREEN);

                                    if (leftEye != null) {
                                        Log.d("Test: ", String.valueOf(leftEye.getPosition().x));
                                        Log.d("Test: ", String.valueOf(leftEye.getPosition().y));
                                    }

                                    if (rightEye != null) {
                                        Log.d("Test: ", String.valueOf(rightEye.getPosition().x));
                                        Log.d("Test: ", String.valueOf(rightEye.getPosition().y));
                                    }
                                    */

                                        //if (leftEye != null) canvas.drawPoint(width - changeWidth(leftEye.getPosition().x), changeHeight(leftEye.getPosition().y), paint);
                                        //if (rightEye != null) canvas.drawPoint(width - changeWidth(rightEye.getPosition().x), changeHeight(leftEye.getPosition().y), paint);
                                        /////////////////////////////
                                    /*
                                    paint.setColor(Color.MAGENTA);

                                    if (contour != null) {
                                        for (int i = 0; i < contour.size(); i++) {
                                            canvas.drawLine(
                                                    width - changeWidth(contour.get(i % contour.size()).x),
                                                    changeHeight(contour.get(i % contour.size()).y),
                                                    width - changeWidth(contour.get((i + 1) % contour.size()).x),
                                                    changeHeight(contour.get((i + 1) % contour.size()).y), paint);
                                        }
                                    }*/
                                        /////////////////////////////

                                        //((ImageView) findViewById(R.id.imageView)).setImageBitmap(bitmap);

                                        // 충분히 가까이 있고, 정면을 의식하고 있는 사용자의 경우에만 반기도록
                                        if ((bounds.top - bounds.bottom) * (bounds.left - bounds.right) > 20000 &&
                                                leftEye != null && rightEye != null &&
                                                (rightEye.getPosition().x - leftEye.getPosition().x) > 40) {

                                            stopCamera();
                                            if (!dialog.isShowing())
                                                dialog.show();

                                            // Dialogflow history 에 기록.
                                            sendRequest("greeting");
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

        // QUEUE_FLUSH: 초기화하고 새로 넣는 것, QUEUE_ADD: 현재 말하는거 다음으로 대기.
        ttsAskQuestion.speak(
                question,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "question");
    }

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

    private void speakWithLan(String korean, String english) {
        speak(language == Locale.KOREAN? korean: english);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        Log.d("Test: ", "onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null) // timeout 된 경우
            return;

        if (requestCode == 1) { // 선생님 이동
            if (resultCode == RESULT_OK) { // Yes
                String teacher = data.getStringExtra("teacher");
                String result = data.getStringExtra("result");
                if (result.equals("Yes")) {
                    Log.d(TAG, "onActivityResult: goTo " + teacher);

                    speakWithLan(teacher + " 자리로 이동합니다.", "Moving to location.");
                    robot.goTo(teacher);
                }
            }
        }

        // 설정 화면에서 메인 화면으로
        if (requestCode == 100) {
            Bundle bundle = data.getExtras(); // settingIsLocked null 불가능
            settingIsLocked = bundle.getBoolean("settingIsLocked");
        }
    }

    private void initV2Chatbot() {
        try {
            InputStream stream = getResources().openRawResource(R.raw.credential_reception_robot);
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
        QueryInput queryInput = QueryInput.newBuilder()
                .setText(TextInput.newBuilder()
                        .setText(input)
                        .setLanguageCode(language == Locale.KOREAN? "ko": "en"))
                .build();

        new RequestJavaV2Task(MainActivity.this, session, sessionsClient, queryInput).execute();
    }

    // RequestJavaV2Task 함수에 의해 불려지는 callback 함수
    public void callbackV2(DetectIntentResponse response) {
        if (response != null) {
            QueryResult result = response.getQueryResult();
            String botReply = result.getFulfillmentText();

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

            // 테미가 이동하는 경우
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

            // 팝업과 연관된 답변 (답변이 너무 길거나 추가 설명이 필요한 경우)
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

            // Depth로 설명된 Intent의 경우
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

            // 그 이외는 speak or ask botReply, exception of button
            else {
                if (botReply.contains("?"))
                    askQuestion(botReply);
                else {
                    if (!botReply.contains("button"))
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
        Log.d("Test: ", String.valueOf(true));
        stopDisconnectTimer();
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
            Log.d("Test: ", String.valueOf(true));
            stopDisconnectTimer();
            mic.setVisibility(View.VISIBLE);
            AsrText.setText(text);
        }

        if (status == 2) {  // NLP
            whileTalking = true;
            Log.d("Test: ", String.valueOf(true));
            stopDisconnectTimer();
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
        robot.setAutoReturnOn(true);
        TtsText.setText("");

        if (status.equals(OnGoToLocationStatusChangedListener.START)) moving = true;

        else if (status.equals(OnGoToLocationStatusChangedListener.COMPLETE)) {
            moving = false;

            // Standby에 도착한 경우, 다시 인사 하도록 변수 설정.
            if (location.equals("행정실 입구")) {
                atStandby = true;
                resetDisconnectTimer();
            } else if (location.equals("home base")) {
                atHome = true;
                robot.setAutoReturnOn(false);
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

                    // 키보드가 사라지고, alertDialog 창이 닫히는 것을 순차적으로 하기 위해 (UI의 간결함)
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