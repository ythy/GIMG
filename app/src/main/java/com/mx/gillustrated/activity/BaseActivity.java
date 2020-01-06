package com.mx.gillustrated.activity;

import com.mx.gillustrated.MyApplication;
import com.mx.gillustrated.database.DataBaseHelper;
import com.mx.gillustrated.di.components.DaggerBaseActivityComponent;
import com.mx.gillustrated.di.modules.BaseActivityModule;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import javax.inject.Inject;

public abstract class BaseActivity extends AppCompatActivity {

    @Inject
    DataBaseHelper mOrmHelper;

    @Inject
	ProgressDialog mProgressDialog;

    @Inject
    public SharedPreferences mSP;

    public static final String SHARE_IMAGE_ORIENTATION = "gameinfo_image_orientation";
    public static final String SHARE_IMAGE_ORIENTATION_EVENT = "gameinfo_image_orientation_event";
    public static final String SHARE_IMAGES_MATRIX = "add_images_matrix";
    public static final String SHARE_IMAGES_MATRIX_NUMBER = "add_images_matrix_number";
    public static final String SHARE_SHOW_HEADER_IMAGES = "show_header_images";
    public static final String SHARE_PAGE_SIZE = "list_page_size";
    public static final String SHARE_IMAGES_HEADER_SCALE_NUMBER = "header_images_scale_float_number";
    public static final String SHARE_SHOW_COST_COLUMN = "gameinfo_show_cost_column";

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
