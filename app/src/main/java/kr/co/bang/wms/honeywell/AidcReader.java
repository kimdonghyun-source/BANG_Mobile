package kr.co.bang.wms.honeywell;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.InvalidScannerNameException;
import com.honeywell.aidc.ScannerNotClaimedException;
import com.honeywell.aidc.ScannerUnavailableException;
import com.honeywell.aidc.TriggerStateChangeEvent;
import com.honeywell.aidc.UnsupportedPropertyException;

import java.util.HashMap;
import java.util.Map;

import kr.co.bang.wms.common.Utils;

public class AidcReader {
    private static AidcReader mAidcReader = null;

    private BarcodeReader barcodeReader = null;
    private AidcManager manager = null;
    private Handler mHandler = null;

    public AidcReader(){

    }
    BarcodeReader.BarcodeListener barcodeListener = new BarcodeReader.BarcodeListener() {
        @Override
        public void onBarcodeEvent(BarcodeReadEvent event) {
            Log.d("JeLib","Barcode data: " + event.getBarcodeData());
            Log.d("JeLib","Character Set: " + event.getCharset());
            Log.d("JeLib","Code ID: " + event.getCodeId());
            Log.d("JeLib","AIM ID: " + event.getAimId());
            Log.d("JeLib","Timestamp: " + event.getTimestamp());

            String barcode = event.getBarcodeData();
            if(mHandler!=null && Utils.nullString(barcode,"").length() > 0){
                Message msg = mHandler.obtainMessage();
                msg.what=   1;
                msg.obj =   event;
                mHandler.sendMessage(msg);
            }
        }

        @Override
        public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {
            Log.d("JeLib","onFailureEvent: " + barcodeFailureEvent.getSource());
        }
    };

    BarcodeReader.TriggerListener triggerListener = new BarcodeReader.TriggerListener() {
        @Override
        public void onTriggerEvent(TriggerStateChangeEvent event) {
            try {
                // only handle trigger presses
                // turn on/off aimer, illumination and decoding
                barcodeReader.aim(event.getState());
                barcodeReader.light(event.getState());
                barcodeReader.decode(event.getState());

            } catch (ScannerNotClaimedException e) {

            } catch (ScannerUnavailableException e) {

            }
        }
    };

    public void init(final Context context){
        AidcManager.create(context, new AidcManager.CreatedCallback() {
            @Override
            public void onCreated(AidcManager aidcManager) {
                manager = aidcManager;
                try{
                    barcodeReader = manager.createBarcodeReader();

                    barcodeReader.addBarcodeListener(barcodeListener);
                    // set the trigger mode to client control
                    try {
                        barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                                BarcodeReader.TRIGGER_CONTROL_MODE_CLIENT_CONTROL);
                    } catch (UnsupportedPropertyException e) {
                        Toast.makeText(context, "Failed to apply properties", Toast.LENGTH_SHORT).show();
                    }
                    // register trigger state change listener
                    barcodeReader.addTriggerListener(triggerListener);

                    Map<String, Object> properties = new HashMap<String, Object>();
                    // Set Symbologies On/Off
                    properties.put(BarcodeReader.PROPERTY_CODE_128_ENABLED, true);
                    properties.put(BarcodeReader.PROPERTY_GS1_128_ENABLED, true);
                    properties.put(BarcodeReader.PROPERTY_QR_CODE_ENABLED, true);
                    properties.put(BarcodeReader.PROPERTY_CODE_39_ENABLED, true);
                    properties.put(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true);
                    properties.put(BarcodeReader.PROPERTY_UPC_A_ENABLE, true);
                    properties.put(BarcodeReader.PROPERTY_EAN_13_ENABLED, true);
                    properties.put(BarcodeReader.PROPERTY_AZTEC_ENABLED, false);
                    properties.put(BarcodeReader.PROPERTY_CODABAR_ENABLED, false);
                    properties.put(BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED, false);
                    properties.put(BarcodeReader.PROPERTY_PDF_417_ENABLED, false);
                    // Set Max Code 39 barcode length
                    properties.put(BarcodeReader.PROPERTY_CODE_39_MAXIMUM_LENGTH, 10);
                    // Turn on center decoding
                    properties.put(BarcodeReader.PROPERTY_CENTER_DECODE, true);
                    // Disable bad read response, handle in onFailureEvent
                    properties.put(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, false);
                    // Apply the settings
                    barcodeReader.setProperties(properties);
                }
                catch (InvalidScannerNameException e){
                    Toast.makeText(context, "Invalid Scanner Name Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                catch (Exception e){
                    Toast.makeText(context, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static AidcReader getInstance(){
        if(mAidcReader==null){
            mAidcReader = new AidcReader();
        }
        return mAidcReader;
    }

    public void setListenerHandler(Handler h){
        this.mHandler = h;
    }

    public void claim(Context context){
        if (barcodeReader != null) {
            try {
                barcodeReader.claim();
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                Toast.makeText(context, "Scanner unavailable", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void release(){
        if (barcodeReader != null) {
            barcodeReader.release();
        }
    }

    public void destroy(){
        Log.d("JeLib","============destroy==============");
        if (barcodeReader != null) {
            // unregister barcode event listener
            barcodeReader.removeBarcodeListener(barcodeListener);

            // unregister trigger state change listener
            barcodeReader.removeTriggerListener(triggerListener);
        }
        if (barcodeReader != null) {
            // close BarcodeReader to clean up resources.
            barcodeReader.close();
            barcodeReader = null;
        }

        if (manager != null) {
            // close AidcManager to disconnect from the scanner service.
            // once closed, the object can no longer be used.
            manager.close();
        }
    }

}
