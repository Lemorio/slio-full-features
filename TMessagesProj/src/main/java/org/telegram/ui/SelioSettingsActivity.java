package org.telegram.ui;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.SelioConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ListView.AdapterWithDiffUtils;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Selio's own settings screen, kept separate from stock Telegram settings so
 * the custom (non-stock) features are easy to find and don't get lost among
 * regular Telegram settings. Poin 4-8 are toggleable here; Poin 1-3 (no
 * Google services, single-ABI build, simplified theme system) are build-time
 * changes, not runtime settings, so they're shown as read-only info instead.
 */
public class SelioSettingsActivity extends BaseFragment {

    private RecyclerListView listView;
    private ListAdapter adapter;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAllowOverlayTitle(true);
        actionBar.setTitle(LocaleController.getString(R.string.SelioSettingsTitle));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        fragmentView = new FrameLayout(context);
        FrameLayout frameLayout = (FrameLayout) fragmentView;
        frameLayout.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        listView = new RecyclerListView(context);
        listView.setSections();
        actionBar.setAdaptiveBackground(listView);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        });
        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutAnimation(null);
        listView.setAdapter(adapter = new ListAdapter());
        DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setDurations(350);
        itemAnimator.setInterpolator(CubicBezierInterpolator.EASE_OUT_QUINT);
        itemAnimator.setDelayAnimations(false);
        itemAnimator.setSupportsChangeAnimations(false);
        listView.setItemAnimator(itemAnimator);
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        listView.setOnItemClickListener((view, position) -> {
            if (position < 0 || position >= items.size()) {
                return;
            }
            ItemInner item = items.get(position);
            boolean newValue;
            switch (item.id) {
                case ID_PRIORITIZE_CALL_NETWORK:
                    newValue = !SelioConfig.isPrioritizeCallNetworkEnabled();
                    SelioConfig.setPrioritizeCallNetworkEnabled(newValue);
                    ((TextCheckCell) view).setChecked(newValue);
                    break;
                case ID_HIDE_JOIN_BUBBLE:
                    newValue = !SelioConfig.isHideJoinBubbleEnabled();
                    SelioConfig.setHideJoinBubbleEnabled(newValue);
                    ((TextCheckCell) view).setChecked(newValue);
                    break;
                case ID_AUTO_MUTE_ON_JOIN:
                    newValue = !SelioConfig.isAutoMuteOnJoinEnabled();
                    SelioConfig.setAutoMuteOnJoinEnabled(newValue);
                    ((TextCheckCell) view).setChecked(newValue);
                    break;
                case ID_DEFAULT_DELETE_FOR_EVERYONE:
                    newValue = !SelioConfig.isDefaultDeleteForEveryoneEnabled();
                    SelioConfig.setDefaultDeleteForEveryoneEnabled(newValue);
                    ((TextCheckCell) view).setChecked(newValue);
                    break;
                case ID_ACTIVE_CALL_SORT_TOP:
                    newValue = !SelioConfig.isActiveCallSortTopEnabled();
                    SelioConfig.setActiveCallSortTopEnabled(newValue);
                    ((TextCheckCell) view).setChecked(newValue);
                    break;
                case ID_QUICK_SAVE_CONTEXT_MENU:
                    newValue = !SelioConfig.isQuickSaveContextMenuEnabled();
                    SelioConfig.setQuickSaveContextMenuEnabled(newValue);
                    ((TextCheckCell) view).setChecked(newValue);
                    break;
                case ID_MSG_SHARE_BUTTON:
                    newValue = !SelioConfig.isMessageShareButtonEnabled();
                    SelioConfig.setMessageShareButtonEnabled(newValue);
                    ((TextCheckCell) view).setChecked(newValue);
                    break;
                case ID_MSG_SAVE_BUTTON:
                    newValue = !SelioConfig.isMessageSaveButtonEnabled();
                    SelioConfig.setMessageSaveButtonEnabled(newValue);
                    ((TextCheckCell) view).setChecked(newValue);
                    break;
                case ID_MSG_TRANSLATE_BUTTON:
                    newValue = !SelioConfig.isMessageTranslateButtonEnabled();
                    SelioConfig.setMessageTranslateButtonEnabled(newValue);
                    ((TextCheckCell) view).setChecked(newValue);
                    break;
                default:
                    break;
            }
        });

        updateItems();

        return fragmentView;
    }

    private final ArrayList<ItemInner> items = new ArrayList<>();

    private static final int ID_PRIORITIZE_CALL_NETWORK = 1;
    private static final int ID_HIDE_JOIN_BUBBLE = 2;
    private static final int ID_AUTO_MUTE_ON_JOIN = 3;
    private static final int ID_DEFAULT_DELETE_FOR_EVERYONE = 5;
    private static final int ID_ACTIVE_CALL_SORT_TOP = 6;
    private static final int ID_QUICK_SAVE_CONTEXT_MENU = 7;
    private static final int ID_MSG_SHARE_BUTTON = 8;
    private static final int ID_MSG_SAVE_BUTTON = 9;
    private static final int ID_MSG_TRANSLATE_BUTTON = 10;

    private void updateItems() {
        items.clear();

        items.add(new ItemInner(VIEW_TYPE_HEADER, 0, LocaleController.getString(R.string.SelioFeaturesHeader)));

        items.add(new ItemInner(VIEW_TYPE_CHECK, ID_PRIORITIZE_CALL_NETWORK, LocaleController.getString(R.string.SelioPrioritizeCallNetwork)));
        items.add(new ItemInner(VIEW_TYPE_INFO, 0, LocaleController.getString(R.string.SelioPrioritizeCallNetworkInfo)));

        items.add(new ItemInner(VIEW_TYPE_CHECK, ID_HIDE_JOIN_BUBBLE, LocaleController.getString(R.string.SelioHideJoinBubble)));
        items.add(new ItemInner(VIEW_TYPE_INFO, 0, LocaleController.getString(R.string.SelioHideJoinBubbleInfo)));

        items.add(new ItemInner(VIEW_TYPE_CHECK, ID_AUTO_MUTE_ON_JOIN, LocaleController.getString(R.string.SelioAutoMuteOnJoin)));
        items.add(new ItemInner(VIEW_TYPE_INFO, 0, LocaleController.getString(R.string.SelioAutoMuteOnJoinInfo)));

        items.add(new ItemInner(VIEW_TYPE_CHECK, ID_DEFAULT_DELETE_FOR_EVERYONE, LocaleController.getString(R.string.SelioDefaultDeleteForEveryone)));
        items.add(new ItemInner(VIEW_TYPE_INFO, 0, LocaleController.getString(R.string.SelioDefaultDeleteForEveryoneInfo)));

        items.add(new ItemInner(VIEW_TYPE_CHECK, ID_ACTIVE_CALL_SORT_TOP, LocaleController.getString(R.string.SelioActiveCallSortTop)));
        items.add(new ItemInner(VIEW_TYPE_INFO, 0, LocaleController.getString(R.string.SelioActiveCallSortTopInfo)));

        items.add(new ItemInner(VIEW_TYPE_CHECK, ID_QUICK_SAVE_CONTEXT_MENU, LocaleController.getString(R.string.SelioQuickSave)));
        items.add(new ItemInner(VIEW_TYPE_INFO, 0, LocaleController.getString(R.string.SelioQuickSaveInfo)));

        items.add(new ItemInner(VIEW_TYPE_HEADER, 0, LocaleController.getString(R.string.SelioMessageButtonsHeader)));
        items.add(new ItemInner(VIEW_TYPE_CHECK, ID_MSG_SHARE_BUTTON, LocaleController.getString(R.string.SelioMsgShareButton)));
        items.add(new ItemInner(VIEW_TYPE_CHECK, ID_MSG_SAVE_BUTTON, LocaleController.getString(R.string.SelioMsgSaveButton)));
        items.add(new ItemInner(VIEW_TYPE_CHECK, ID_MSG_TRANSLATE_BUTTON, LocaleController.getString(R.string.SelioMsgTranslateButton)));
        items.add(new ItemInner(VIEW_TYPE_INFO, 0, LocaleController.getString(R.string.SelioMessageButtonsInfo)));

        items.add(new ItemInner(VIEW_TYPE_HEADER, 0, LocaleController.getString(R.string.SelioBuildInfoHeader)));
        items.add(new ItemInner(VIEW_TYPE_INFO, 0, LocaleController.getString(R.string.SelioBuildInfoText)));

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_CHECK = 1;
    private static final int VIEW_TYPE_INFO = 2;

    private static class ItemInner extends AdapterWithDiffUtils.Item {
        public CharSequence text;
        public int id;
        public ItemInner(int viewType, int id, CharSequence text) {
            super(viewType, false);
            this.id = id;
            this.text = text;
        }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ItemInner item = (ItemInner) o;
            return id == item.id && viewType == item.viewType && Objects.equals(text, item.text);
        }
    }

    private class ListAdapter extends AdapterWithDiffUtils {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (viewType == VIEW_TYPE_HEADER) {
                view = new HeaderCell(getContext());
            } else if (viewType == VIEW_TYPE_CHECK) {
                view = new TextCheckCell(getContext());
            } else {
                view = new TextInfoPrivacyCell(getContext());
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (position < 0 || position >= items.size()) {
                return;
            }
            ItemInner item = items.get(position);
            final boolean divider = position + 1 < items.size() && items.get(position + 1).viewType == item.viewType;
            if (holder.getItemViewType() == VIEW_TYPE_HEADER) {
                ((HeaderCell) holder.itemView).setText(item.text);
            } else if (holder.getItemViewType() == VIEW_TYPE_INFO) {
                TextInfoPrivacyCell cell = (TextInfoPrivacyCell) holder.itemView;
                if (TextUtils.isEmpty(item.text)) {
                    cell.setFixedSize(12);
                    cell.setText(null);
                } else {
                    cell.setFixedSize(0);
                    cell.setText(item.text);
                }
            } else if (holder.getItemViewType() == VIEW_TYPE_CHECK) {
                TextCheckCell cell = (TextCheckCell) holder.itemView;
                boolean checked;
                switch (item.id) {
                    case ID_PRIORITIZE_CALL_NETWORK:
                        checked = SelioConfig.isPrioritizeCallNetworkEnabled();
                        break;
                    case ID_HIDE_JOIN_BUBBLE:
                        checked = SelioConfig.isHideJoinBubbleEnabled();
                        break;
                    case ID_AUTO_MUTE_ON_JOIN:
                        checked = SelioConfig.isAutoMuteOnJoinEnabled();
                        break;
                    case ID_DEFAULT_DELETE_FOR_EVERYONE:
                        checked = SelioConfig.isDefaultDeleteForEveryoneEnabled();
                        break;
                    case ID_ACTIVE_CALL_SORT_TOP:
                        checked = SelioConfig.isActiveCallSortTopEnabled();
                        break;
                    case ID_QUICK_SAVE_CONTEXT_MENU:
                        checked = SelioConfig.isQuickSaveContextMenuEnabled();
                        break;
                    case ID_MSG_SHARE_BUTTON:
                        checked = SelioConfig.isMessageShareButtonEnabled();
                        break;
                    case ID_MSG_SAVE_BUTTON:
                        checked = SelioConfig.isMessageSaveButtonEnabled();
                        break;
                    case ID_MSG_TRANSLATE_BUTTON:
                        checked = SelioConfig.isMessageTranslateButtonEnabled();
                        break;
                    default:
                        checked = false;
                        break;
                }
                cell.setTextAndCheck(item.text, checked, divider);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return holder.getItemViewType() == VIEW_TYPE_CHECK;
        }

        @Override
        public int getItemViewType(int position) {
            if (position < 0 || position >= items.size()) {
                return 0;
            }
            return items.get(position).viewType;
        }
    }

    @Override
    public boolean isSupportEdgeToEdge() {
        return true;
    }

    @Override
    public void onInsets(int left, int top, int right, int bottom) {
        listView.setPadding(0, 0, 0, bottom);
        listView.setClipToPadding(false);
    }
}
