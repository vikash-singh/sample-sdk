package com.gopaysense.psdata.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gopaysense.psdata.R;
import com.gopaysense.psdata.models.UserContactFeature;

/**
 * Created by Vikash Singh on 6/10/17.
 */

public class ContactAdapter extends BaseAdapter {

    private UserContactFeature[] contacts;
    private Context context;

    public void setItems(UserContactFeature[] items){
        contacts = items;
        this.notifyDataSetInvalidated();
    }

    public ContactAdapter(Context context, UserContactFeature[] contacts){
        this.contacts = contacts;
        this.context = context;
    }

    @Override
    public int getCount() {
        if(contacts != null)
            return contacts.length;

        return 0;
    }

    @Override
    public Object getItem(int itemId) {
        return contacts[itemId];
    }

    @Override
    public long getItemId(int itemId) {
        return itemId;
    }

    @Override
    public View getView(int itemId, View view, ViewGroup viewGroup) {

        UserContactFeature user = this.contacts[itemId];
        view = LayoutInflater.from(context).inflate(R.layout.contact, null);

        TextView tvName = (TextView)view.findViewById(R.id.tv_name);
        TextView tvPhone = (TextView)view.findViewById(R.id.tv_contact_no);
        TextView tvFrequency = (TextView)view.findViewById(R.id.tv_frequency_score);
        TextView tvCallScore = (TextView)view.findViewById(R.id.tv_call_score);
        TextView tvCloseness = (TextView)view.findViewById(R.id.tv_closeness_score);
        TextView tvTotalScore = (TextView)view.findViewById(R.id.tv_total_score);


        tvName.setText(user.getUserContact().getName());
        tvPhone.setText(user.getUserContact().getContactNos().toString());
        tvTotalScore.setText("" + (user.getNormalizedCallDurationScore() + user.getNormalizedNoOfCalls() + user.getNormalizedClosenessScore()));

        tvFrequency.setText("No Of Calls : " + user.getNoOfCalls() + ", Normalized : " + user.getNormalizedNoOfCalls());
        tvCloseness.setText("Closeness : " + user.getClosenessScore() + ", Normalized : " + user.getNormalizedClosenessScore());
        tvCallScore.setText("CallUnits : " + user.getCallDurationScore() + ", Normalized : " + user.getNormalizedCallDurationScore());
        view.setTag(user);
        return view;
    }

}
