package com.ivr2.ivr2;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;

public class Call extends AppCompatActivity {

    StringBuilder builder = new StringBuilder();

    public void load_contact(String contact) {

        String contname = contact.replace("call", "");

        ContentResolver contentResolver = getContentResolver();

        Uri lkup = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, contname);
        Cursor idCursor = getContentResolver().query(lkup, null, null, null, null);
        while (idCursor.moveToNext()) {
            String id = idCursor.getString(idCursor.getColumnIndex(ContactsContract.Contacts._ID));
            String name = idCursor.getString(idCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            int hasPhoneNumber = Integer.parseInt(idCursor.getString(idCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));

            if (hasPhoneNumber > 0) {
                Cursor cursor2 = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{id}, null);

                while (cursor2.moveToNext()) {
                    String phoneNumber = cursor2.getString(cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    builder.append("Contact: ").append(name).append("\nPhone Number: ").append(phoneNumber).append("\n\n");
                }

                cursor2.close();
            }
        }
        idCursor.close();
    }

    public String show_contact() {
        String contact_details = builder.toString();
        return contact_details;
    }

}
