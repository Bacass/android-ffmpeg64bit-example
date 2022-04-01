package cafe.adriel.androidaudioconverter.sample;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.arthenica.mobileffmpeg.FFmpeg;

import java.io.File;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;

public class MainActivity extends AppCompatActivity {

    private File selectedFile = null;
    private final int READ_REQUEST_CODE = 1085;
    private Uri selectedFileUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setBackgroundDrawable(
                    new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));
        }

        Util.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        Util.requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //readAudioFile();
                findAudioFile();
            }
        });
    }

    public void convertAudio(String file_path) {
        /**
         *  Update with a valid audio file!
         *  Supported formats: {@link AndroidAudioConverter.AudioFormat}
         */


        File audioFile = new File(file_path);
        Log.e("Lee", audioFile.getAbsolutePath());
        Toast.makeText(this, "Converting audio file...  ", Toast.LENGTH_SHORT).show();

        //AndroidAudioConverter.load(this, file_path);

        //String[] cmd = {"-i", file_path, "-ss", "0", "-t", "30"};
        FFmpeg.execute("-i " + file_path + " -ss 0 -t 30 " + file_path.replace(".wav", ".mp3"));

    }


    /**
     * 오디오파일을 선택한다.
     */
    private void findAudioFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("audio/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            selectedFileUri = data.getData();

            if (data.getData() == null) {
                Log.d("Lee", "data.getData() is null");
                return;
            } else {
                Log.d("Lee", "selectedFileUri = " + selectedFileUri.toString());

                String filePath = getAudioPath(selectedFileUri);
                Log.d("Lee", "getAudioPath = " + filePath);

                if (filePath == null) {
                    Toast.makeText(MainActivity.this, "저장정치 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "오디오가 선택되었습니다.", Toast.LENGTH_SHORT).show();

                    convertAudio(filePath);
                }
            }
        }
    }

    /**
     * uri 를 이용해서 파일의 path를 얻어낸다.
     * READ_EXTERNAL_STORAGE 권한이 없다면 null이 리턴된다.
     * @param uri
     * @return
     */
    public String getAudioPath(Uri uri){
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":")+1);
        cursor.close();

        cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Audio.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = null;
        if (cursor.getCount() != 0) {
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
        }
        cursor.close();

        return path;
    }

//이때 다른앱에서 생성한 이미지(Camera, Download)들을 선택하면 uri값이 리턴되지만 getImagePath()를 호출하면 null이 리턴된다.
//다른앱에서 생성한 이미지를 접근하려면 READ_EXTERNAL_STORAGE 권한이 있어야만 가능하다.



    //MediaStore를 이용한 공용공간의 파일접근은 아래의 방법을 사용한다.
    private void readAudioFile() {
        Uri externalUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.MIME_TYPE
        };

        Cursor cursor = getContentResolver().query(externalUri, projection, null, null, null);

        if (cursor == null || !cursor.moveToFirst()) {
            Log.e("Lee", "cursor null or cursor is empty");
            return;
        }


        Log.e("Lee", "cursor.getCount() : " + cursor.getCount());

        while (cursor.moveToNext()) {
            String contentUrl = externalUri.toString() + "/" + cursor.getString(0);

//            Log.e("Lee", "getPath : " + getPath(externalUri));
            Log.e("Lee", "contentUrl : " + contentUrl);
        }
    }

}