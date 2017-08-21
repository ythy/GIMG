package com.mx.gillustrated.di.components;

import com.mx.gillustrated.activity.BaseActivity;
import com.mx.gillustrated.di.modules.BaseActivityModule;
import com.mx.gillustrated.di.scope.ActivityScope;

import dagger.Component;

/**
 * Created by maoxin on 2017/2/20.
 */
@ActivityScope
@Component(dependencies = AppComponent.class, modules= BaseActivityModule.class)
public interface BaseActivityComponent {
    void inject(BaseActivity activity);
}
