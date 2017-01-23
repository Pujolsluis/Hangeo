package com.pujolsluis.android.hangeo;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Oficina on 21/01/2017.
 */

public class PlanAdapter extends ArrayAdapter<PlanTemp> {
    public PlanAdapter(Context context, int resource, List<PlanTemp> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.list_item, parent, false);
        }

        ImageView photoImageView = (ImageView) convertView.findViewById(R.id.plan_background_header);
        TextView messageTextView = (TextView) convertView.findViewById(R.id.modify_plan_name_textView);
        TextView authorTextView = (TextView) convertView.findViewById(R.id.plan_locations_textView);

        PlanTemp plan = getItem(position);

        boolean isPhoto = plan.getmImageBannerResource() != 0;
        if (isPhoto) {
//            messageTextView.setVisibility(View.GONE);
//            photoImageView.setVisibility(View.VISIBLE);
//            Glide.with(photoImageView.getContext())
//                    .load(plan.getPhotoUrl())
//                    .into(photoImageView);
//            Glide.with(holder.mImageView.getContext())
//                    .load(Plan.getRandomCheeseDrawable())
//                    .fitCenter()
//                    .into(holder.mImageView);
        } else {
            messageTextView.setVisibility(View.VISIBLE);
            photoImageView.setVisibility(View.GONE);
            messageTextView.setText(plan.getmTitle());
        }
        authorTextView.setText(plan.getmTitle());

        return convertView;
    }
}
