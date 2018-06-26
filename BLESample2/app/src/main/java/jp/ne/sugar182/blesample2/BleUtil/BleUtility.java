package jp.ne.sugar182.blesample2.BleUtil;


/*
    Bleユーティリティ
    必要なパーミッション
    <uses-permission android:name="android.permission.BLUETOOTH"></uses-permission>
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"></uses-permission>
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"></uses-permission>
    <uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>

    Android4は別パッケージのAPIで定義されているため利用不可

    端末の動作やAndroidVerによる差分を確認したいため実装したが先人が作ったライブラリーを利用するべき。
    https://github.com/AltBeacon/android-beacon-library
    端末差分、AndroidVerによる差分を吸収してもらえる。
 */

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

public class BleUtility {

    public static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    public final String NOT_FOUND = "ble not found";
    public int cnt;

    BluetoothManager mbluetoothManager;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothLeScanner mBluetoothLeScanner;
    BleUtilIf mBleUtilIf;

    /**
     * コンストラクタ
     * @param ac
     * @param mBleUtilIf
     */
    public BleUtility(Activity ac, BleUtilIf mBleUtilIf) {
        mbluetoothManager = (BluetoothManager) ac.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mbluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        //TODO Android6以降のみ必要 AndroidVerによる場合分け
        ac.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);

        this.mBleUtilIf = mBleUtilIf;
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        public void onBatchScanResults(List<ScanResult> results) {
        };

        public void onScanFailed(int errorCode) {
        };

        // 通常このメソッドがコールバックされます
        public void onScanResult(int callbackType, ScanResult result) {
            cnt++;

            byte[] msg_bytes = result.getScanRecord().getManufacturerSpecificData(76);
            if (msg_bytes == null) {
                mBleUtilIf.bleScanCallback(NOT_FOUND);
                return;
            }

            String msg = "";
            for (int i = 9; i < msg_bytes.length; i++) {
                msg += Byte.toString(msg_bytes[i]) + ",";
            }

            //Log.d("byte", Integer.toString(msg_bytes.length));
            String uuid = bytesToHex(result.getScanRecord().getBytes()).substring(12, 40);

            //Log.d("ibeacon msg", msg);
            //Log.d("ibeacon uuid",uuid);

            mBleUtilIf.bleScanCallback(uuid);
        }
    };

    //byte配列をhex文字列に変換
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public void stopScan() {
        mBluetoothLeScanner.stopScan(mScanCallback);
    }
    public void startScan() {
        mBluetoothLeScanner.startScan(mScanCallback);
    }
}
