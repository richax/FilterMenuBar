package com.example.x.compoundselector;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Richa on 07/09/2017.
 */

public class NodeListAdapter extends ArrayAdapter<Node> {

    private final LayoutInflater mLayoutInflater;
    private final Drawable markCheckedLeafDrawable;
    private final Drawable markCheckedNonLeafDrawable;
    private final Drawable placeHolderDrawable;

    private final int listItemBgRes;

    public NodeListAdapter(@NonNull Context context, @NonNull List<Node> objects,
                           FilterView.ListLevelRes listLevelRes) {
        super(context, 0, new ArrayList<>(objects));
        this.mLayoutInflater = LayoutInflater.from(context);
        this.listItemBgRes = listLevelRes.getListItemBg();

        this.markCheckedLeafDrawable = ContextCompat.getDrawable(context, R.drawable.ic_checked_state_18dp);
        int w = markCheckedLeafDrawable.getIntrinsicWidth();
        int h = markCheckedLeafDrawable.getIntrinsicHeight();
        markCheckedLeafDrawable.setBounds(0, 0, w, h);
        this.placeHolderDrawable = new ColorDrawable(Color.TRANSPARENT);
        placeHolderDrawable.setBounds(0, 0, w, h);
        this.markCheckedNonLeafDrawable = ContextCompat.getDrawable(context, R.drawable.ic_done_all_18dp);
        markCheckedNonLeafDrawable.setBounds(0, 0, w, h);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        ViewHolder viewHolder;
        if (convertView != null) {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        } else {
            view = mLayoutInflater.inflate(R.layout.view_node_list_item, parent, false);
            view.setBackgroundResource(listItemBgRes);
            viewHolder = new ViewHolder();
            viewHolder.tvName = (TextView) view.findViewById(R.id.tv_node_name);
            viewHolder.tvNum = (TextView) view.findViewById(R.id.tv_node_num);
            view.setTag(viewHolder);
        }

        final Node node = getItem(position);
        viewHolder.tvName.setText(node.getShowName());

        if (node.isLeaf()) {
            viewHolder.tvNum.setVisibility(View.GONE);
        } else {
            viewHolder.tvNum.setVisibility(View.VISIBLE);
            viewHolder.tvNum.setText(String.valueOf(node.getChildren().size()));
        }
        Drawable drawable = !node.isChecked() ? placeHolderDrawable :
                (node.isLeaf() ? markCheckedLeafDrawable : markCheckedNonLeafDrawable);
        viewHolder.tvName.setCompoundDrawables(drawable, null, null, null);

        return view;
    }

    private static class ViewHolder {
        TextView tvName;
        TextView tvNum;
    }
}
