package com.example.eileen.boxsetting;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eileen.boxsetting.bluetooth.PairedDevicesAdapter;
import com.example.eileen.boxsetting.bluetooth.Constent;
import com.example.eileen.boxsetting.bluetooth.UnPairedDevicesAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BluetoothActivity extends AppCompatActivity implements View.OnClickListener{
    private TextView netSetting;
    private Button btnBluetoothStatus;
    private LinearLayout llBluetoothSwitchStatus;
    RecyclerView rvPairedDevices;
    RecyclerView rvUnpairedDevices;


    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private List<Map<String, BluetoothDevice>> mPairedDevicesList = new ArrayList<Map<String, BluetoothDevice>>();
    private List<Map<String, BluetoothDevice>> mUnpairedDevicesList = new ArrayList<Map<String, BluetoothDevice>>();
    private PairedDevicesAdapter mPairedListAdapter;
    private UnPairedDevicesAdapter mUnpairedListAdapter;
    private BluetoothManager bluetoothManager;
    myBluetoothReceiver mReceiver;

    private static final String TAG = "BluetoothActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_activity);
        btnBluetoothStatus = (Button) findViewById(R.id.bluetooth_btn_status);
        rvPairedDevices = (RecyclerView) findViewById(R.id.bluetooth_rv_paired_list);
        rvUnpairedDevices = (RecyclerView) findViewById(R.id.bluetooth_rv_unpaired_list);
        llBluetoothSwitchStatus = (LinearLayout) findViewById(R.id.bluetooth_ll_switch_status);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this);
        bluetoothManager = (BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        rvPairedDevices.setLayoutManager(layoutManager);
        rvUnpairedDevices.setLayoutManager(layoutManager1);


        netSetting = (TextView) findViewById(R.id.net_setting);
        netSetting.setBackgroundResource(R.drawable.menu_item_select);
        llBluetoothSwitchStatus.setOnClickListener(this);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (null == mBluetoothAdapter) {
            Toast.makeText(BluetoothActivity.this,
                    "沒有藍牙適配器", Toast.LENGTH_SHORT).show();

        }
        pairedDevices = mBluetoothAdapter.getBondedDevices();

        for (BluetoothDevice device : pairedDevices){

        }





        mPairedListAdapter = new PairedDevicesAdapter(mPairedDevicesList);
        mUnpairedListAdapter = new UnPairedDevicesAdapter(mUnpairedDevicesList);
        rvPairedDevices.setAdapter(mPairedListAdapter);
        rvUnpairedDevices.setAdapter(mUnpairedListAdapter);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        mReceiver = new myBluetoothReceiver();
        registerReceiver(mReceiver, filter);


    }

    @Override
    protected void onResume(){
        super.onResume();
        if (mBluetoothAdapter.isEnabled()){
            btnBluetoothStatus.setBackgroundResource(R.drawable.checkbox_on);
        }else {
            btnBluetoothStatus.setBackgroundResource(R.drawable.checkbox_off);
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.bluetooth_ll_switch_status:
                Constent.LOGI(" 你点击我了");
                if (mBluetoothAdapter.isEnabled()) {
                    btnBluetoothStatus.setBackgroundResource(R.drawable.checkbox_off);
                    mBluetoothAdapter.cancelDiscovery();
                    mBluetoothAdapter.disable();
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mBluetoothAdapter.disable();

                    rvPairedDevices.setVisibility(View.INVISIBLE);
                    rvUnpairedDevices.setVisibility(View.INVISIBLE);
//                    showControlView(false);
                    Constent.LOGI("disable bluetooth . ");
                } else {
                    btnBluetoothStatus.setBackgroundResource(R.drawable.checkbox_on);
                    mBluetoothAdapter.enable();

                    rvPairedDevices.setVisibility(View.VISIBLE);
                    rvUnpairedDevices.setVisibility(View.VISIBLE);
                    //3.9更新， 连allListAdapter都没拿， 咋显示list啊
                    //只在第一次打开的时候会拿，
                    // 如果第一次进入时没有打开蓝牙 ， 手动打开， 在这里 会再拿一次
//                    showControlView(true);
                    Constent.LOGI("enable bluetooth . ");
                }
        }
    }

    class myBluetoothReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Constent.LOGI(action);
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
                    //蓝牙状态改变时，分状态处理
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR);
                    switch (state){
                        case BluetoothAdapter.STATE_OFF:
                            Constent.LOGI("Bluetooth STATE_OFF .");
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            Constent.LOGI("Bluetooth STATE_TURNING_OFF .");
                            for (int i = 0; i < mPairedDevicesList.size(); i++) {
                                BluetoothDevice tmpDevice = mPairedDevicesList.get(i);

                                BluetoothDevice remoteDevice = mBluetoothAdapter.getRemoteDevice(tmpDevice.getAddress());
                                if (remoteDevice == null) {
                                    Constent.LOGE("onclick get remoteDevice  null .");
                                    return;
                                }
                                if (remoteDevice.getName() == null || remoteDevice.getName().isEmpty()) {
                                    return;
                                }
                                Log.i("MyReceiver", "Enter the loop++name: " + remoteDevice.getName());

                                if (remoteDevice.getName() != null && remoteDevice.getName().equals("MobileBoxRemote")) {
                                    Log.i("MyReceiver", "get device");
                                    Log.i("MyReceiver", "stop Bonding!");
                                    //Toast.makeText(paraContext, "请按下“OK”键与遥控器配对", Toast.LENGTH_SHORT).show();
                                    remoteDevice.removeBond();

                                }
                            }
                            break;
                        case BluetoothAdapter.STATE_ON:
                            Constent.LOGI("Bluetooth STATE_ON .");

                            //7.30修改 刷新UI,显示一配对的设备
//                        showControlView(true);
                            // ↑↑↑一行的代码大概是想刷新界面吧
                            if (mBluetoothAdapter.isEnabled()) {
                                Constent.LOGI("mBluetoothAdapte is Enabled");
                                //5.2修改
                                if (mBluetoothAdapter.isDiscovering()) {
                                    Constent.LOGD("Now we cancel discovery .");
                                    mBluetoothAdapter.cancelDiscovery();
                                }
                                try {
                                    Thread.sleep(1000);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //如果蓝牙正在搜索，则停止搜索，然后再重新开始搜索
                                        if (mBluetoothAdapter.isDiscovering()) {
                                            mBluetoothAdapter.cancelDiscovery();
                                        }
                                        //调用此方法会不断的发送广播，用户只需自定义一个receiver接受即可
                                        mBluetoothAdapter.startDiscovery();
                                    }
                                }).start();
                                Constent.LOGI(" after sleep, start Discovery");

                            } else {
                                mBluetoothAdapter.setScanMode(BluetoothAdapter.SCAN_MODE_CONNECTABLE, 86400000);
                                Constent.LOGI("fail to enable mBluetoothAdapte");
                            }
                            break;
                        default:
                            Constent.LOGI("没有找到指定的状态");
                            break;

                    }
                }else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    //处理可连接设备
                    BluetoothDevice foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (foundDevice == null) {
                        Constent.LOGI("没有获取到可连接设备");
                        return;
                    }

                    if (foundDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
                        for (int i = 0; i < mPairedDevicesList.size(); i++) {

                            BluetoothDevice tempBoundDevice = mPairedDevicesList.get(i);
                            if (tempBoundDevice == null) {
                                continue;
                            }

                            if (foundDevice.getAddress() != null
                                    && foundDevice.getAddress().equals(tempBoundDevice.getAddress())) {
                                //获取到的设备没有名字但有mac的情况
                                Constent.LOGI("获取到两个mac地址相同的设备-> 获取到的设备地址为:"
                                        + foundDevice.getAddress()
                                        + " 已连接设备的地址为" + tempBoundDevice.getAddress());

                            }
                        }
                        //判断找到的设备是否已经在未绑定List中了
                        for (int j = 0; j < mUnpairedDevicesList.size(); j++) {
                            BluetoothDevice tempUnboundDevice = mPairedDevicesList.get(j);
                            if (tempUnboundDevice == null){
                                continue;
                            }

                            if (tempUnboundDevice.getAddress().equals(foundDevice.getAddress())) {
                                if (foundDevice.getName() == null || foundDevice.getName().isEmpty()) {
                                    Constent.LOGI("get one addr repeat bluetooth ,return .");
                                    return;
                                }else if (tempUnboundDevice.getName() == null || tempUnboundDevice.getName().equals("")){
                                    mUnpairedDevicesList.remove(j);
                                    Constent.LOGI("找到一个同地址名字不为空的，替换前边名字为空的设备");
                                }
                            }

                        }

                        if (foundDevice.getName() == null || foundDevice.getName().isEmpty()) {
                            Constent.LOGD("in nameNull addNotNull device" + foundDevice.getAddress());
                            mUnpairedDevicesList.add(foundDevice);
                        } else {
                            Constent.LOGD("in nameNotNull device" + foundDevice.getAddress());

                            mUnpairedDevicesList.add(foundDevice);
                        }
                    }

                    Constent.LOGI(mUnpairedDevicesList + "未获取");

                }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    // 做一些搜索完成的事情,如果adapter ！= null 刷新界面

                    if (mUnpairedListAdapter != null) {
                        mUnpairedListAdapter.notifyDataSetChanged();
                    } else {
                        Constent.LOGI("mUnpairedListAdapter is null");
                    }
                    if (mPairedListAdapter != null) {
                        mPairedListAdapter.notifyDataSetChanged();
                    }

                }else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                    //绑定事件发生改变
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    switch (device.getBondState()) {
                        case BluetoothDevice.BOND_BONDING://正在配对
                            if (device.getName() == null || device.getName().isEmpty()) {
                                Constent.LOGI(device.getAddress() + "   BOND_BONDING");
                            } else {
                                Constent.LOGI(device.getName() + "   BOND_BONDING");
                            }

                            break;
                        case BluetoothDevice.BOND_BONDED://配对结束

                            if (device.getName() == null || device.getName().isEmpty()) {
                                Constent.LOGI(device.getAddress() + "   BOND_BONDED");
                            } else {
                                Constent.LOGI(device.getName() + "   BOND_BONDED");
                            }

//                        moveUnpairToPairedList(device);
                            //connectDevice(device);
                            Toast.makeText(BluetoothActivity.this,
                                    "配对成功！",
                                    Toast.LENGTH_SHORT).show();

                            break;
                        case BluetoothDevice.BOND_NONE://取消配对/未配对

                            if (device.getName() == null || device.getName().isEmpty()) {
                                Constent.LOGI(device.getAddress() + "   BOND_NONE");
                            } else {
                                Constent.LOGI(device.getName() + "   BOND_NONE");
                            }
//                        movePairToUnPairList(device);

                        default:
                            break;
                    }

/*
                if (mPairedListAdapter != null) {
                    mPairedListAdapter.updateBondState();
                    mPairedListAdapter.notifyDataSetChanged();
                }*/
                }
            }
        }

}
