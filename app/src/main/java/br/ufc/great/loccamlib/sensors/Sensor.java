/*******************************************************************************
 * Copyright (c) 2015 LG Electronics. All Rights Reserved. This software is the 
 * confidential and proprietary information of LG Electronics. You shall not
 * disclose such Confidential Information and shall use it only in accordance
 * with the terms of the license agreement you entered into with LG Electronics.
 *******************************************************************************/
package br.ufc.great.loccamlib.sensors;

import android.os.RemoteException;

import br.ufc.great.syssu.base.interfaces.ISysSUService;
import br.ufc.great.syssu.base.Tuple;

public class Sensor {
	
	private ISysSUService mLoccam;
	private String mContextKey;
	
	public Sensor(ISysSUService loccam, String contextKey) {
		mLoccam = loccam;
		mContextKey = contextKey;
	}
	
	public Tuple get() {
		Tuple getTuple = (Tuple) new Tuple().addField("Type", "Get");
		try {
			return mLoccam.cacAccess(mContextKey, getTuple);
		} catch (RemoteException e) {
			// TODO create exception tuple
			return new Tuple(); 
		}
	}
	
	public Tuple set(Tuple action) {
		Tuple setTuple = (Tuple) action.addField("Type", "Set");
		try {
			return mLoccam.cacAccess(mContextKey, setTuple);
		} catch (RemoteException e) {
			// TODO create exception tuple
			return new Tuple(); 
		}
	}

}
