/*******************************************************************************
 * Copyright (c) 2015 LG Electronics. All Rights Reserved. This software is the 
 * confidential and proprietary information of LG Electronics. You shall not
 * disclose such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with LG Electronics.
 *******************************************************************************/
package br.ufc.great.loccamlib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import br.ufc.great.loccamlib.sensors.Sensor;
import br.ufc.great.syssu.base.interfaces.IClientReaction;
import br.ufc.great.syssu.base.interfaces.IFilter;
import br.ufc.great.syssu.base.interfaces.ISysSUService;
import br.ufc.great.syssu.base.Pattern;
import br.ufc.great.syssu.base.Tuple;

public class LoccamManager {
	private ISysSUService mService;
	private MyServiceConnection mConnection;
	private ArrayList<String> mInterests = new ArrayList<String>();
	private HashMap<String, String> mReactionIds = new HashMap<String, String>();
	private String mAppId;
	private Context mContext;
	private LoccamListener mListener;

	private static final String SERVICE_NAME = "br.ufc.great.loccam.service.SysSUService";

	public LoccamManager(Context context, String appId) {
		this.mContext = context;
		this.mAppId = appId;
	}

	public void connect(LoccamListener listener) {
		this.mListener = listener;
		initService();
	}

	public void disconnect() {
		if (mService != null) {
			finishAll();
			releaseService();
		}
	}

	public void init(String key) {
		if (!checkServiceState()) {
			Log.e("Loccam Manager", "init - LoCCAM n�o iniciado");
		}
		else {
			try {
				Tuple tupla = (Tuple) new Tuple().addField("AppId", mAppId)
						.addField("InterestElement", key);
				mService.put(tupla);
				mInterests.add(key);
			} catch (RemoteException e) {
				mListener.onLoccamException(e);
			}
		}
	}

	public void finish(String key) {
		if (!checkServiceState()) {
			Log.e("Loccam Manager", "finish - LoCCAM nao iniciado");
		}
		else {
			try {
				if (mReactionIds.containsKey(key)) {
					unSubscribe(mReactionIds.get(key));
					mReactionIds.remove(key);
				}
				Pattern pattern1 = (Pattern) new Pattern().addField("AppId",
						mAppId).addField("InterestElement", key);
				mService.take(pattern1, null);
				mInterests.remove(key);
			} catch (RemoteException e) {
				mListener.onLoccamException(e);
			}
		}
	}

	public void finishAll() {
		if (!checkServiceState()) {
			Log.e("Loccam Manager", "finishAll - LoCCAM nao iniciado");
		}
		else {
			for (String interest : new ArrayList<String>(mInterests)) {
				finish(interest);
			}
		}
	}

	public ArrayList<String> getInterests() {
		return mInterests;
	}

	public Tuple getSync(String key, int timeout) {
		if (!checkServiceState()) {
			Log.e("Loccam Manager", "getSync - LoCCAM nao iniciado");
			return new Tuple();
		} else {
			try {
				Pattern pattern = (Pattern) new Pattern().addField(
						"ContextKey", key);
				List<Tuple> lista = null;
				lista = mService.readSync(pattern, null, timeout);
				if (!lista.isEmpty()) {
					return lista.get(0);
				} else {
					return new Tuple();
				}
			} catch (RemoteException e) {
				mListener.onLoccamException(e);
				return new Tuple();
			}
		}
	}

	public void getAsync(IClientReaction callback, String event, String key,
			IFilter filter) {
		if (!checkServiceState()) {
			Log.e("Loccam Manager", "getAsync - LoCCAM nao iniciado");
		} else {
			try {
				String reactionId = "";

				Pattern pattern = (Pattern) new Pattern().addField(
						"ContextKey", key);
				reactionId = mService
						.subscribe(callback, event, pattern, filter);

				if (!reactionId.equals("")) {
					mReactionIds.put(key, reactionId);
				}

			} catch (RemoteException e) {
				mListener.onLoccamException(e);
			}
		}
	}

	public void getASync(IClientReaction callback, String key) {
		getAsync(callback, "put", key, null);
	}

	public Tuple setSync(String key, String command, int timeout) {
		if (!checkServiceState()) {
			Log.e("Loccam Manager", "setSync - LoCCAM nao iniciado");
			return new Tuple();
		} else {
			try {
				Tuple tuple = (Tuple) new Tuple().addField("ControlKey", key)
						.addField("Command", command);
				mService.put(tuple);

				Pattern pattern = (Pattern) new Pattern().addField(
						"ControlKey", key).addField("CommandResponse", "?");
				List<Tuple> lista = null;
				lista = mService.readSync(pattern, null, timeout);
				if (lista.isEmpty()) {
					return new Tuple();
				} else {
					return lista.get(0);
				}
			} catch (RemoteException e) {
				mListener.onLoccamException(e);
				return new Tuple();
			}
		}
	}

	public Tuple setSync(Tuple tuple, Pattern pattern, int timeout) {

		if (!checkServiceState()) {
			Log.e("Loccam Manager", "setSync - LoCCAM nao iniciado");
			return new Tuple();
		} else {
			try {
				if (tuple != null) {
					if (!tuple.isEmpty()) {
						mService.put(tuple);
					}
				}

				List<Tuple> lista = null;
				if (pattern != null) {
					if (!pattern.isEmpty()) {
						lista = mService.readSync(pattern, null, timeout);
					}
				}
				if (lista.isEmpty()) {
					return new Tuple();
				} else {
					return lista.get(0);
				}
			} catch (RemoteException e) {
				mListener.onLoccamException(e);
				return new Tuple();
			}
		}

	}

	public void setASync(String key, String command) {
		if (!checkServiceState()) {
			Log.e("Loccam Manager", "setAsync - LoCCAM nao iniciado");
		}
		else {
			try {
				Tuple tuple = (Tuple) new Tuple().addField("ControlKey", key)
						.addField("Command", command);
				mService.put(tuple);
			} catch (RemoteException e) {
				mListener.onLoccamException(e);
			}
		}
	}

	public void setASync(Tuple tuple) {
		if (!checkServiceState()) {
			Log.e("Loccam Manager", "setAsync - LoCCAM nao iniciado");
		}
		else {
			try {
				if (tuple != null) {
					if (!tuple.isEmpty()) {
						mService.put(tuple);
					}
				}
			} catch (RemoteException e) {
				mListener.onLoccamException(e);
			}
		}

	}
	
	public Sensor getSensor(String contextKey) {
		return new Sensor(mService, contextKey);
	}

	private void unSubscribe(String reactionId) {
		if (!checkServiceState()) {
			Log.e("Loccam Manager", "unSubscribe - LoCCAM nao iniciado");
		}
		else {
			try {
				mService.unSubscribe(reactionId);
				mReactionIds.remove(reactionId);
			} catch (RemoteException e) {
				mListener.onLoccamException(e);
			}
		}
	}

	private boolean checkServiceState() {
		ActivityManager manager = (ActivityManager) mContext
				.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (SERVICE_NAME.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private void initService() {
		mConnection = new MyServiceConnection();
		Intent i = new Intent();
		i.putExtra("AppId", mContext.getPackageName());
		i.setClassName("br.ufc.great.loccam",
				"br.ufc.great.loccam.service.SysSUService");
		boolean ret = mContext.bindService(i, mConnection,
				Context.BIND_AUTO_CREATE);
		Log.d(mAppId, "initService() bound with " + ret);
	}

	private void releaseService() {
		mContext.unbindService(mConnection);
		mListener.onServiceDisconnected();
		mConnection = null;
		Log.d(mAppId, "releaseService() unbound.");
		mService = null;
	}

	private class MyServiceConnection implements ServiceConnection {

		public void onServiceConnected(ComponentName name, IBinder boundService) {
			mService = ISysSUService.Stub.asInterface(boundService);
			mListener.onServiceConnected(mService);
		}

		public void onServiceDisconnected(ComponentName name) {
			mService = null;
			mListener.onServiceDisconnected();
		}
	}	
}