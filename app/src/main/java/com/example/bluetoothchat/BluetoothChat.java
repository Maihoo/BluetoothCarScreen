package com.example.bluetoothchat;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.StaticLayout;
import android.text.method.ScrollingMovementMethod;
import android.util.AndroidException;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class BluetoothChat extends Activity {
    // Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_TEXT = 6;
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    // Layout Views
    private TextView mTitle, tv, scrollText;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private BluetoothChatService mChatService = null;
    private Bitmap imageToDraw;
    private ImageView iv;
    private int pastX, pastY;
    private Button mSendButton, msendTextButton, mSizeButton, mColorButton, mClearButton, mConnectButton, mUndoButton, mRedoButton, mAddButton;
    private EditText editText;
    private Paint paint = new Paint();
    private int size = 30;
    String white = "#ffffff";
    private int color = Color.parseColor(white);
    ArrayList<Bitmap> hist = new ArrayList<Bitmap>();
    private int pointer = 0;
    private boolean textScrolling = false;


    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB_MR2)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration config = getResources().getConfiguration();
        int w = config.smallestScreenWidthDp;
        if(w > 500) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        }

        if(D) Log.e(TAG, "+++ ON CREATE +++");
        // Set up the window layout
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.main);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
        // Set up the custom title
        mTitle = (TextView) findViewById(R.id.title_left_text);
        //mTitle.setText("BTC");
        mTitle = (TextView) findViewById(R.id.title_right_text);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        iv = (ImageView) findViewById(R.id.imageView);
        Drawable dr = getResources().getDrawable(R.drawable.blackground);
        imageToDraw = ((BitmapDrawable) dr).getBitmap();

        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(30);

        try{getActionBar().hide();}
        catch (Exception e) {}

        scrollText      = (TextView) findViewById(R.id.scrollText   );
        editText        = (EditText) findViewById(R.id.editText     );

        mSizeButton     = (Button) findViewById(R.id.button_size    );
        msendTextButton = (Button) findViewById(R.id.button_sendText);
        mColorButton    = (Button) findViewById(R.id.button_color   );
        mClearButton    = (Button) findViewById(R.id.button_clear   );
        mConnectButton  = (Button) findViewById(R.id.button_connect );
        mUndoButton     = (Button) findViewById(R.id.button_undo    );
        mRedoButton     = (Button) findViewById(R.id.button_redo    );
        mAddButton      = (Button) findViewById(R.id.button_add     );

        mSizeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), PopSize.class);
                startActivityForResult(i, 999);
            }
        });

        Context context = this;
        tv = findViewById(R.id.colorTextView);

        mColorButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), PopColor.class);
                startActivityForResult(i, 888);
            }
        });

        msendTextButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                byte[] st = editText.getText().toString().getBytes(StandardCharsets.UTF_8);
                mChatService.write(st);
            }
        });

        mClearButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                imageToDraw = ((BitmapDrawable) dr).getBitmap();
                ImageView iv = findViewById(R.id.imageView);
                iv.setImageBitmap(imageToDraw);
            }
        });

        mConnectButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent serverIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            }
        });

        mUndoButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if(pointer > 0) {
                    if(pointer == hist.size()) {
                        hist.add(imageToDraw);
                    }
                    pointer--;
                    imageToDraw = hist.get(pointer);
                    iv.setImageBitmap(imageToDraw);
                    mRedoButton.setAlpha((float) 1.0);
                } else {
                    mUndoButton.setAlpha((float) 0.3);
                }
            }
        });

        mRedoButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if(pointer < hist.size()-1) {
                    Log.e("before","pointer: " + pointer + " size: " + hist.size());
                    pointer++;
                    imageToDraw = hist.get(pointer);
                    iv.setImageBitmap(imageToDraw);
                    if(pointer >= hist.size()-1) {
                        mRedoButton.setAlpha((float) 0.3);
                    }
                    Log.e("after","pointer: " + pointer + " size: " + hist.size());
                } else {
                    mRedoButton.setAlpha((float) 0.3);
                }
            }
        });

        mAddButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

            }
        });

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else {
            if (mChatService == null) setupChat();
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }
    private void setupChat() {
        Log.d(TAG, "setupChat()");
        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);


        // Initialize the compose field with a listener for the return key

        // Initialize the send button with a listener that for click events
        mSendButton = (Button) findViewById(R.id.button_send);
        mSendButton.setOnClickListener(new OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                sendMessage();
            }
        });
        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler, this);
        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }
    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }
    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "-- ON STOP --");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (mChatService != null) mChatService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }
    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    private void sendMessage() {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        imageToDraw.compress(Bitmap.CompressFormat.JPEG, 40, bytes);
        byte[] image = bytes.toByteArray();

        mChatService.write(image);
        //mOutStringBuffer.setLength(0);
    }
    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener =
            new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                    // If the action is a key-up event on the return key, send the message
                    if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                        String message = view.getText().toString();
                        sendMessage();
                    }
                    if(D) Log.i(TAG, "END onEditorAction");
                    return true;
                }
            };
    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            //mTitle.setText(R.string.title_connected_to);
                            //mTitle.append(mConnectedDeviceName);
                            mConversationArrayAdapter.clear();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            //mTitle.setText(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            //mTitle.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);

                    break;
                case MESSAGE_READ:

                    mConversationArrayAdapter.clear();
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);

                    Log.e("BLUE", "String: " + readMessage);

                    if(readMessage.substring(0, 3).equalsIgnoreCase("pic")) {
                        if(readMessage.substring(3).equalsIgnoreCase("")) { textScrolling = false; }
                        textScrolling = true;
                        scrollText.setText(readMessage.substring(3));
                        scrollText.setAlpha((float) 1.0);

                        new Thread() {
                            @Override
                            public void run() {
                                float textWidth = StaticLayout.getDesiredWidth(scrollText.getText().toString(), scrollText.getPaint());
                                DisplayMetrics dm = new DisplayMetrics();
                                scrollText.setX( dm.widthPixels );
                                while(textScrolling) {

                                    float x = scrollText.getX();
                                    x -= 0.5;
                                    scrollText.setX(x);
                                    if(-x > textWidth ) { scrollText.setX( dm.widthPixels ); }
                                    try {
                                        Thread.sleep(1);
                                    } catch (Exception e) {}
                                }
                            }
                        }.start();

                        break;
                    }

                    //Log.e("Hmm", "I recieved a message of length: " + readBuf.length);

                    Bitmap bmp = BitmapFactory.decodeByteArray(readBuf, 0, readBuf.length);

                    iv.setImageBitmap(bmp);

                    Log.e("Hmm", "I set the image..");

                    mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp= Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD_MR1)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.e(TAG, "onActivityResult " + resultCode);

        switch (requestCode) {
            case 999:
                if(resultCode == RESULT_OK) {
                    size = data.getIntExtra("size", 2);
                    tv.setTextSize(size/2);
                }
                break;
            case 888:
                if(resultCode == RESULT_OK) {
                    color = data.getIntExtra("color", Color.parseColor(white));
                    tv.setTextColor(color);
                }
                break;
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    mChatService.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan:
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(this, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                return true;
            case R.id.discoverable:
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public boolean onTouchEvent(MotionEvent event) {

        paint.setColor(color);
        paint.setStrokeWidth(size);


        int[] viewCoords = new int[2];
        iv.getLocationOnScreen(viewCoords);

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                hist.add(imageToDraw);
                pointer ++;
                ArrayList<Bitmap> temp = new ArrayList<Bitmap>();
                for(int i = 0; i < pointer; i++) {
                    temp.add(hist.get(i));
                }
                hist = temp;
                break;
            case MotionEvent.ACTION_MOVE:

                int imageX = 0;
                int imageY = 0;

                if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
                    int touchX = (int) event.getX();
                    //touchX = (int) ((touchX * (width/1920))    +    ((1920 - 1920*(width/1920) )));
                    int touchY = (int) event.getY();

                    DisplayMetrics dm = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getRealMetrics(dm);

                    imageX = dm.widthPixels - (int) ((float) touchX*((float) 1920/(float) findViewById(R.id.imageView).getWidth( ))) +50;
                    imageY =                  (int) ((float) touchY*((float) 1080/(float) findViewById(R.id.imageView).getHeight())) -0;
                } else {

                    int touchX = (int) event.getY();
                    //touchX = (int) ((touchX * (width/1920))    +    ((1920 - 1920*(width/1920) )));
                    int touchY = (int) event.getX();

                    DisplayMetrics dm = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getRealMetrics(dm);

                    imageX = (int) ((float) touchX*((float) 1920/(float) findViewById(R.id.imageView).getWidth())) -0;
                    imageY = (int) ((float) touchY*((float) 1080/(float) findViewById(R.id.imageView).getHeight())) + 3*(83 -viewCoords[0]);

                    Log.e("wichtig", "x: " + viewCoords[0] + " y: " + viewCoords[1]);

                }

                Bitmap mutableBitmap = imageToDraw.copy(Bitmap.Config.ARGB_8888, true);
                Canvas canvas = new Canvas(mutableBitmap);
                canvas.drawCircle(imageY, imageX, size/2, paint);

                if(pastX != 0 && pastY != 0) {
                        canvas.drawLine(pastY, pastX, imageY, imageX, paint);
                }
                pastX = imageX;
                pastY = imageY;

                imageToDraw = mutableBitmap.copy(Bitmap.Config.ARGB_8888, true);
                mUndoButton.setAlpha((float) 1.0);
                mRedoButton.setAlpha((float) 0.3);

                iv = findViewById(R.id.imageView);
                iv.setImageBitmap(imageToDraw);

                break;
            case MotionEvent.ACTION_UP:
                pastX = 0;
                pastY = 0;
                break;
        }
        return true;
    }
}

