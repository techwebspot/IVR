package com.ivr2.ivr2;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SpeechRecognizerManager.OnMagicWordListener{

    private SpeechRecognizerManager mSpeechRecognizerManager;
    private Call call;
    private final int REQ_CODE_SPEECH_INPUT = 10;
    private TextView speech_to_text;
    public boolean flag = true;
    public boolean contact_flag = true;
    private String speech_to_text_string;
    private String detect_call = "call";
    int hasPhoneNumber;
    int count_contact = 0;
    StringBuilder builder = new StringBuilder();
    String array_contact_name[];
    String array_contact_number[];
    String call_number;

    @Override
    public void OnMagicWordDeceted(String word) {
        if (mSpeechRecognizerManager!=null){
            mSpeechRecognizerManager.destroy();
            mSpeechRecognizerManager=null;
        }
        //Log.d("debug", "1");
        speech_input();
        //Log.d("debug", "2");
        //start_hot_word_detection();
        //Log.d("debug", "3");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speech_to_text = (TextView) findViewById(R.id.speech_to_text);

        start_hot_word_detection();

    }

    public void start_hot_word_detection() {

        if (flag && contact_flag) {
            try {
                if (mSpeechRecognizerManager == null) {
                    mSpeechRecognizerManager = new SpeechRecognizerManager(MainActivity.this);
                    mSpeechRecognizerManager.setOnResultListner(MainActivity.this);
                }
            }
            catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void load_contact(String contact) {

        int i = 0;

        builder = new StringBuilder();

        String contname = contact.replace("call", "");
        contname = contname.startsWith(" ") ? contname.substring(1) : contname;

        Log.d("debug", "contname: " + contname);

        ContentResolver contentResolver = getContentResolver();

        Uri lkup = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, contname);
        Cursor idCursor = getContentResolver().query(lkup, null, null, null, null);
        while (idCursor.moveToNext()) {

            String id = idCursor.getString(idCursor.getColumnIndex(ContactsContract.Contacts._ID));
            String name = idCursor.getString(idCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            hasPhoneNumber = Integer.parseInt(idCursor.getString(idCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));

            Log.d("MYINT", "value: " + hasPhoneNumber);

            //array_contact_name = new String[hasPhoneNumber + 10];
            //array_contact_number = new String[hasPhoneNumber + 10];

            if (hasPhoneNumber > 0) {
                Cursor cursor2 = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{id}, null);

                while (cursor2.moveToNext()) {
                    count_contact += 1;
                    String phoneNumber = cursor2.getString(cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    builder.append("Contact: ").append(name).append("\nPhone Number: ").append(phoneNumber).append("\n\n");
                    //array_contact_name[i] = name;
                    //array_contact_number[i] = phoneNumber;
                    //i++;
                }

                Log.d("MYINT", "value contact: " + count_contact);

                cursor2.close();
            }
        }

        array_contact_name = new String[count_contact];
        array_contact_number = new String[count_contact];

        lkup = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, contname);
        idCursor = getContentResolver().query(lkup, null, null, null, null);

        while (idCursor.moveToNext()) {

            String id = idCursor.getString(idCursor.getColumnIndex(ContactsContract.Contacts._ID));
            String name = idCursor.getString(idCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            hasPhoneNumber = Integer.parseInt(idCursor.getString(idCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));

            //Log.d("MYINT", "value: " + hasPhoneNumber);

            //array_contact_name = new String[hasPhoneNumber + 10];
            //array_contact_number = new String[hasPhoneNumber + 10];

            if (hasPhoneNumber > 0) {
                Cursor cursor2 = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{id}, null);

                while (cursor2.moveToNext()) {
                    String phoneNumber = cursor2.getString(cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    array_contact_name[i] = name;
                    array_contact_number[i] = phoneNumber;

                    Log.d("debug", "Name: " + array_contact_name[i] + ":" + i);

                    i++;
                }

                cursor2.close();
            }
        }

        Log.d("MYINT", "value 2: " + count_contact);

        idCursor.close();

        if (count_contact > 1) {

            boolean check = false;

            for (int l = 0; l < i; l++) {

                Log.d("debug", "Name 2: " + array_contact_name[l] + ":" + l);

                if (contname.equalsIgnoreCase(array_contact_name[l])) {

                    Log.d("debug", "el psy congroo");

                    builder = new StringBuilder();
                    builder.append("Contact: ").append(array_contact_name[l]).append("\nPhone Number: ").append(array_contact_number[l]).append("\n\n");

                    call_number = array_contact_number[l];

                    check = true;
                }
            }

            if (check == false)
                contact_speech_input();

        }
    }

    public String show_contact() {
        String contact_details = builder.toString();
        return contact_details;
    }

    private void call(String call_number) {
        String number = call_number;
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            Toast.makeText(this, "Missing permissions", Toast.LENGTH_SHORT).show();
            return;
        }
        startActivity(intent);

    }

    public void contact_speech_input() {

        contact_flag = false;

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "We found " + count_contact + " similar contact \n" + builder.toString() + "\n" + "Please specify to whom you want to call");

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException error) {
            Toast.makeText(this, "Your device don't support Speech Recognition feature", Toast.LENGTH_SHORT).show();
        }
    }

    public void speech_input(){

        flag = false;

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException error) {
            Toast.makeText(this, "Your device don't support Speech Recognition feature", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT:
                if (resultCode == RESULT_OK && data != null) {
                    //ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    speech_to_text_string = result.get(0);

                    if (speech_to_text_string.toLowerCase().indexOf(detect_call.toLowerCase()) != -1) {
                        /*
                        call = new Call();
                        call.load_contact(speech_to_text_string);
                        speech_to_text.setText(call.show_contact());
                        */
                        load_contact(speech_to_text_string);
                        speech_to_text.setText(show_contact());
                        call(call_number);
                    }

                    else {
                        speech_to_text.setText(speech_to_text_string);
                        contact_flag = true;
                    }
                }
                break;
        }

        flag = true;

        start_hot_word_detection();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
