/*******************************************************************************
 * Copyright (c) 1999, 2014 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution. 
 *
 * The Eclipse Public License is available at 
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.eclipse.paho.android.service.sample;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import myMQTT.app.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Handles collection of user information to create a new MQTT Client
 *
 */
public class NewConnection extends Activity {

  /** {@link Bundle} which holds data from activities launched from this activity **/
  private Bundle result = null;

  /** 
   * @see android.app.Activity#onCreate(android.os.Bundle)
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_new_connection);

      //load auto compete options

    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
    adapter.addAll(readHosts());
    AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.serverURI);
    textView.setAdapter(adapter);

      ArrayAdapter<String> adapterD = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
      adapterD.addAll(readDevices());
      AutoCompleteTextView textViewD = (AutoCompleteTextView) findViewById(R.id.deviceName);
      textViewD.setAdapter(adapterD);


  }

  /** 
   * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.activity_new_connection, menu);
    OnMenuItemClickListener listener = new Listener(this);
    menu.findItem(R.id.connectAction).setOnMenuItemClickListener(listener);
//    menu.findItem(R.id.advanced).setOnMenuItemClickListener(listener);

    return true;
  }

  /** 
   * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home :
        NavUtils.navigateUpFromSameTask(this);
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
   */
  @Override
  protected void onActivityResult(int requestCode, int resultCode,
      Intent intent) {

    if (resultCode == RESULT_CANCELED) {
      return;
    }

    result = intent.getExtras();

  }

  /**
   * Handles action bar actions
   *
   */
  private class Listener implements OnMenuItemClickListener {

    //used for starting activities 
    private NewConnection newConnection = null;

    public Listener(NewConnection newConnection)
    {
      this.newConnection = newConnection;
    }

    /**
     * @see android.view.MenuItem.OnMenuItemClickListener#onMenuItemClick(android.view.MenuItem)
     */
    @Override
    public boolean onMenuItemClick(MenuItem item) {
      {
        // this will only connect need to package up and sent back

        int id = item.getItemId();

        Intent dataBundle = new Intent();

        switch (id) {
          case R.id.connectAction :
            //extract client information
            String deviceName = ((AutoCompleteTextView) findViewById(R.id.deviceName))
                    .getText().toString();
            String server = ((AutoCompleteTextView) findViewById(R.id.serverURI))
                .getText().toString();

            if (server.equals(ActivityConstants.empty) || deviceName.equals(ActivityConstants.empty))
            {
              String notificationText = newConnection.getString(R.string.missingOptions);
              Notify.toast(newConnection, notificationText, Toast.LENGTH_LONG);
              return false;
            }

             String username = ((AutoCompleteTextView) findViewById(R.id.uname)).getText()
                      .toString();
             String password = ((EditText) findViewById(R.id.password))
                      .getText().toString();


              String port = ((EditText) findViewById(R.id.port))
                      .getText().toString();
              if (port.equals(ActivityConstants.empty)){
                  port = newConnection.getString(R.string.portHint);
              }
              String clientId = ((EditText) findViewById(R.id.clientId))
                      .getText().toString();
              if (clientId.equals(ActivityConstants.empty)) {
//                  clientId = Long.toString(UUID.randomUUID().getLeastSignificantBits());
                  clientId = Long.toString(UUID.nameUUIDFromBytes(deviceName.getBytes()).getLeastSignificantBits());
                  Notify.toast(newConnection, clientId, Toast.LENGTH_LONG);

              }

              // Keep subscriptions stored in broker
              boolean cleanSession = !((CheckBox) findViewById(R.id.cleanSessionCheckBox)).isChecked();

              //persist server and devicename
            persistServerURI(server);
            persistDeviceName(deviceName);

            //put data into a bundle to be passed back to ClientConnections
            dataBundle.putExtra(ActivityConstants.deviceName, deviceName);
            dataBundle.putExtra(ActivityConstants.server, server);
            dataBundle.putExtra(ActivityConstants.port, port);
            dataBundle.putExtra(ActivityConstants.clientId, clientId);
            dataBundle.putExtra(ActivityConstants.action, ActivityConstants.connect);
            dataBundle.putExtra(ActivityConstants.cleanSession, cleanSession);
            dataBundle.putExtra(ActivityConstants.username, username);
            dataBundle.putExtra(ActivityConstants.password, password);


              if (result == null) {
              // create a new bundle and put default advanced options into a bundle
              result = new Bundle();

              result.putString(ActivityConstants.message,
                  ActivityConstants.empty);
              result.putString(ActivityConstants.topic, ActivityConstants.empty);
              result.putInt(ActivityConstants.qos, ActivityConstants.defaultQos);
              result.putBoolean(ActivityConstants.retained,
                  ActivityConstants.defaultRetained);

              result.putInt(ActivityConstants.timeout,
                  ActivityConstants.defaultTimeOut);
              result.putInt(ActivityConstants.keepalive,
                  ActivityConstants.defaultKeepAlive);
              result.putBoolean(ActivityConstants.ssl,
                  ActivityConstants.defaultSsl);

            }
            //add result bundle to the data being returned to ClientConnections
            dataBundle.putExtras(result);

            setResult(RESULT_OK, dataBundle);
            newConnection.finish();
            break;
/*          case R.id.advanced :
            //start the advanced options activity
            dataBundle.setClassName(newConnection,
                "org.eclipse.paho.android.service.sample.Advanced");
            newConnection.startActivityForResult(dataBundle,
                ActivityConstants.advancedConnect);

            break;
 */
        }
        return false;

      }

    }

    /**
     * Add a server URI to the persisted file
     * 
     * @param serverURI the uri to store
     */
    private void persistServerURI(String serverURI) {
      File fileDir = newConnection.getFilesDir();
      File presited = new File(fileDir, "hosts.txt");
      BufferedWriter bfw = null;
      try {
        bfw = new BufferedWriter(new FileWriter(presited));
        bfw.write(serverURI);
        bfw.newLine();
      }
      catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      finally {
        try {
          if (bfw != null) {
            bfw.close();
          }
        }
        catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }

  }

  /**
   * Read persisted hosts
   * @return The hosts contained in the persisted file
   */
  private String[] readHosts() {
    File fileDir = getFilesDir();
    File persisted = new File(fileDir, "hosts.txt");
    if (!persisted.exists()) {
      return new String[0];
    }
    ArrayList<String> hosts = new ArrayList<String>();
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(persisted));
      String line = null;
      line = br.readLine();
      while (line != null) {
        hosts.add(line);
        line = br.readLine();
      }
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    finally {
      try {
        if (br != null) {
          br.close();
        }
      }
      catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    return hosts.toArray(new String[hosts.size()]);

  }
    /**
     * Add a device name to the persisted file
     *
     * @param deviceName the uri to store
     */
    private void persistDeviceName(String deviceName) {
        File fileDir = getFilesDir();
        File presited = new File(fileDir, "devices.txt");
        BufferedWriter bfw = null;
        try {
            bfw = new BufferedWriter(new FileWriter(presited));
            bfw.write(deviceName);
            bfw.newLine();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally {
            try {
                if (bfw != null) {
                    bfw.close();
                }
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }



    /**
     * Read persisted devices
     * @return The hosts contained in the persisted file
     */
    private String[] readDevices() {
        File fileDir = getFilesDir();
        File persisted = new File(fileDir, "devices.txt");
        if (!persisted.exists()) {
            return new String[0];
        }
        ArrayList<String> devices = new ArrayList<String>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(persisted));
            String line = null;
            line = br.readLine();
            while (line != null) {
                devices.add(line);
                line = br.readLine();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (br != null) {
                    br.close();
                }
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return devices.toArray(new String[devices.size()]);

    }

}
