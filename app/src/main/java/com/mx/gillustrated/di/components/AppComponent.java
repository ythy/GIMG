package com.mx.gillustrated.di.components;

import android.content.SharedPreferences;

import com.mx.gillustrated.database.DataBaseHelper;
import com.mx.gillustrated.di.modules.AppModule;
//import com.mx.gillustrated.di.modules.DBModule;
import com.mx.gillustrated.util.DBHelper;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by maoxin on 2017/2/20.
 */
@Singleton
@Component(modules=AppModule.class)
public interface AppComponent {
    DBHelper dBHelper();
    DataBaseHelper dataBaseHelper();
    SharedPreferences sharedPreferences();
}
