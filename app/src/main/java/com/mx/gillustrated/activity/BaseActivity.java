package com.mx.gillustrated.activity;

import com.mx.gillustrated.MyApplication;
import com.mx.gillustrated.database.DataBaseHelper;
import com.mx.gillustrated.di.components.DaggerBaseActivityComponent;
import com.mx.gillustrated.di.modules.BaseActivityModule;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import javax.inject.Inject;

public abstract class BaseActivity extends Activity {

    @Inject
    DataBaseHelper mOrmHelper;

    @Inject
	ProgressDialog mProgressDialog;

    @Inject
    SharedPreferences mSP;

    public static final String SHARE_IMAGE_ORIENTATION = "gameinfo_image_orientation";

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
