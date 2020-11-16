package com.seongnamc.sns_project.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.seongnamc.sns_project.R;
import com.seongnamc.sns_project.Userinfo;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static com.seongnamc.sns_project.Utility.INTENT_PATH;
import static com.seongnamc.sns_project.Utility.showToast;

public class FacialSymmetryHomeActivity extends BasicActivity {
    private static final String TAG = "FacialSymmetryHomeActivity";
    private String FacePath;
    private String FaceUri;
    private ImageView profileView;
    private RelativeLayout buttonsBackgroundLayout;
    private RelativeLayout facialSymmetryLoader;
    private ImageView faceView;
    private FirebaseUser user;
    private BufferedReader socketIn;
    private PrintWriter socketOut;
    private MyHandler myHandler;
    private MyThread myThread;
    private Socket socket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_facial_symmetry_home);
        setToolbarTitle("안면 분석");

        findViewById(R.id.pictureSelectButton).setOnClickListener(onClickListner);
        findViewById(R.id.measureButton).setOnClickListener(onClickListner);
        findViewById(R.id.captureSelect).setOnClickListener(onClickListner);
        findViewById(R.id.gallerySelcet).setOnClickListener(onClickListner);
        buttonsBackgroundLayout = findViewById(R.id.ButtonsBackgroundLayout);
        facialSymmetryLoader = findViewById(R.id.facialSymmetryLoader);
        faceView = findViewById(R.id.face_view);
        buttonsBackgroundLayout.setOnClickListener(onClickListner);

        user = FirebaseAuth.getInstance().getCurrentUser();
    }


    View.OnClickListener onClickListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.pictureSelectButton:
                    buttonsBackgroundLayout.setVisibility(View.VISIBLE);
                    break;
                case R.id.measureButton:

                    if(FacePath != null) {
                        Log.e("inininin","inmininin");
                        myStartActivity(FacialSymmetryResultActivity.class);
                        //storageUploader();
                        buttonsBackgroundLayout.setVisibility(View.GONE);
                    }
                    break;
                case R.id.gallerySelcet:
                    PostActivity(GalleryActivity.class,"image");
                    buttonsBackgroundLayout.setVisibility(View.GONE);
                    break;
                case R.id.captureSelect:
                    CameraActivity(CameraActivity.class, 1);
                    buttonsBackgroundLayout.setVisibility(View.GONE);
                    break;
            }
        }
    };

    private void PostActivity(Class c, String media) {
        Intent intent = new Intent(this, c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("media",media);
        startActivityForResult(intent, 0);
    }

    private void CameraActivity(Class c, int camera) {
        Intent intent = new Intent(this, c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("camera", camera);
        startActivityForResult(intent, 0);
    }
    private void myStartActivity(Class c) {
        Intent intent = new Intent(this, c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("facepath",FacePath);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 0:
                if(resultCode == Activity.RESULT_OK){
                    FacePath = data.getStringExtra(INTENT_PATH);
                    Log.e("path",FacePath+"");
                    /*Bitmap bmp = BitmapFactory.decodeFile(ProfilePath);
                    profileView.setImageBitmap(bmp);*/
                    Glide.with(this).load(FacePath).centerCrop().override(300).into(faceView);
                }
                break;
        }
    }

    private void storageUploader() {


        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();
        final StorageReference mountainImagesRef = storageRef.child("facial/" + user.getUid() + "/face.jpg");

        facialSymmetryLoader.setVisibility(View.VISIBLE);


        try {
            InputStream stream = new FileInputStream(new File(FacePath));

            UploadTask uploadTask = mountainImagesRef.putStream(stream);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();

                    }

                    return mountainImagesRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();


                        Log.e("성공", "성공 : " + downloadUri);
                        // Access a Cloud Firestore instance from your Activity

                        FaceUri = downloadUri.toString();

                         Socket_communication(user.getUid());


                    } else {

                        Log.e("실패", "회원 정보를 보내는데 실패하였습니다.");
                    }
                }
            });
        } catch (FileNotFoundException e) {
            Log.e("로그", "error" + e.toString());
        }

    }

    private void Socket_communication(String id){
        connect(id);
    }

    void connect(String id) {
        final String ip = "34.64.155.142";            // IP 번호
        int port = 1818;                          // port 번호
        Log.w("connect", "연결 하는중");
        // 받아오는거


        try{
            socket = new Socket(ip, port);
            socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            socketOut = new PrintWriter(socket.getOutputStream(), true);
        }catch(Exception e){
            e.printStackTrace();
        }

        //myHandler = new MyHandler();
        //myThread = new MyThread();
        //myThread.start();

        socketOut.print(id);
        socketOut.flush();
        String data = "";
        try {
            data = socketIn.readLine();
            Log.e("return",data+"");
            facialSymmetryLoader.setVisibility(View.GONE);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    class MyThread extends Thread{
        @Override
        public void run() {
            while(true){
                try{
                    System.out.println("Trying to read...");
                    String data = socketIn.readLine();
                    Log.e("return",data);
                    Message msg = myHandler.obtainMessage();
                    msg.obj = data;
                    myHandler.sendMessage(msg);
                    System.out.println(data);
                    socketOut.print(data);
                    socketOut.flush();
                    System.out.println("Message sent");
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    class MyHandler extends Handler {
        public void handleMessage(Message msg){
            //textView.setText(msg.obj.toString());
        }
    }
}
