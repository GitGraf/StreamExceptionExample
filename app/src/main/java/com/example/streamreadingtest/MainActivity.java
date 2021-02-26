package com.example.streamreadingtest;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    Button sendBtn;
    EditText bytesTf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bytesTf = findViewById(R.id.byteAmount);
        sendBtn = findViewById(R.id.send_it);

        runSocketServer();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int bytes = Integer.parseInt(bytesTf.getText().toString());
                runSocketClient(bytes);
            }
        });
    }

    private void runSocketClient(final int numberOfBytes) {
        Runnable client = new Runnable() {
            @Override
            public void run() {
                // Change to "10.0.2.2" if server is not running on Android
                String host = "10.0.2.2";
                try (Socket socket = new Socket(host, 12345)) {
                    DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                    DataInputStream dataIn = new DataInputStream(socket.getInputStream());
                    dataOut.writeInt(numberOfBytes);
                    dataOut.flush();

                    // Insert breakpoint (or Thread.sleep) here
                    // and observe that EOFExceptions become more rare.

                    byte[] data = new byte[numberOfBytes];
                    dataIn.readFully(data);
                    Log.i("Test", data.length + " bytes: successfully read");
                } catch(IOException ex) {
                    Log.i("Test", numberOfBytes + " bytes: " + ex);
                    ex.printStackTrace();
                }
            }
        };

        new Thread(client).start();
    }

    private void runSocketServer() {
        Runnable server = new Runnable() {
            @Override
            public void run() {
                try {
                    ServerSocket serverSocket = new ServerSocket(12345);
                    System.out.println("Listening to port " + serverSocket.getLocalPort());
                    while (true) {
                        Socket socket = serverSocket.accept();
                        DataInputStream dataIn = new DataInputStream(socket.getInputStream());
                        DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());

                        int amountOfBytesToGenerate = dataIn.readInt();
                        byte[] randomBytes = new byte[amountOfBytesToGenerate];
                        new Random().nextBytes(randomBytes);

                        System.out.println("Sending " + amountOfBytesToGenerate + " bytes to client " + socket.toString());

                        dataOut.write(randomBytes);
                        dataOut.flush();

                        socket.close();
                    }
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        new Thread(server).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
