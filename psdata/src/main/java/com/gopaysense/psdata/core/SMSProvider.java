package com.gopaysense.psdata.core;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.Telephony;
import android.provider.Telephony.TextBasedSmsColumns;
import android.util.Log;

import com.gopaysense.psdata.models.SMS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Vikash Singh on 5/10/17.
 */

public class SMSProvider {

    public static final Uri INBOX_URI = android.provider.Telephony.Sms.Inbox.CONTENT_URI;
    public static final Uri CONVERSATIONS_URI = android.provider.Telephony.Sms.Conversations.CONTENT_URI;

    private ContentResolver contentResolver;

    private final String ID = BaseColumns._ID;
    private final String BODY = TextBasedSmsColumns.BODY;
    private final String DATE = TextBasedSmsColumns.DATE;
    private final String PERSON = TextBasedSmsColumns.PERSON;
    private final String SUBJECT = TextBasedSmsColumns.SUBJECT;
    private final String THREAD_ID = TextBasedSmsColumns.THREAD_ID;
    private final String TYPE = TextBasedSmsColumns.TYPE;
    private final String CREATOR = TextBasedSmsColumns.CREATOR;
    private final String ADDRESS = TextBasedSmsColumns.ADDRESS;

    public SMSProvider(Context context) {
        contentResolver = context.getContentResolver();
    }

    public List<SMS> compile() {
        List<SMS> result = new ArrayList<>();
        String[] projection = new String[]{ID, BODY, DATE, PERSON, SUBJECT, THREAD_ID, TYPE, CREATOR, ADDRESS};
        Cursor cursor = contentResolver.query(INBOX_URI, projection, null, null, null);

        if (cursor == null)
            return result;

        while (cursor.moveToNext()) {
            SMS sms = getSMS(cursor);
            result.add(sms);
        }

        cursor.close();

        Collections.sort(result, new SMSSortByTimeStamp());
        return result;
    }


    public List<SMS> conversations() {
        List<SMS> result = new ArrayList<>();
        String[] projection = new String[]{ID, BODY, DATE, PERSON, SUBJECT, THREAD_ID, TYPE, CREATOR, ADDRESS, Telephony.Sms.Conversations.SNIPPET, Telephony.Sms.Conversations.MESSAGE_COUNT};
        Cursor cursor = contentResolver.query(CONVERSATIONS_URI, projection, null, null, null);

        if (cursor == null)
            return result;

        while (cursor.moveToNext()) {
            SMS sms = getSMS(cursor);
            result.add(sms);
        }

        cursor.close();

        Collections.sort(result, new SMSSortByTimeStamp());
        return result;
    }


    private SMS getSMS(Cursor cursor) {
        SMS sms = new SMS();

        sms.setId(cursor.getString(cursor.getColumnIndex(ID)));
        sms.setBody(cursor.getString(cursor.getColumnIndex(BODY)));
        sms.setPerson(cursor.getString(cursor.getColumnIndex(PERSON)));
        sms.setSubject(cursor.getString(cursor.getColumnIndex(SUBJECT)));
        sms.setThreadId(cursor.getInt(cursor.getColumnIndex(THREAD_ID)));
        sms.setType(cursor.getString(cursor.getColumnIndex(TYPE)));
        sms.setDate(cursor.getLong(cursor.getColumnIndex(DATE)));
        sms.setCreator(cursor.getString(cursor.getColumnIndex(CREATOR)));
        sms.setAddress(cursor.getString(cursor.getColumnIndex(ADDRESS)));
        return sms;
    }


    class SMSSortByTimeStamp implements Comparator<SMS> {

        public int compare(SMS s1, SMS s2) {
            return s1.getDate().compareTo(s2.getDate());
        }
    }
}