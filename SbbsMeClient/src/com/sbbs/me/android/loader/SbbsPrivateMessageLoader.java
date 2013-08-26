package com.sbbs.me.android.loader;

import java.io.File;
import java.util.List;

import android.content.Context;

import com.rarnu.devlib.base.BaseLoader;
import com.rarnu.utils.DownloadUtils;
import com.sbbs.me.android.api.SbbsMeAPI;
import com.sbbs.me.android.api.SbbsMePrivateMessage;
import com.sbbs.me.android.api.SbbsMeUser;
import com.sbbs.me.android.consts.PathDefine;
import com.sbbs.me.android.database.PrivateMessageUtils;
import com.sbbs.me.android.utils.MiscUtils;

public class SbbsPrivateMessageLoader extends BaseLoader<SbbsMePrivateMessage> {

	private String lastMsgId;
	private String userId;
	private int page;
	private int pageSize;
	private boolean refresh = false;

	public SbbsPrivateMessageLoader(Context context) {
		super(context);
	}

	public void setQuery(String lastMsgId, String userId, int page, int pageSize) {
		this.lastMsgId = lastMsgId;
		this.userId = userId;
		this.page = page;
		this.pageSize = pageSize;
	}

	@Override
	public List<SbbsMePrivateMessage> loadInBackground() {

		File fUserHead = new File(PathDefine.ROOT_PATH + userId + ".jpg");
		if (!fUserHead.exists()) {
			SbbsMeUser user = SbbsMeAPI.getUser(userId);
			DownloadUtils.downloadFile(user.AvatarURL, PathDefine.ROOT_PATH
					+ MiscUtils.extractFileNameFromURL(user.AvatarURL), null);
		}

		List<SbbsMePrivateMessage> list = null;
		if (refresh) {
			list = SbbsMeAPI.queryMessage(lastMsgId, userId, page, pageSize);
			if (list != null && list.size() != 0) {
				PrivateMessageUtils.saveMessages(getContext(), list);
			} else {
				list = PrivateMessageUtils.queryMessages(getContext());
			}
		} else {
			list = PrivateMessageUtils.queryMessages(getContext());
			if (list == null || list.size() == 0) {
				refresh = true;
				list = SbbsMeAPI.queryMessage(lastMsgId, page, pageSize);
				if (list != null && list.size() != 0) {
					PrivateMessageUtils.saveMessages(getContext(), list);
				}
			}
		}
		return list;
	}

	public boolean isRefresh() {
		return refresh;
	}

	public void setRefresh(boolean refresh) {
		this.refresh = refresh;
	}

}
