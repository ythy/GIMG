package com.mx.gillustrated.di.components;

import android.content.SharedPreferences;

import com.mx.gillustrated.database.DataBaseHelper;
import com.mx.gillustrated.di.modules.AppModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by maoxin on 2017/2/20.
 */
@Singleton
@Component(modules=AppModule.class)
public interface AppComponent {
    DataBaseHelper dataBaseHelper();
    SharedPreferences sharedPreferences();
}
