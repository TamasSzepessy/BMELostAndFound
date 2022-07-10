package hu.bme.aut.android.bmelostandfound.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import hu.bme.aut.android.bmelostandfound.R
import hu.bme.aut.android.bmelostandfound.fragments.FoundFragment
import hu.bme.aut.android.bmelostandfound.fragments.LostFragment


class ViewPagerAdapter(fm: FragmentManager, val lost: String, val found: String) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> LostFragment()
            1 -> FoundFragment()
            else -> LostFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return if (position == 0) lost else found
    }

    override fun getCount(): Int {
        return 2
    }
}