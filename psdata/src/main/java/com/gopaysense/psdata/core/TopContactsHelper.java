package com.gopaysense.psdata.core;

import android.provider.ContactsContract;
import android.util.Log;

import com.gopaysense.psdata.LoanApplicationActivity;
import com.gopaysense.psdata.models.CallLog;
import com.gopaysense.psdata.models.UserContact;
import com.gopaysense.psdata.models.UserContactFeature;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * Created by Vikash Singh on 6/10/17.
 */

public class TopContactsHelper {


    /**
     *
     * Call Log - Number, Type, TimeStamp, Duration
     *
     * Number --> Contact
     *      Starred
     *        Top Contact
     *      PINNED
     *        Top Contact - Order
     *      Name
     *       One Word or Matches (Father, Mother, Son, Wife, Similar Surname)
     *      Photo
     *        Photo associated contacts are closer
     *
     * Duration :
     *      Remove 0 seconds call - Missed/Not Connected
     *      Less than 30 seconds average call duration between 9 am to 9 pm- SPAM
     *      More than average call duration -> weight = duration/average call duration
     *
     * Number
     *      If not in address book, ignore
     *      Check for multiple no of one contact
     *      Remove call starting 1800
     *      Add Extra weight to out of country call - regular - incoming calls may be originating from random nos
     *      ContactId - Early contacts - More closeness? - inconclusive
     *
     * TimeStamp
     *      Frequency
     *      Age of Contact - the more, the better - Given continued calls
     *      Add Extra weight to call happening at evening after 6 pm
     *      Add Extra weight to call happening at evening after 9 pm
     *      Add Extra weight to call happening at evening after 11 pm
     *      Add Extra weight to call happening on Sunday, Holiday
     *
     * Type
     *      Outgoing more weight than Incoming
     *
     *
     */

    private String[] CLOSE_FIRST_NAMES = new String[]{"Ma", "Maa", "Mom", "Mummy", "Mamma", "Mother",
            "Pa", "Paapa", "Papa", "Dad", "Daddy",
            "Mama", "Chacha", "Ghar", "Home"};

    public List<UserContactFeature> process(List<UserContact> contacts, List<CallLog> logs) {

        List<UserContactFeature> features = deDuplicatedFeatures(contacts);
        Map<String, UserContactFeature> featureMap = getFeatureMap(features);

        cleanup(featureMap, logs);
        removeSpam(logs);
        addCallDurationScore(featureMap, logs);
        addClosenessScore(featureMap);
        normalizeScore(featureMap);

//        print("+919100644824", featureMap);

        Collections.sort(features, new ContactSortByScore());
        if (features.size() >= 50)
            return features.subList(0, 50);
        else
            return features;
    }

    private void print(String phoneNumber, Map<String, UserContactFeature> featureMap) {
        UserContactFeature f = featureMap.get(phoneNumber);
        if (f != null) {
            Log.d(LoanApplicationActivity.LOG_TAG, "DEBUG-F " + f.log());
        } else {
            Log.d(LoanApplicationActivity.LOG_TAG, "DEBUG-F NOT FOUND - " + phoneNumber);
        }
    }

    private Map<String, UserContactFeature> getFeatureMap(List<UserContactFeature> features) {
        Map<String, UserContactFeature> featureMap = new HashMap<>();

        for (UserContactFeature f : features) {
            for (String phone : f.getUserContact().getContactNos()) {
                featureMap.put(phone, f);
            }
        }

        return featureMap;
    }

    private List<UserContactFeature> deDuplicatedFeatures(List<UserContact> contacts) {
        Collections.sort(contacts, new ContactSortByNoOfPhoneNumbers());

        Set<String> phoneNumbers = new HashSet<>();
        Iterator<UserContact> ite = contacts.iterator();

        Log.d(LoanApplicationActivity.LOG_TAG, "Contact Size " + contacts.size());

        while (ite.hasNext()) {
            UserContact c = ite.next();
            if (c.getContactNos() == null) {
                ite.remove();
                continue;
            }

            Iterator<String> iteNos = c.getContactNos().iterator();
            while(iteNos.hasNext()) {
                String phone = iteNos.next();
                if (phoneNumbers.contains(phone)) {
                    iteNos.remove();
                }
            }

            if (c.getContactNos().isEmpty()) {
                ite.remove();
            } else {
                phoneNumbers.addAll(c.getContactNos());
            }
        }

        Log.d(LoanApplicationActivity.LOG_TAG, "Contact Size " + contacts.size());

        List<UserContactFeature> features = new ArrayList<>();
        for (UserContact u : contacts) {
            UserContactFeature feature = new UserContactFeature(u);
            features.add(feature);
        }

        return features;
    }

    private void cleanup(Map<String, UserContactFeature> featureMap, List<CallLog> logs) {

        Long year = 365L*24L*60L*60L*1000L;
        long currentTime = System.currentTimeMillis();
        Iterator<CallLog> ite = logs.iterator();

        while (ite.hasNext()) {
            CallLog l = ite.next();
            if (l.getDuration() == null || l.getDuration() == 0) {
                Log.d(LoanApplicationActivity.LOG_TAG, "cleanup 1 - " + l.getContactNo());
                ite.remove();
                continue;
            }

            if (l.getTimestamp() + year < currentTime) {
                Log.d(LoanApplicationActivity.LOG_TAG, "cleanup 2 - " + l.getContactNo());
                ite.remove();
                continue;
            }

            if (l.getContactNo() == null
                    || l.getContactNo().startsWith("1800")) {
                Log.d(LoanApplicationActivity.LOG_TAG, "cleanup 3 - " + l.getContactNo());
                ite.remove();
                continue;
            }


            if (featureMap.get(l.getContactNo()) == null) {
                Log.d(LoanApplicationActivity.LOG_TAG, "cleanup 4 - " + l.getContactNo());
                ite.remove();
                continue;
            }
        }
    }

    private class CallFrequency {
        Integer duration = 0;
        Integer times = 0;
    }

    private void removeSpam(List<CallLog> logs) {
        Iterator<CallLog> ite = logs.iterator();
        Map<String, CallFrequency> frequencyMap = new HashMap<>();

        while (ite.hasNext()) {
            CallLog l = ite.next();
            if (l.getCallType() != null && !l.getCallType().equals("INCOMING")) {
                continue;
            }

            CallFrequency frequency = frequencyMap.get(l.getContactNo());
            if (frequency == null) {
                frequency = new CallFrequency();
                frequency.duration = 0;
                frequency.times = 0;
            }
            frequency.duration = frequency.duration + l.getDuration();
            frequency.times = frequency.times + 1;
            frequencyMap.put(l.getContactNo(), frequency);
        }

        Iterator<String> iteFreq = frequencyMap.keySet().iterator();
        while (iteFreq.hasNext()) {
            String contactNo = iteFreq.next();
            CallFrequency fre = frequencyMap.get(contactNo);
            Integer avgDuration = fre.duration / fre.times;
            if (avgDuration > 30 || fre.times <= 2) {
                iteFreq.remove();
            }
        }

        if (frequencyMap.size() == 0) {
            return;
        }

        ite = logs.iterator();
        while (ite.hasNext()) {
            CallLog l = ite.next();
            if (l.getCallType() != null && !l.getCallType().equals("INCOMING")) {
                continue;
            }

            if (frequencyMap.get(l.getContactNo()) != null) {
                Log.d(LoanApplicationActivity.LOG_TAG, "removeSpam - " + l.getContactNo());
                ite.remove();
            }
        }
    }

    private void addCallDurationScore(Map<String, UserContactFeature> contacts, List<CallLog> logs) {
//        Integer avgCallDuration = averageCallDuration(logs);
        Iterator<CallLog> ite = logs.iterator();

        while (ite.hasNext()) {
            CallLog l = ite.next();
            double timestampMultiplier = getTimestampMultiplier(l);
//            double durationUnits = l.getDuration() / avgCallDuration + 1;
//            Double score = timestampMultiplier * durationUnits;
            Double score = timestampMultiplier * l.getDuration();
            UserContactFeature userContactFeature = contacts.get(l.getContactNo());
            userContactFeature.setCallDurationScore(userContactFeature.getCallDurationScore() + score.intValue());
            userContactFeature.setNoOfCalls(userContactFeature.getNoOfCalls() + 1);
        }
    }

    private double getTimestampMultiplier(CallLog log) {
        double timestampMultiplier = 1.0;
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("IST"));
        cal.setTimeInMillis(log.getTimestamp());

        int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
        if (0 < hourOfDay && hourOfDay <= 6) {
            timestampMultiplier = 3;
        } else if (23 <= hourOfDay) {
            timestampMultiplier = 2;
        } else if (21 <= hourOfDay) {
            timestampMultiplier = 1.5;
        } else if (18 <= hourOfDay) {
            timestampMultiplier = 1.2;
        } else {
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek == Calendar.SUNDAY) {
                timestampMultiplier = 1.2;
            }
        }
        return timestampMultiplier;
    }

    private void addClosenessScore(Map<String, UserContactFeature> contacts) {

        for (UserContactFeature c : contacts.values()) {
            Integer score = 0;
            if (c.getUserContact().getStarred()) {
                score = 50;
            }

            if (c.getUserContact().getPinned() != null
                    && c.getUserContact().getPinned() != ContactsContract.PinnedPositions.UNPINNED
                    && c.getUserContact().getPinned() != ContactsContract.PinnedPositions.DEMOTED) {
                score = score + (10 - c.getUserContact().getPinned() < 0 ? 0 : 10 - c.getUserContact().getPinned()) * 2;
            }

            String[] names = c.getUserContact().getName().split(" ");
//              Interfering with contacts who are called once though good indicator
//            if (names.length == 1) {
//                score = score + 5;
//            }

            String firstName = names[0];
            for (String s : CLOSE_FIRST_NAMES) {
                if (firstName.equalsIgnoreCase(s)) {
                    score = score + 50;
                    break;
                }
            }
            c.setClosenessScore(score);
        }
    }

    private void normalizeScore(Map<String, UserContactFeature> contacts) {

        Integer maxClosenessScore = -1, minClosenessScore = -1;
        Integer maxCallDurationScore = -1, minCallDurationScore = -1;
        Integer maxNoOfCalls = -1, minNoOfCalls = -1;
        for (UserContactFeature c : contacts.values()) {
            if (maxNoOfCalls == -1 && minNoOfCalls == -1) {
                maxNoOfCalls = c.getNoOfCalls();
                minNoOfCalls = c.getNoOfCalls();
            }

            if (maxCallDurationScore == -1 && minCallDurationScore == -1) {
                maxCallDurationScore = c.getCallDurationScore();
                minCallDurationScore = c.getCallDurationScore();
            }

            if (maxClosenessScore == -1 && minClosenessScore == -1) {
                maxClosenessScore = c.getClosenessScore();
                minClosenessScore = c.getClosenessScore();
            }

            if (c.getCallDurationScore() < minCallDurationScore) {
                minCallDurationScore = c.getCallDurationScore();
            }

            if (c.getCallDurationScore() > maxCallDurationScore) {
                maxCallDurationScore = c.getCallDurationScore();
            }

            if (c.getClosenessScore() < minClosenessScore) {
                minClosenessScore = c.getClosenessScore();
            }

            if (c.getClosenessScore() > maxClosenessScore) {
                maxClosenessScore = c.getClosenessScore();
            }

            if (c.getNoOfCalls() < minNoOfCalls) {
                minNoOfCalls = c.getNoOfCalls();
            }

            if (c.getNoOfCalls() > maxNoOfCalls) {
                maxNoOfCalls = c.getNoOfCalls();
            }
        }

        for (UserContactFeature c : contacts.values()) {
            c.setNormalizedCallDurationScore(100*(c.getCallDurationScore() - minCallDurationScore) / (maxCallDurationScore - minCallDurationScore));
            c.setNormalizedClosenessScore(100*(c.getClosenessScore() - minClosenessScore) / (maxClosenessScore - minClosenessScore));
            c.setNormalizedNoOfCalls(100*(c.getNoOfCalls() - minNoOfCalls) / (maxNoOfCalls - minNoOfCalls));
        }
    }

    private Integer averageCallDuration(List<CallLog> logs) {

        int size = logs.size();
        int totalDuration = 0;
        for (CallLog l : logs) {
            totalDuration = totalDuration + l.getDuration();
        }

        int avg = totalDuration/size;

        Log.d(LoanApplicationActivity.LOG_TAG, "AverageCallDuration = " + avg);
        return avg;
    }


    private class ContactSortByScore implements Comparator<UserContactFeature> {

        public int compare(UserContactFeature s1, UserContactFeature s2) {
            return (s2.getNormalizedCallDurationScore() + s2.getNormalizedClosenessScore() + s2.getNormalizedNoOfCalls())
                    - (s1.getNormalizedCallDurationScore() + s1.getNormalizedClosenessScore() + s1.getNormalizedNoOfCalls());
        }
    }

    private class ContactSortByNoOfPhoneNumbers implements Comparator<UserContact> {

        public int compare(UserContact s1, UserContact s2) {
            return s2.getContactNos().size() - s1.getContactNos().size();
        }
    }
}
