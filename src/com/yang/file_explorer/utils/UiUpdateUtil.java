package com.yang.file_explorer.utils;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;

public class UiUpdateUtil {

	protected static List<Handler> clients = new ArrayList<Handler>();

	public static void registerClient(Handler client) {
		if (!clients.contains(client)) {
			clients.add(client);
		}
	}

	public static void unregisterClient(Handler client) {
		while (clients.contains(client)) {
			clients.remove(client);
		}
	}

	public static void updateClients() {
		// myLog.l(Log.DEBUG, "UI update");
		// Log.d("UiUpdate", "Update now");
		for (Handler client : clients) {
			client.sendEmptyMessage(0);
		}
	}

}
