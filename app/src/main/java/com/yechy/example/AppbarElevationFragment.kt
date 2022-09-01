package com.yechy.example

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.switchmaterial.SwitchMaterial

/**
 *
 * Created by cloud on 2022/8/31.
 */
class AppbarElevationFragment: Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_appbarlayout_elevation, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val appBarLayout = requireActivity().findViewById<AppBarLayout>(R.id.appbar_layout)
        val toggle = view.findViewById<SwitchMaterial>(R.id.switch_set_target)
        toggle.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                appBarLayout.liftOnScrollTargetViewId = R.id.scrollView
            } else {
                appBarLayout.liftOnScrollTargetViewId = View.NO_ID
            }
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = ExampleAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context)
        val dividerItemDecoration = MaterialDividerItemDecoration(requireContext(), LinearLayout.VERTICAL)
        dividerItemDecoration.dividerThickness = 40
        recyclerView.addItemDecoration(dividerItemDecoration)
    }
}