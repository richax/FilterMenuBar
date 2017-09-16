package com.example.x.compoundselector;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Richa on 07/09/2017.
 */

public class FilterView {

    private static final int MAX_VISIBLE_ITEM_COUNT = 9;

    private final Context context;
    private final Node rootNode;
    private final int treeDegree;
    private final OnNodeSelectedListener nodeSelectedListener;

    private final int listViewCount;
    private final List<ListView> listViewList = new ArrayList<>();
    private final List<NodeListAdapter> adapterList = new ArrayList<>();

    private FilterView(Context context, Node rootNode, OnNodeSelectedListener nodeSelectedListener) {
        this.context = context;
        this.rootNode = rootNode;
        this.nodeSelectedListener = nodeSelectedListener;
        this.listViewCount = Node.getTreeDepth(rootNode) - 1;

        // As screen dimension limit, we just enabled 1-3 level, though more levels are supported.
        if (listViewCount < 1 || listViewCount > 3) {
            throw new IllegalArgumentException("Unsupported tree");
        }
        this.treeDegree = Node.getTreeDegree(rootNode);
    }

    public static View newFilterView(Context context, Node rootNode,
                                     OnNodeSelectedListener nodeSelectedListener) {
        FilterView filterView = new FilterView(context, rootNode, nodeSelectedListener);
        return filterView.buildView();
    }

    public enum ListLevelRes {
        LV_ONE(R.color.filter_common, R.drawable.selector_list_one_bg),
        LV_TWO(R.color.filter_lv2_bg, R.drawable.selector_list_two_bg),
        LV_THREE(R.color.filter_lv3_bg, R.drawable.selector_list_three_bg);

        @ColorRes
        private final int listViewBgColor;
        @DrawableRes
        private final int listItemBg;

        ListLevelRes(@ColorRes int listViewBgColor, @DrawableRes int listItemBg) {
            this.listItemBg = listItemBg;
            this.listViewBgColor = listViewBgColor;
        }

        public
        @ColorRes
        int getListViewBgColor() {
            return listViewBgColor;
        }

        public
        @DrawableRes
        int getListItemBg() {
            return listItemBg;
        }

        public static ListLevelRes getListLevelResByIndex(int index) {
            if (index < 0)
                return LV_ONE; // default LV_ONE
            return ListLevelRes.values()[index % ListLevelRes.values().length];
        }
    }

    private
    @ColorInt
    int getListViewBgColorByIndex(int index) {
        ListLevelRes listLevelRes = ListLevelRes.getListLevelResByIndex(index);
        return ContextCompat.getColor(context, listLevelRes.getListViewBgColor());
    }

    private View buildView() {

        final View view = LayoutInflater.from(context).inflate(R.layout.view_filter_multi_list, null);
        final LinearLayout contentView = (LinearLayout) view.findViewById(R.id.content_view);

        final Node checkedNode = Node.getCheckedLeafWithDefault(rootNode);
        final List<Node> simplePath = Node.getNodeSimplePath(checkedNode);

        for (int index = 0; index < listViewCount; index++) {
            List<Node> nodeList;
            if (index <= (simplePath.size() - 1)) {
                nodeList = new ArrayList<>(simplePath.get(index).getChildren());
            } else {
                nodeList = new ArrayList<>();
            }
            ListLevelRes listLevelRes = ListLevelRes.getListLevelResByIndex(index);
            NodeListAdapter adapter = new NodeListAdapter(context, nodeList, listLevelRes);

            ListView listView = newListView();
            listView.setBackgroundColor(getListViewBgColorByIndex(index));
            listView.setOnItemClickListener(onItemClickListener);

            // Limit the max height of the listView.
            if (index == 0 && treeDegree > MAX_VISIBLE_ITEM_COUNT) {
                float listItemHeight = context.getResources().getDimension(R.dimen.list_item_height);
                listView.setLayoutParams(new LinearLayout.LayoutParams(
                        0, (int) ((MAX_VISIBLE_ITEM_COUNT + 0.5) * listItemHeight), 1.0f));
            }

            listView.setAdapter(adapter);
            contentView.addView(listView);
            listViewList.add(listView);
            adapterList.add(adapter);

            // If some one in the nodeList is in checked state, updating the listView's
            // checked state, which would affect the list view item's "activated" state.
            for (int i = 0, length = nodeList.size(); i < length; i++) {
                Node node = nodeList.get(i);
                if (node.isChecked()) {
                    listView.setItemChecked(i, true);
                    // 9 items visible on screen. Make the checked item shown in the center of the listView.
                    if (length > 9 && i > 4) {
                        int scrollPos = Math.min((i + 4), (length - 1));
                        listView.smoothScrollToPosition(scrollPos);
                    }
                    break;
                }
            }
        }
        return view;
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    @NonNull
    private ListView newListView() {
        ListView listView = new ListView(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        listView.setLayoutParams(lp);
        listView.setDivider(null);
        listView.setCacheColorHint(Color.TRANSPARENT);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        return listView;
    }

    private final AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final Node clickedNode = (Node) ((ListView) parent).getAdapter().getItem(position);

            if (clickedNode.isLeaf()) {
                Node.setSingleLeafNodeChecked(rootNode, clickedNode);
                if (nodeSelectedListener != null) {
                    nodeSelectedListener.onNodeSelected(clickedNode);
                }
            } else {
                List<Node> simplePath = Node.getNodeSimplePath(clickedNode);

                // 1. Refresh the clicked ListView
                // Root node of the tree is not shown in view. So if a node is not a leaf
                // nor a root node, the count of nodes in the simple path (the path from
                // the node to the root) must be greater than or equal to 2.
                final int clickedListViewIndex = simplePath.size() - 2;
                ListView clickedListView = listViewList.get(clickedListViewIndex);
                clickedListView.setItemChecked(position, true);

                // 2. Update all subsequent ListView
                Node node = clickedNode;
                for (int i = clickedListViewIndex + 1, length = listViewList.size(); i < length; i++) {
                    List<Node> nodeList;
                    // The index of the checked node in the node list, or -1 if all nodes in the list are unchecked.
                    int checkedPos = -1;
                    if (node != null && node.getChildren().size() > 0) {
                        nodeList = node.getChildren();
                        // Search the checked node of the current level, if exist.
                        for (int pos = 0, len = nodeList.size(); pos < len; pos++) {
                            if (nodeList.get(pos).isChecked()) {
                                checkedPos = pos;
                                break;
                            }
                        }
                        // Handling the scenario that one of the nodes was already
                        // in checked state. If such a node exist (we call it "A"),
                        // all the subsequent listView data should be A's descendant,
                        // rather than A's first sibling's (index 0) default.
                        node = nodeList.get(checkedPos > -1 ? checkedPos : 0);
                    } else {
                        nodeList = new ArrayList<>();
                        node = null;
                    }
                    NodeListAdapter adapter = adapterList.get(i);
                    adapter.setNotifyOnChange(false);
                    adapter.clear();
                    adapter.setNotifyOnChange(true);
                    adapter.addAll(nodeList);

                    // Underneath data changed, so clear the listView's checked state first. (the
                    // listView's checked state would affect the "activated" state of the list
                    // view items in it).
                    // And if some one in the new dataset is in checked state, updating the listView's
                    // checked state, which would affect the list view item's "activated" state.
                    ListView listView = listViewList.get(i);
                    listView.clearChoices();
                    if (checkedPos > -1) {
                        listView.setItemChecked(checkedPos, true);
                    }
                }
            }
        }
    };

    public interface OnNodeSelectedListener {
        void onNodeSelected(Node node);
    }
}
