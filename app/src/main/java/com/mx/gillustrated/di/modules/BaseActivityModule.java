package com.mx.gillustrated.di.modules;

import android.app.ProgressDialog;

import com.mx.gillustrated.activity.BaseActivity;
import com.mx.gillustrated.di.scope.ActivityScope;

import dagger.Module;
import dagger.Provides;

/**
 * Created by maoxin on 2017/2/20.
 */

@Module
public class BaseActivityModule {

    private BaseActivity baseActivity;

    public BaseActivityModule(BaseActivity activity){
        this.baseActivity = activity;
    }

    @Provides
    @ActivityScope
    BaseActivity provideBaseActivity(){
        return baseActivity;
    }


    @Provides
    @ActivityScope
    ProgressDialog provideProgressDialog(BaseActivity activity){
        ProgressDialog pd = new ProgressDialog(activity);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage("请稍等。。。");
        pd.setIndeterminate(false);
        pd.setCancelable(false);
        return pd;
    }

}
