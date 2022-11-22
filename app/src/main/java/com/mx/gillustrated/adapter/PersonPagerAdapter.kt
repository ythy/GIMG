package com.mx.gillustrated.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class PersonPagerAdapter(fm: FragmentManager, private val datas:MutableList<Fragment>, private val titles:MutableList<String>) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return datas[position]
    }

    override fun getCount(): Int {
        return datas.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }

}
