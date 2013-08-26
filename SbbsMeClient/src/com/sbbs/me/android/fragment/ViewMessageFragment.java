package com.sbbs.me.android.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.Loader;
import android.content.Loader.OnLoadCompleteListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.rarnu.devlib.base.BaseFragment;
import com.rarnu.devlib.component.PullDownListView;
import com.rarnu.devlib.component.intf.OnPullDownListener;
import com.rarnu.utils.UIUtils;
import com.sbbs.me.android.Global;
import com.sbbs.me.android.R;
import com.sbbs.me.android.adapter.SbbsMePrivateMessageAdapter;
import com.sbbs.me.android.api.SbbsMeAPI;
import com.sbbs.me.android.api.SbbsMePrivateMessage;
import com.sbbs.me.android.consts.MenuIds;
import com.sbbs.me.android.database.PrivateMessageUtils;
import com.sbbs.me.android.dialog.SendMessageDialog;
import com.sbbs.me.android.loader.SbbsPrivateMessageLoader;
import com.sbbs.me.android.utils.Config;
import com.sbbs.me.android.utils.MiscUtils;

public class ViewMessageFragment extends BaseFragment implements
		OnClickListener, OnPullDownListener,
		OnLoadCompleteListener<List<SbbsMePrivateMessage>> {

	MenuItem miMessage;
	String userId;
	String userName;

	PullDownListView lvPullDown;
	TextView tvNodata;
	TextView tvLoading;
	List<SbbsMePrivateMessage> list;
	SbbsMePrivateMessageAdapter adapter;
	SbbsPrivateMessageLoader loader;

	int page = 1;
	private static final int PAGE_SIZE = 100;
	boolean isBottom = false;

	@Override
	public int getBarTitle() {
		return R.string.view_message;
	}

	@Override
	public int getBarTitleWithPath() {
		return R.string.view_message;
	}

	@Override
	public String getCustomTitle() {
		return null;
	}

	@Override
	public void initComponents() {

		lvPullDown = (PullDownListView) innerView.findViewById(R.id.lvPullDown);
		tvNodata = (TextView) innerView.findViewById(R.id.tvNodata);
		tvLoading = (TextView) innerView.findViewById(R.id.tvLoading);

		list = new ArrayList<SbbsMePrivateMessage>();
		adapter = new SbbsMePrivateMessageAdapter(getActivity(), list,
				Config.getUserId(getActivity()));

		lvPullDown.getListView().setAdapter(adapter);
		loader = new SbbsPrivateMessageLoader(getActivity());
		lvPullDown.enableAutoFetchMore(true, 1);

		int devide = UIUtils.dipToPx(8);
		lvPullDown.getListView().setDivider(null);
		lvPullDown.getListView().setDividerHeight(devide);
		lvPullDown.getListView().setPadding(devide, devide, devide, devide);
		lvPullDown.getListView().setSelector(R.color.transparent);
		lvPullDown.getListView().setOverScrollMode(View.OVER_SCROLL_NEVER);
	}

	@Override
	public void initEvents() {
		tvNodata.setOnClickListener(this);
		lvPullDown.setOnPullDownListener(this);
		loader.registerListener(0, this);
	}

	@Override
	public void initLogic() {
		page = 1;
		setIsBottom(false);

		userId = getArguments().getString("id");
		userName = getArguments().getString("name");

		lvPullDown.notifyDidLoad();
		if (SbbsMeAPI.isLogin()) {
			tvNodata.setText(R.string.no_data_refresh);
			tvLoading.setText(R.string.loading);
			tvLoading.setVisibility(View.VISIBLE);
			loader.setQuery(
					PrivateMessageUtils.getLastMessageId(getActivity()),
					userId, 1, PAGE_SIZE);
			loader.setRefresh(false);
			loader.startLoading();
		} else {
			tvNodata.setText(R.string.no_data_cannot_refresh);
			tvNodata.setVisibility(View.VISIBLE);
			tvLoading.setText(R.string.not_login);
			tvLoading.setVisibility(View.VISIBLE);
		}
	}

	private void setIsBottom(boolean bottom) {
		isBottom = bottom;
		if (isBottom) {
			lvPullDown.enableAutoFetchMore(false, 0);
			lvPullDown.showAutoFetchMore(false);
		} else {
			lvPullDown.enableAutoFetchMore(true, 1);
			lvPullDown.showAutoFetchMore(true);
		}
	}

	@Override
	public int getFragmentLayoutResId() {
		return R.layout.fragment_view_message;
	}

	@Override
	public String getMainActivityName() {
		return "";
	}

	@Override
	public void initMenu(Menu menu) {
		miMessage = menu.add(0, MenuIds.MENU_ID_MESSAGE, 99, R.string.message);
		miMessage.setIcon(MiscUtils.loadResIcon(getActivity(),
				R.drawable.ic_menu_notifications));
		miMessage.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MenuIds.MENU_ID_MESSAGE:
			startActivity(new Intent(getActivity(), SendMessageDialog.class)
					.putExtra("user", userId));
			break;
		}
		return true;
	}

	@Override
	public void onGetNewArguments(Bundle bn) {

	}

	@Override
	public Bundle getFragmentState() {
		return null;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tvNodata:
			tvNodata.setEnabled(false);
			tvLoading.setText(R.string.loading);
			tvLoading.setVisibility(View.VISIBLE);
			loader.setRefresh(true);
			page = 1;
			loader.setQuery(
					PrivateMessageUtils.getLastMessageId(getActivity()),
					userId, page, PAGE_SIZE);
			loader.setRefresh(false);
			loader.startLoading();
			break;
		}
	}

	@Override
	public void onRefresh() {

		page = 1;
		setIsBottom(false);
		loader.setRefresh(true);
		loader.setQuery(PrivateMessageUtils.getLastMessageId(getActivity()),
				userId, page, PAGE_SIZE);
		loader.startLoading();
	}

	@Override
	public void onMore() {
		if (!isBottom) {
			page++;
			loader.setRefresh(true);
			loader.setQuery(
					PrivateMessageUtils.getLastMessageId(getActivity()),
					userId, page, 100);
			loader.startLoading();
		} else {
			lvPullDown.notifyDidMore();
		}
	}

	@Override
	public void onLoadComplete(Loader<List<SbbsMePrivateMessage>> loader,
			List<SbbsMePrivateMessage> data) {
		if (page == 1) {
			list.clear();
		}
		if (data != null && data.size() != 0) {
			list.addAll(data);
		} else {
			setIsBottom(true);
		}
		if (getActivity() != null) {
			adapter.setNewList(list);
			lvPullDown.notifyDidRefresh();
			lvPullDown.notifyDidMore();
			if (!((SbbsPrivateMessageLoader) loader).isRefresh()) {
				((SbbsPrivateMessageLoader) loader).setRefresh(true);
				loader.startLoading();
			} else {
				tvNodata.setEnabled(true);
				tvNodata.setVisibility(Global.listArticle.size() == 0 ? View.VISIBLE
						: View.GONE);
				tvLoading.setVisibility(View.GONE);
			}
		}

	}

}
