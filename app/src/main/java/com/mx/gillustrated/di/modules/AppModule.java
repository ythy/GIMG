package com.mx.gillustrated.di.modules;

import android.app.Application;
import android.content.Context;

import com.mx.gillustrated.MyApplication;
import com.mx.gillustrated.provider.Providerdata;
import com.mx.gillustrated.util.DBHelper;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by maoxin on 2017/2/20.
 */

@Module
public class AppModule {
    private MyApplication application;

    public AppModule(MyApplication application) {
        this.application = application;
    }

    @Provides
    @Singleton
    Context provideContext(){
        return application;
    }

    @Provides
    @Singleton
    DBHelper provideDBHelper(Context context){
        return new DBHelper(context, Providerdata.DATABASE_NAME,
                null, Providerdata.DATABASE_VERSION);
    }
}
