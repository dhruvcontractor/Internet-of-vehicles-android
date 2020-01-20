package com.acrcloud.Musicsharedplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import java.util.LinkedHashSet;

import com.acrcloud.rec.*;
import com.acrcloud.rec.utils.ACRCloudLogger;

import android.view.Window;

import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements IACRCloudListener, IACRCloudRadioMetadataListener {

    private final static String TAG = "MainActivity";
    Button scanButton;
    RadioGroup discoveryGroup;
    RadioButton discoveryR, discoveryPlusR;
    ListView scanListView;
    ArrayList<String> stringArrayList = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;
    BluetoothAdapter myAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final int REQUEST_ENABLE_BT = 1;

    private TextView mVolume, mResult, tv_time;

    private boolean mProcessing = false;
    private boolean mAutoRecognizing = false;
    private boolean initState = false;

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private boolean isPlaying = false;

    private String path = "";

    private long startTime = 0;
    private long stopTime = 0;

    private final int PRINT_MSG = 1001;

    private ACRCloudConfig mConfig = null;
    private ACRCloudClient mClient = null;

    WebView chWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().requestFeature(Window.FEATURE_PROGRESS);
        setContentView(R.layout.potrait_mode);


        path = Environment.getExternalStorageDirectory().toString()
                + "/acrcloud";
        Log.e(TAG, path);

        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }

        mVolume = (TextView) findViewById(R.id.volume);
        mResult = (TextView) findViewById(R.id.result);
        tv_time = (TextView) findViewById(R.id.time);
        chWebView = findViewById(R.id.chWebView);

        scanButton = findViewById(R.id.scanButton);
        scanListView = findViewById(R.id.scanListView);
        discoveryGroup = findViewById(R.id.discoveryGroup);
        discoveryR = findViewById(R.id.discoveryR);
        discoveryPlusR = findViewById(R.id.discoveryPlusR);


        try {
            scanButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    
                    myAdapter.startDiscovery();
                    //myAdapter.disable();
                }
            });
        } catch (Exception e) {


        }

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(myReceiver, intentFilter);

        arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_expandable_list_item_1, stringArrayList);
        scanListView.setAdapter(arrayAdapter);

       

        Switch sb = findViewById(R.id.auto_switch);
        sb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    openAutoRecognize();
                } else {
                    closeAutoRecognize();
                }
            }
        });

        verifyPermissions();

        this.mConfig = new ACRCloudConfig();

        this.mConfig.acrcloudListener = this;
        this.mConfig.context = this;

        // Please create project in "http://console.acrcloud.cn/service/avr".
        this.mConfig.host = "identify-us-west-2.acrcloud.com";
        //this.mConfig.accessKey = "170f5e2678bf19533cb69f8d5db5d1bc";
        this.mConfig.accessKey = "cf7a3bb1e4c76fddaf4cfcd25dfe5a49";
        this.mConfig.accessSecret = "VJfNCIDaETpZvrVxqamUSotuViKRCxijQB2gyhXL";

        // auto recognize access key
        //this.mConfig.hostAuto = "";
        //this.mConfig.accessKeyAuto = "";
        //this.mConfig.accessSecretAuto = "";

        this.mConfig.recorderConfig.rate = 8000;
        this.mConfig.recorderConfig.channels = 1;

        // If you do not need volume callback, you set it false.
        this.mConfig.recorderConfig.isVolumeCallback = true;

        this.mClient = new ACRCloudClient();
        ACRCloudLogger.setLog(true);

        this.initState = this.mClient.initWithConfig(this.mConfig);

        // Makes Progress bar Visible
        getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

        WebView mWebView = findViewById(R.id.chWebView); //This is the id you gave
        //to the WebView in the main.xml
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setSupportZoom(true);       //Zoom Control on web (You don't need this
        //if ROM supports Multi-Touch
        mWebView.getSettings().setBuiltInZoomControls(true); //Enable Multitouch if supported by ROM

        // Load URL
        // Bundle b = getIntent().getExtras();
        //String url = b.getString("url");
        final String url = "https://www.youtube.com/watch?v=";
        Log.d(TAG, "url " + url);
        if(savedInstanceState == null) {

            mWebView.loadUrl(url); //**************************************************************************************************************************
        }



        // Sets the Chrome Client, and defines the onProgressChanged
        // This makes the Progress bar be updated.
        //mWebView =  findViewById(R.id.webview);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                MainActivity.this.setTitle(view.getTitle());
            }
        });

        mWebView.setWebViewClient(new WebViewClient() {


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
                // return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                // TODO Auto-generated method stub
                super.onLoadResource(view, url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                //ImageView logoImageView = (ImageView)findViewById(R.id.logoimage);
                // logoImageView.setVisibility(View.GONE);
                Log.d(TAG, "view.getTitle() " + view.getTitle());
                System.out.println("title is =" + view.getTitle());


                
                MainActivity.this.setTitle(view.getTitle());

            }

        });


// List view palava
        scanListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                        // TextView textView = findViewById(R.id.textView);
                        String text = ((TextView) view).getText().toString();
                        // textView.setText(text);



                        //chWebView.loadUrl(launchUrl);
                        chWebView.loadUrl("https://www.youtube.com/results?search_query=" + text);
                        //Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(launchUrl)); //*************************************************************
                        //startActivity(browserIntent);

                    }
                }
        );
    }

    @Override
    protected void onSaveInstanceState(Bundle outState )
    {
        super.onSaveInstanceState(outState);
        chWebView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        chWebView.restoreState(savedInstanceState);
    }
	
    public String getUrlMethod(String urlString, String breakString){
        int index = urlString.indexOf(breakString);

        return urlString.substring(index+1, urlString.length());
    }

    BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String name = device.getName();



                String deviceString = "";
                if (name == null || name.equals("")) {
                    deviceString = name;
                    System.out.println("Name is null");
                    //Log.i("test","names are here");
                } else if (name.charAt(0) == "*".charAt(0)) { //Only scan bluetooth names with music information encoded with *
                    deviceString = name;


                    
                    stringArrayList.add(deviceString);

                    
                    arrayAdapter.notifyDataSetChanged();

                }

             

            }
        }
    };

    public void openAutoRecognize() {
        //String str = this.getString(R.string.suss);
        if (!mAutoRecognizing) {
            mAutoRecognizing = true;
            if (this.mClient == null || !this.mClient.runAutoRecognize()) {
                mAutoRecognizing = true;
                //str = this.getString(R.string.error);
            }
        }
       // Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    public void closeAutoRecognize() {
        //String str = this.getString(R.string.suss);
        if (mAutoRecognizing) {
            mAutoRecognizing = false;
            this.mClient.cancelAutoRecognize();
           // str = this.getString(R.string.error);
        }
        //Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    // callback IACRCloudRadioMetadataListener
    public void requestRadioMetadata() {
        String lat = "39.98";
        String lng = "116.29";
        List<String> freq = new ArrayList<>();
        freq.add("88.7");
        if (!this.mClient.requestRadioMetadataAsyn(lat, lng, freq,
                ACRCloudConfig.RadioType.FM, this)) {
            String str = this.getString(R.string.error);
            Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        }
    }

    public void reset() {
        tv_time.setText("");
        mResult.setText("");
        mProcessing = false;
    }

    @Override
    public void onResult(ACRCloudResult results) {
        this.reset();

	// If you want to save the record audio data, you can refer to the following codes.
	/*
	byte[] recordPcm = results.getRecordDataPCM();
        if (recordPcm != null) {
            byte[] recordWav = ACRCloudUtils.pcm2Wav(recordPcm, this.mConfig.recorderConfig.rate, this.mConfig.recorderConfig.channels);
            ACRCloudUtils.createFileWithByte(recordWav, path + "/" + "record.wav");
        }
	*/

        String result = results.getResult();
        String title_1 = "\n";
        String artist_1 = "\n";
        String tres = "\n";

        try {
            JSONObject j = new JSONObject(result);
            JSONObject j1 = j.getJSONObject("status");
            int j2 = j1.getInt("code");
            if(j2 == 0){
                JSONObject metadata = j.getJSONObject("metadata");
                //
                if (metadata.has("music")) {
                    System.out.println("Title is" + title_1);
                    System.out.println("Title is" + artist_1);
                    JSONArray musics = metadata.getJSONArray("music");
                    for(int i=0; i<musics.length(); i++) {
                        JSONObject tt = (JSONObject) musics.get(i);
                        String title = tt.getString("title");
                        JSONArray artistt = tt.getJSONArray("artists");
                        JSONObject art = (JSONObject) artistt.get(0);
                        String artist = art.getString("name");
                        tres = tres  + title + " " + artist + "\n";
						title_1 = title;
                        artist_1 = artist;
						break;
                    }

                }
                String newName = BTname.changeName("*" + title_1 + " " + artist_1); // Bluetooth name is encoded with music information.                
            }else{
                tres = result;
                String newName = BTname.changeName("No Music information found");
            }
        } catch (JSONException e) {
            tres = result;
            e.printStackTrace();
        }

        mResult.setText(tres);

        startTime = System.currentTimeMillis();
        System.out.println("title is =" + tres);
     }



    @Override
    public void onVolumeChanged(double volume) {
        long time = (System.currentTimeMillis() - startTime) / 1000;
        mVolume.setText(getResources().getString(R.string.volume) + volume + "\n\nTime: " + time + " s");
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS = {
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.RECORD_AUDIO
    };
    public void verifyPermissions() {
        for (int i=0; i<PERMISSIONS.length; i++) {
            int permission = ActivityCompat.checkSelfPermission(this, PERMISSIONS[i]);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS,
                        REQUEST_EXTERNAL_STORAGE);
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.e("MainActivity", "release");
        if (this.mClient != null) {
            this.mClient.release();
            this.initState = false;
            this.mClient = null;
        }
    }

    @Override
    public void onRadioMetadataResult(String s) {
        mResult.setText(s);
    }

    public void enableDiscoverablePlus() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else{ mBluetoothAdapter.disable();}

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
        startActivity(discoverableIntent);
    }

    public void enableDiscoverable() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
        startActivity(discoverableIntent);
    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId()) {
            case R.id.discoveryR:
                if (checked)
                    enableDiscoverable();
                break;
            case R.id.discoveryPlusR:
                if (checked)
                    enableDiscoverablePlus();
                break;
        }
    }
}
