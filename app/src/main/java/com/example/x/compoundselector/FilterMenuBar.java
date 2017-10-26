package com.example.x.compoundselector;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Richa on 08/09/2017.
 */

public class FilterMenuBar extends LinearLayout {

    private final int MAX_SUPPORT_DEPTH = 4;
    private final int default_horizontal_padding;
    private final int default_vertical_padding;
    private final int default_background_color;
    private final Drawable default_menu_title_badge;

    private PopupWindow mPopupWindow;

    public FilterMenuBar(Context context) {
        this(context, null);
    }

    public FilterMenuBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FilterMenuBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.default_horizontal_padding = dp2px(4.0f);
        this.default_vertical_padding = dp2px(10.0f);
        this.default_background_color = ContextCompat.getColor(context, R.color.filter_common);
        this.default_menu_title_badge = ContextCompat.getDrawable(context, R.drawable.ic_spinner_badge_24dp);
        default_menu_title_badge.setBounds(0, 0, default_menu_title_badge.getIntrinsicWidth(),
                default_menu_title_badge.getIntrinsicHeight());

        init();
    }

    private void init() {
        setOrientation(HORIZONTAL);
        setBackgroundColor(default_background_color);
        setShowDividers(SHOW_DIVIDER_MIDDLE);
        setDividerPadding(dp2px(8f));
        Drawable divider = ContextCompat.getDrawable(getContext(),
                android.R.drawable.divider_horizontal_bright);
        setDividerDrawable(divider);
    }

    public void setMenuItems(List<Node> rootNodeList) {
        removeAllViews();
        for (Node rootNode : rootNodeList) {
            appendMenuItem(rootNode);
        }
    }

    public boolean appendMenuItem(Node rootNode) {
        if (rootNode == null || rootNode.getChildren() == null || rootNode.getChildren().size() == 0) {
            return false;
        }

        Node.formatTree(rootNode);

        int depth = Node.getTreeDepth(rootNode);
        // Unsupported tree, limited by the dimension of the screen.
        if (depth < 2 && depth > MAX_SUPPORT_DEPTH) {
            return false;
        }

        Node checkedLeafNode = Node.getCheckedLeafWithDefault(rootNode);
        TextView titleView = newMenuTitleView();
        titleView.setText(checkedLeafNode.getShowName());
        // Store the tree into the textView's tag attribute.
        titleView.setTag(rootNode);
        titleView.setOnClickListener(internalTitleClickListener);
        addView(titleView);

        return true;
    }

    @NonNull
    private TextView newMenuTitleView() {
        TextView titleView = new TextView(getContext());
        LayoutParams lp = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        titleView.setLayoutParams(lp);
        titleView.setMaxLines(1);
        titleView.setEllipsize(TextUtils.TruncateAt.END);
        titleView.setGravity(Gravity.CENTER);
        titleView.setPadding(default_horizontal_padding, default_vertical_padding,
                default_horizontal_padding, default_vertical_padding);
        titleView.setCompoundDrawables(null, null, default_menu_title_badge, null);
        titleView.setCompoundDrawablePadding(-default_menu_title_badge.getIntrinsicWidth());
        return titleView;
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    private final View.OnClickListener internalTitleClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mPopupWindow == null || !mPopupWindow.isShowing()) {
                showFilterMenuWindow(view);
            } else {
                mPopupWindow.dismiss();
            }
        }
    };

    private final View.OnClickListener internalShadowClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mPopupWindow.dismiss();
        }
    };

    private void showFilterMenuWindow(@NonNull View anchorView) {

        Object tag = anchorView.getTag();
        if (!(tag instanceof Node)) {
            return;
        }
        Node rootNode = (Node) tag;

        final TextView invokerView = (anchorView instanceof TextView) ? ((TextView) anchorView) : null;
        View contentView = FilterView.newFilterView(getContext(), rootNode,
                new FilterView.OnNodeSelectedListener() {
                    @Override
                    public void onNodeSelected(Node node) {
                        // Update the title text.
                        if (invokerView != null) {
                            invokerView.setText(node.getShowName());
                        }
                        mPopupWindow.dismiss();

                        // Collecting filter selected result.
                        if (onFilterItemSelectedListener != null) {

                            List<List<Node>> result = new ArrayList<>();
                            // index for indicating which menu group invoked this selection action.
                            int invokedGroupIndex = -1;
                            for (int i = 0, count = getChildCount(); i < count; i++) {
                                View view = getChildAt(i);
                                if (!(view instanceof TextView)) {
                                    continue;
                                }
                                TextView textView = (TextView) view;
                                Object tag = textView.getTag();
                                if (!(tag instanceof Node)) {
                                    continue;
                                }
                                if (invokedGroupIndex < 0 && textView.equals(invokerView)) {
                                    invokedGroupIndex = result.size();
                                }
                                List<Node> subResult;
                                Node rootNode = (Node) tag;
                                Node checkedNode = Node.findCheckedLeafPreOrder(rootNode);
                                if (checkedNode == null) {
                                    subResult = Collections.emptyList();
                                } else {
                                    List<Node> nodePath = Node.getNodeSimplePath(checkedNode);
                                    subResult = new ArrayList<>();

                                    // index 0 represents the root node for differentiating the group
                                    for (int j = 0, length = nodePath.size(); j < length; j++) {
                                        subResult.add((Node) nodePath.get(j).clone());
                                    }
                                }
                                result.add(subResult);
                            }
                            onFilterItemSelectedListener.onFilterItemSelected(result, invokedGroupIndex);
                        }
                    }
                });

        View shadowView = contentView.findViewById(R.id.view_shadow);
        shadowView.setOnClickListener(internalShadowClickListener);

        if (mPopupWindow == null) {
            mPopupWindow = new PopupWindow(getContext());
            mPopupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            mPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            mPopupWindow.setTouchable(true);
            mPopupWindow.setFocusable(true);
            mPopupWindow.setOutsideTouchable(true);

            mPopupWindow.setBackgroundDrawable(new ColorDrawable(
                    ContextCompat.getColor(getContext(), R.color.filter_shadow_color)));
        }
        // Update the contentView of the popupWindow.
        mPopupWindow.setContentView(contentView);

        if (Build.VERSION.SDK_INT != 24 && Build.VERSION.SDK_INT != 25) {
            mPopupWindow.showAsDropDown(this, 0, 0);
        } else {
            int[] location = new int[2];
            getLocationOnScreen(location);
            mPopupWindow.showAtLocation(this, Gravity.NO_GRAVITY, 0, location[1] + getHeight());
        }
    }

    private OnFilterItemSelectedListener onFilterItemSelectedListener;

    public void setOnFilterItemSelectedListener(OnFilterItemSelectedListener listener) {
        this.onFilterItemSelectedListener = listener;
    }

    public interface OnFilterItemSelectedListener {
        /**
         * @param selectedGroups    the selection results of all the menu groups. If one group without
         *                          selected item, empty list would be provided rather than {@code null}.
         * @param invokedGroupIndex index for indicating which menu group invoked this selection action.
         */
        void onFilterItemSelected(List<List<Node>> selectedGroups, int invokedGroupIndex);
    }
}
