package tw.com.ei.mydrivetest;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    private TelephonyManager tmgr;
    private MyListener myListener;
    private int lastState = -1;
    private ImageView img;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //取得權限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS,
                            Manifest.permission.READ_CALL_LOG,
                            Manifest.permission.READ_PHONE_STATE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        } else {
            init();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        init();
    }

    private void init() {
        img = (ImageView) findViewById(R.id.img);

        tmgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        String deviceid = tmgr.getDeviceId();//取得IMEI
        Log.i("simon", "IMEI:" + deviceid);
        String num = tmgr.getLine1Number();
        Log.i("simon", "phone:" + num);
        String IMSI = tmgr.getSubscriberId();
        Log.i("simon", "IMSI:" + IMSI);

        myListener = new MyListener();
        tmgr.listen(myListener, PhoneStateListener.LISTEN_CALL_STATE);

        String name = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
        String number = ContactsContract.CommonDataKinds.Phone.NUMBER;
        ContentResolver cr = getContentResolver();  // SQLiteDatabase => db
        Cursor c = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (c.moveToNext()) {
            String f1 = c.getString(c.getColumnIndex(name));
            String f2 = c.getString(c.getColumnIndex(number));
            Log.i("simon", f1 + " : " + f2);
        }
        c.close();

        c = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        c.moveToLast();
        String file = c.getString(c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
        Log.i("simon", file);

        Bitmap bmp = BitmapFactory.decodeFile(file);
        img.setImageBitmap(bmp);


    }

    private class MyListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            Log.i("simon", "got it");
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    if (state != lastState) {
                        Log.i("simon", "idle");//閒置
                        lastState = state;
                    }
                    break;

                case TelephonyManager.CALL_STATE_RINGING:
                    if (state != lastState) {
                        Log.i("simon", "ring:" + incomingNumber);//incomingNumber來電電話
                        reListen();
                        lastState = state;
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.i("simon", "offhook");
                    break;
                default:
                    Log.i("simon", "other:" + state);
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }

    private void reListen() {
        tmgr.listen(myListener, PhoneStateListener.LISTEN_CALL_STATE);

    }
}

