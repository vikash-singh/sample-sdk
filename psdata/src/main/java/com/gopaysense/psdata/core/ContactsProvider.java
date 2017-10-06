package com.gopaysense.psdata.core;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.gopaysense.psdata.models.UserContact;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Vikash Singh on 5/10/17.
 */

public class ContactsProvider {

    private final Uri QUERY_URI = ContactsContract.Contacts.CONTENT_URI;
    private final String CONTACT_ID = ContactsContract.Contacts._ID;
    private final String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
    private final Uri EMAIL_CONTENT_URI = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
    private final String EMAIL_CONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
    private final String EMAIL_DATA = ContactsContract.CommonDataKinds.Email.DATA;
    private final String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
    private final String PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
    private final Uri PHONE_CONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
    private final String PHONE_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
    private final String STARRED_CONTACT = ContactsContract.Contacts.STARRED;
    private final String PINNED_CONTACT = ContactsContract.Contacts.PINNED;

    private ContentResolver contentResolver;

    public ContactsProvider(Context context) {
        contentResolver = context.getContentResolver();
    }

    public List<UserContact> getContacts(boolean full) {
        List<UserContact> contactList = new ArrayList<>();
        String[] projection = new String[]{CONTACT_ID, DISPLAY_NAME, HAS_PHONE_NUMBER, STARRED_CONTACT, PINNED_CONTACT};
        Cursor cursor = contentResolver.query(QUERY_URI, projection, HAS_PHONE_NUMBER + ">?", new String[] {"0"}, null);

        if (cursor == null)
            return contactList;

        while (cursor.moveToNext()) {
            UserContact contact = getContact(cursor, full);
            contactList.add(contact);
        }

        cursor.close();
        return contactList;
    }

    private UserContact getContact(Cursor cursor, boolean full) {
        String contactId = cursor.getString(cursor.getColumnIndex(CONTACT_ID));
        String name = (cursor.getString(cursor.getColumnIndex(DISPLAY_NAME)));
        Boolean starredContact = cursor.getInt(cursor.getColumnIndex(STARRED_CONTACT)) == 1;
        Uri uri = Uri.withAppendedPath(QUERY_URI, String.valueOf(contactId));
        Integer pinned = cursor.getInt(cursor.getColumnIndex(PINNED_CONTACT));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        String intentUriString = intent.toUri(0);

        UserContact contact = new UserContact();
        contact.setId(contactId);
        contact.setName(name);
        contact.setUriString(intentUriString);
        contact.setStarred(starredContact);
        contact.setPinned(pinned);
        contact.setContactNos(new HashSet<String>());
        if (full) {
            getPhone(contactId, contact);
//            getEmail(contactId, contact);
        }
        return contact;
    }

    private void getEmail(String contactId, UserContact contact) {
        Cursor emailCursor = contentResolver.query(EMAIL_CONTENT_URI, null, EMAIL_CONTACT_ID + " = ?", new String[]{contactId}, null);
        if (emailCursor == null)
            return;

        while (emailCursor.moveToNext()) {
            String email = emailCursor.getString(emailCursor.getColumnIndex(EMAIL_DATA));
            if (!TextUtils.isEmpty(email)) {
                contact.setEmail(email);
            }
        }
        emailCursor.close();
    }

    private void getPhone(String contactId, UserContact contact) {
        Cursor phoneCursor = contentResolver.query(PHONE_CONTENT_URI, null, PHONE_CONTACT_ID + " = ?", new String[]{contactId}, null);
        if (phoneCursor == null)
            return;
        while (phoneCursor.moveToNext()) {
            String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(PHONE_NUMBER));
            contact.addContact(PhoneNumberUtils.format(phoneNumber));
        }
        phoneCursor.close();
    }

    @SuppressWarnings({"MissingPermission"})
    public List<com.gopaysense.psdata.models.CallLog> getCallDetails() {
        List<com.gopaysense.psdata.models.CallLog> logs = new LinkedList<>();

        Cursor managedCursor = contentResolver.query(CallLog.Calls.CONTENT_URI, null, null, null, null);
        if (managedCursor == null)
            return logs;

        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);

        while (managedCursor.moveToNext()) {
            String phNumber = managedCursor.getString(number);
            String callType = managedCursor.getString(type);
            Long callDate = managedCursor.getLong(date);
            Integer callDuration = managedCursor.getInt(duration);
            String dir = null;
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;

                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }

            logs.add(new com.gopaysense.psdata.models.CallLog(PhoneNumberUtils.format(phNumber), dir, callDate, callDuration));
        }

        managedCursor.close();

        return logs;
    }
}
