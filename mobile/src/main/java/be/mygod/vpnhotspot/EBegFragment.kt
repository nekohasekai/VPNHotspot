package be.mygod.vpnhotspot

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatDialogFragment
import be.mygod.vpnhotspot.databinding.FragmentEbegBinding
import be.mygod.vpnhotspot.util.launchUrl

/**
 * Based on: https://github.com/PrivacyApps/donations/blob/747d36a18433c7e9329691054122a8ad337a62d2/Donations/src/main/java/org/sufficientlysecure/donations/DonationsFragment.java
 */
class EBegFragment : AppCompatDialogFragment() {

    private lateinit var binding: FragmentEbegBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEbegBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog!!.setTitle(R.string.settings_misc_donate)
        @Suppress("ConstantConditionIf")
        if (BuildConfig.DONATIONS) (binding.donationsMoreStub.inflate() as Button).setOnClickListener {
            requireContext().launchUrl("https://mygod.be/donate/")
        }
    }
}
