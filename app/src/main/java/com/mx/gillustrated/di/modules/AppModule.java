package com.mx.gillustrated.di.modules;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.mx.gillustrated.MyApplication;
import com.mx.gillustrated.database.DataBaseHelper;
import com.mx.gillustrated.database.DatabaseManager;

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
    DataBaseHelper provideDataBaseHelper(Context context){
        return DatabaseManager.getHelper(context, DataBaseHelper.class);
    }

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences(Context context){
        return context.getSharedPreferences("commonset", Context.MODE_PRIVATE);
    }
}
