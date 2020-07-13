package top.gtf35.savetxtdemo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class MainActivity extends Activity {

    private final FrameLayout.LayoutParams MATCH_WIDTH_WRAP_WEIGHT_LP = new FrameLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
    );
    public static final int OPEN_REQUEST_CODE = 1;
    public static final int WRITE_REQUEST_CODE = 2;

    private Uri mFileUri = null;
    private EditText mContentET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout rootView = new LinearLayout(this);
        rootView.setOrientation(LinearLayout.VERTICAL);

        mContentET = new EditText(this);
        mContentET.setHint("选择文件后这里显示内容");
        LinearLayout.LayoutParams contextETLP = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1
        );

        Button submitBtn = new Button(this);
        submitBtn.setText("保存");
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUriFromEditText(mFileUri, mContentET);
            }
        });

        Button openBtn = new Button(this);
        openBtn.setText("打开");
        openBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFile();
            }
        });

        Button createBtn = new Button(this);
        createBtn.setText("创建");
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createFile();
            }
        });

        rootView.addView(mContentET, contextETLP);
        rootView.addView(submitBtn, MATCH_WIDTH_WRAP_WEIGHT_LP);
        rootView.addView(openBtn, MATCH_WIDTH_WRAP_WEIGHT_LP);
        rootView.addView(createBtn, MATCH_WIDTH_WRAP_WEIGHT_LP);

        setContentView(rootView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 在界面可见时判断是否应该显示 Uri
        if (mFileUri == null) return;
        showUri2TextView(mFileUri, mContentET);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        // 判断来源并解析出 Uri
        if (requestCode == OPEN_REQUEST_CODE || requestCode == WRITE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && resultData != null) {
                mFileUri = resultData.getData();
            }
        }
    }

    /**
     * 拉起系统 SAF 框架打开文件
     */
    private void openFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        startActivityForResult(intent, OPEN_REQUEST_CODE);
    }

    /**
     * 拉起系统 SAF 框架创建文件
     */
    private void createFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        startActivityForResult(intent, WRITE_REQUEST_CODE);
    }

    /**
     * 将 EditText 的内容保存到 Uri 中
     *
     * @param uri 写出纯文本文件的 Uri
     * @param editText 源 EditText
     */
    private void saveUriFromEditText(Uri uri, EditText editText) {
        String contentText = editText.getText().toString();
        if (TextUtils.isEmpty(contentText)) {
            Toast.makeText(MainActivity.this, "empty input", Toast.LENGTH_SHORT).show();
            return;
        }
        if (uri == null) {
            Toast.makeText(MainActivity.this, "have not select file success", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean result = saveTextFromUri(uri, contentText);
        Toast.makeText(MainActivity.this, result? "success": "failed, please see log", Toast.LENGTH_SHORT).show();
    }

    /**
     * 将 Uri 指向的纯文本文件显示到 TextView 上
     *
     * @param uri 读入的 Uri
     * @param textView 被显示的 TextView
     */
    private void showUri2TextView(Uri uri, TextView textView) {
        try {
            String txt = readTextFromUri(uri);
            if (TextUtils.isEmpty(txt)) txt = "";
            textView.setText(txt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 从 Uri 读取文本文件
     *
     * @param uri 读入 Uri
     * @return 纯文本文件的内容
     * @throws IOException 读取过程出错
     */
    private String readTextFromUri(Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream =
                     getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 保存文字到 Uri
     *
     * @param uri 写出 Uri
     * @param text 要保存的文字
     * @return true 如果保存成功
     */
    private boolean saveTextFromUri(Uri uri, String text) {
        boolean success = true;
        try {
            ParcelFileDescriptor pfd = getContentResolver().
                    openFileDescriptor(uri, "w");
            if (pfd == null) throw new NullPointerException("pfd is null");
            FileOutputStream fileOutputStream =
                    new FileOutputStream(pfd.getFileDescriptor());
            fileOutputStream.write(text.getBytes());
            fileOutputStream.close();
            pfd.close();
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
        return success;
    }
}