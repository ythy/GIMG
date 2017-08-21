package com.mx.gillustrated;

import android.app.Application;

import com.mx.gillustrated.di.components.AppComponent;
import com.mx.gillustrated.di.components.DaggerAppComponent;
import com.mx.gillustrated.di.modules.AppModule;
//import com.mx.gillustrated.di.modules.AppModule;
//import com.mx.gillustrated.di.modules.DBModule;

/**
 * Created by maoxin on 2017/2/20.
 */

public class MyApplication extends Application {

    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();

    }

    public AppComponent getAppComponent(){
        return appComponent;
    }


}
