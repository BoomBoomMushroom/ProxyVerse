package com.example.proxyverse;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import androidx.collection.SimpleArrayMap;
import androidx.core.app.ActivityCompat;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.proxyverse.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


// COED STARTS!

public class MainActivity extends AppCompatActivity {
    // ask for these perms: ACCESS_FINE_LOCATION, BLUETOOTH_ADVERTISE, BLUETOOTH_CONNECT, BLUETOOTH_SCAN

    int requestingCode = 1337;

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAllNeededPermissions();
                /*
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAnchorView(R.id.fab)
                        .setAction("Action", null).show();
                 */
            }
        });
    }

    // "START" OF PERMISSION REQUEST
    private void requestAllNeededPermissions(){
        ActivityCompat.requestPermissions(this, new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.BLUETOOTH_ADVERTISE,
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_SCAN
        }, requestingCode);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == requestingCode){
            if(grantResults.length >= 4){
                Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                startAdvertising();
                startDiscovery();
            }
            else{
                Toast.makeText(this, "Permission denied :c", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // END OF PERMISSION REQUEST



    // NEARBY CONNECTION! BABY WOO, ONLY TOOK NINE HOURS
    Strategy STRATEGY = Strategy.P2P_CLUSTER;
    String SERVICE_ID = "com.example.proxyverse.nearby_connection";
    Context CONTEXT = this;



    // Put yourself out there!
    private void startAdvertising(){


        AdvertisingOptions advertisingOptions = new AdvertisingOptions.Builder()
                .setStrategy(STRATEGY).build();
        Nearby.getConnectionsClient(CONTEXT)
                .startAdvertising("Mario", SERVICE_ID, connectionLifecycleCallback, advertisingOptions)
                .addOnSuccessListener(
                        (Void unused) -> {
                            // WE'RE ADVERTISING; YIPPEE
                            Toast.makeText(this, "Starting to Advertising", Toast.LENGTH_SHORT).show();
                        })
                .addOnFailureListener(
                        (Exception e) -> {
                            // No advertising :c
                            Toast.makeText(this, "Failed to start Advertising", Toast.LENGTH_SHORT).show();
                        });
    }


    private final ConnectionLifecycleCallback connectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointID, ConnectionInfo connectionInfo) {
                    // Automatically accept connection on both sides
                    Nearby.getConnectionsClient(CONTEXT).acceptConnection(endpointID, payloadCallback);
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch(result.getStatus().getStatusCode()){
                        case 0: // ConnectionsStatusCodes.STATUS_OK
                            // we are connected. now time to send & recieve data
                            Toast.makeText(CONTEXT, "Connection Okay!", Toast.LENGTH_SHORT).show();
                            break;
                        case 8004: // ConnectionsStatusCodes.STATUS_CONNECION_REJECTED
                            // connection was rejected by one or both sides
                            Toast.makeText(CONTEXT, "Rejected :(", Toast.LENGTH_SHORT).show();
                            break;
                        case 13: // ConnectionsStatusCodes.STATUS_ERROR
                            // connection broke before being accepted
                            Toast.makeText(CONTEXT, "Monday left me broken", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(CONTEXT, "¯\\_(ツ)_/¯", Toast.LENGTH_SHORT).show();
                            // unknown.
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    // we've been disconnected. no more data can be sent or recieved
                }
            };



    private final PayloadCallback payloadCallback =
            new PayloadCallback() {
                @Override
                public void onPayloadReceived(String endpointId, Payload payload) {
                    onRecieve(endpointId, payload);
                }

                @Override
                public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {

                }
            };


    // FIND PEOPLE!
    private void startDiscovery() {
        DiscoveryOptions discoveryOptions =
                new DiscoveryOptions.Builder().setStrategy(STRATEGY).build();
        Nearby.getConnectionsClient(CONTEXT)
                .startDiscovery(SERVICE_ID, endpointDiscoveryCallback, discoveryOptions)
                .addOnSuccessListener(
                        (Void unused) -> {
                            // We're discovering people!
                            Toast.makeText(this, "Starting Discovery", Toast.LENGTH_SHORT).show();
                        })
                .addOnFailureListener(
                        (Exception e) -> {
                            // Couldn't start discovering!
                            Toast.makeText(this, "Failed to start discovery", Toast.LENGTH_SHORT).show();
                        });
    }

    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
                    // endpoint found, lets request a connection
                    Nearby.getConnectionsClient(CONTEXT)
                            .requestConnection("Mario", endpointId, connectionLifecycleCallback)
                            .addOnSuccessListener(
                                    (Void unused) -> {
                                        // We have successfully sent a connection
                                        // now we have to accept too
                                        Toast.makeText(CONTEXT, "Sent connection request", Toast.LENGTH_SHORT).show();
                                    })
                            .addOnFailureListener(
                                    (Exception e) -> {
                                        // Nearby Connections failed to request the connection
                                        Toast.makeText(CONTEXT, "Failed to request connection", Toast.LENGTH_SHORT).show();
                                    });
                }

                @Override
                public void onEndpointLost(String endpointId) {
                    // a previously discovered endpoint has gone away
                }
            };

    // When we get a connection!
    //@Override
    public void onConnectionInitiated(String endpointId, ConnectionInfo info){
        /*
        // Some alert code from: https://developers.google.com/nearby/connections/android/manage-connections
        new AlertDialog.Builder(CONTEXT_IGNORE_SECURITY)
                .setTitle("Accept connection to " + info.getEndpointName())
                .setMessage("Confirm the code matches on both devices: " + info.getAuthenticationDigits())
         */

        Toast.makeText(this, "Connected successfully! Say hi", Toast.LENGTH_SHORT).show();

        new AlertDialog.Builder(CONTEXT)
                .setTitle("Accept connection to " + info.getEndpointName())
                .setMessage("Confirm the code matches on both devices: " + info.getAuthenticationDigits())
                .setPositiveButton("Accept", (DialogInterface d, int which)->{})
                .setNegativeButton("Decline", (DialogInterface d, int which)->{});

        Nearby.getConnectionsClient(CONTEXT)
                .acceptConnection(endpointId, payloadCallback);
    }



    private void onRecieve(String endpointId, Payload payload){

    }

}

/*
static class Recieve StreamPayloadCallback extends PayloadCallback{
    private final SimpleArrayMap<Long, Thread> backgroundThreads = new SimpleArrayMap<>();
    private static final long READ_STREAM_IN_BG_TIMEOUT = 5000;

    @Override
    public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update){
        if(backgroundThreads.containsKey(update.getPayloadId())
            && update.getStatus() != PayloadTransferUpdate.Status.IN_PROGRESS){
            backgroundThreads.get(update.getPayloadId()).interrupt();
        }
    }

    @Override
    public void onPayloadRecieved(String endpointId, Payload payload){
        if(payload.getType() == Payload.Type.STREAM){
            // read the avaiable bytes in a while loop to free stream pipe
            // bytes will block the pipe and slow down the throughput
            Thread backgroundThread =
                    new Thread() {
                        @Override
                        public void run(){
                            InputStream inputStream = payload.asStream().asInputStream();
                            long lastRead = SystemClock.elapsedRealtime();
                            while (!Thread.interrupted()){
                                if ((SystemClock.elapsedRealtime() - lastRead) >= READ_STREAM_IN_BG_TIMEOUT){
                                    Log.e("ProxyVerse", "Stream timed out");
                                    break;
                                }

                                try{
                                    int availableBytes = inputStream.available();
                                    if(availableBytes > 0){
                                        byte[] bytes = new byte[availableBytes];
                                        if(inputStream.read(bytes) == availableBytes){
                                            lastRead = SystemClock.elapsedRealtime();
                                            // do stuff with data here...
                                        }
                                    } else{
                                        // sleep or just continue.
                                    }
                                } catch(IOException e){
                                    Log.e("ProxyVerse", "Failed to read bytes from InputStream.", e);
                                    break;
                                } // try-catch
                            } // while
                        }
                    };
            backgroundThread.start();
            backgroundThreads.put(payload.getId(), backgroundThread);
        }
    }
}
*/