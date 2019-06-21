package com.hypechat.models.organizations;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hypechat.API.HypechatRequest;
import com.hypechat.R;
import com.hypechat.fragments.JoinOrganizationFragment;

import java.util.ArrayList;
import java.util.Map;

public class CustomAdapter extends ArrayAdapter<String> implements View.OnClickListener{

    private Map<String, String> dataSet;
    Context mContext;
    private JoinOrganizationFragment fragment;


    // View lookup cache
    private static class ViewHolder {
        TextView txtName;
        ImageView accept;
    }

    public CustomAdapter(Map<String, String> data, Context context, JoinOrganizationFragment fragment) {
        super(context, R.layout.row_item, (new ArrayList<>(data.values())));
        this.dataSet = data;
        this.mContext = context;
        this.fragment = fragment;
    }

    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
        Object object = getItem(position);
        String valueOrganization = (String) object;
        String keyToken = (new ArrayList<>(dataSet.keySet())).get(position);

        switch (v.getId())
        {
            case R.id.item_accept:
                this.fragment.acceptInvitation(keyToken,valueOrganization);
                break;
        }
    }

    private int lastPosition = -1;

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        String dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_item, parent, false);
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.item_org_name);
            viewHolder.accept = (ImageView) convertView.findViewById(R.id.item_accept);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        result.startAnimation(animation);
        lastPosition = position;

        viewHolder.txtName.setText(dataModel);
        viewHolder.accept.setOnClickListener(this);
        viewHolder.accept.setTag(position);

        // Return the completed view to render on screen
        return convertView;
    }
}
