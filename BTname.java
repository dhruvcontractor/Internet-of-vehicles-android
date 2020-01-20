package com.acrcloud.Musicsharedplayer;

/**
 * Created by Dhruv on 11/3/2019.
 */


import android.bluetooth.BluetoothAdapter;

public class BTname {


    public static String changeName (String newName){

        BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
        if (myDevice.getState() == BluetoothAdapter.STATE_ON) {

            myDevice.setName(newName);
           // if (newName.equals)
            System.out.println("new name is =" + newName);
            return myDevice.getName();
        }
        return "No Change";
    }

}