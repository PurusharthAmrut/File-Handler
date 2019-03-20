package com.example.filehandler;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    EditText editText;
    TextView textView;
    Button makeButton, readButton;
    String filename = "MyFile.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.editText);
        textView = (TextView) findViewById(R.id.textView);
        makeButton = (Button) findViewById(R.id.makeButton);
        readButton = (Button) findViewById(R.id.readButton);

        makeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivityPermissionsDispatcher.writeFileWithPermissionCheck(MainActivity.this);
            }
        });

        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText(readFile());
            }
        });
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void writeFile(){
        if (isExternalStorageWritable() && checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            File textfile = new File(Environment.getExternalStorageDirectory(), filename);

            try{
                FileOutputStream fos = new FileOutputStream(textfile);
                fos.write(editText.getText().toString().getBytes());
                fos.close();

                Toast.makeText(this, "File saved", Toast.LENGTH_SHORT).show();
            }catch(Exception e){
                e.printStackTrace();
                Toast.makeText(this, "File not created", Toast.LENGTH_SHORT).show();
            }
        }
        else if (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            Toast.makeText(this, "External storage permission not received", Toast.LENGTH_SHORT).show();
        }
        else if (!isExternalStorageWritable()){
            Toast.makeText(this, "External storage not available", Toast.LENGTH_SHORT).show();
        }
    }

    public String readFile(){
        String text = "";
        try{
            File textfile = new File(Environment.getExternalStorageDirectory(), filename);
            FileInputStream fis = new FileInputStream(textfile);

            int size = fis.available();
            byte[] buffer = new byte[size];

            fis.read(buffer);
            fis.close();

            text = new String(buffer);
        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Error reading file", Toast.LENGTH_SHORT).show();
        }

        return text;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showRationaleForExternalStorage(final PermissionRequest permissionRequest){
        new AlertDialog.Builder(this)
                .setTitle("Permission needed")
                .setMessage("This permission is required to allow this app to store files in phone's external storage")
                .setPositiveButton("Grant permission", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        permissionRequest.proceed();
                    }
                })
                .setNegativeButton("Deny permission", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        permissionRequest.cancel();
                    }
                })
                .show();
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void onExternalStorageDenied(){
        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void onNeverAskAgain(){
        Toast.makeText(this, "Never asking again", Toast.LENGTH_SHORT).show();
    }

    public boolean checkPermission(String permission){
        int check = ContextCompat.checkSelfPermission(this, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    public boolean isExternalStorageWritable(){
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)){
            return true;
        }
        return false;
    }

/*    public boolean isExternalStorageReadable(){
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)){
            return true;
        }
        return false;
    }

    public File getAppStorageDirectory(String filename){
        File file = new File(Environment.getExternalStoragePublicDirectory(null), filename);
        if (!file.mkdirs()){
            Log.e(LOG_TAG, "Directory not created");
        }
        return file;
    }*/
}
