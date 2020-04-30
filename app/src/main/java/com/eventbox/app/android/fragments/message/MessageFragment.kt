package com.eventbox.app.android.fragments.message

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_message.view.toolbarLayout
import com.eventbox.app.android.BottomIconDoubleClick
import com.eventbox.app.android.R
import com.eventbox.app.android.utils.Utils.setToolbar
import com.eventbox.app.android.utils.extensions.hideWithFading
import com.eventbox.app.android.utils.extensions.setPostponeSharedElementTransition
import com.eventbox.app.android.utils.extensions.setStartPostponedEnterTransition
import com.eventbox.app.android.utils.extensions.showWithFading
import kotlinx.android.synthetic.main.fragment_message.view.*

const val MESSAGE_FRAGMENT = "messageFragment"

class MessageFragment : Fragment(), BottomIconDoubleClick {

    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //setPostponeSharedElementTransition()
        rootView = inflater.inflate(R.layout.fragment_message, container, false)
        rootView.messageRecycler.layoutManager = LinearLayoutManager(activity)
        // === put adapter here ====
        rootView.messageRecycler.isNestedScrollingEnabled = false
        rootView.viewTreeObserver.addOnDrawListener {
            //setStartPostponedEnterTransition()
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootView.messageScrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
            if (scrollY > rootView.messageTitle.y && !rootView.toolbarLayout.isVisible)
                rootView.toolbarLayout.showWithFading()
            else if (scrollY < rootView.messageTitle.y && rootView.toolbarLayout.isVisible)
                rootView.toolbarLayout.hideWithFading()
        }
    }

    override fun onResume() {
        super.onResume()
        setToolbar(activity, show = false)
    }

    override fun doubleClick() = rootView.messageScrollView.smoothScrollTo(0, 0)
}
