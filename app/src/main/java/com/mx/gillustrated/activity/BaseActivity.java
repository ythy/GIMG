package com.mx.gillustrated.activity;

import com.mx.gillustrated.MyApplication;
import com.mx.gillustrated.di.components.DaggerBaseActivityComponent;
import com.mx.gillustrated.di.modules.BaseActivityModule;
import com.mx.gillustrated.util.DBHelper;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;

import javax.inject.Inject;

public abstract class BaseActivity extends Activity {

	@Inject
	DBHelper mDBHelper;

    @Inject
	ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        doInject();

	}

    private void doInject(){
        DaggerBaseActivityComponent.builder()
                .appComponent(((MyApplication) getApplication()).getAppComponent())
                .baseActivityModule(new BaseActivityModule(this))
                .build()
                .inject(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
