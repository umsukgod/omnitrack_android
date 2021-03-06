package kr.ac.snu.hcil.omnitrack.ui.pages

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.danielstone.materialaboutlibrary.MaterialAboutActivity
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import com.danielstone.materialaboutlibrary.items.MaterialAboutTitleItem
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import kr.ac.snu.hcil.android.common.ANDROID_ASSET_PATH
import kr.ac.snu.hcil.android.common.versionName
import kr.ac.snu.hcil.android.common.view.dialog.WebviewScreenDialogFragment
import kr.ac.snu.hcil.omnitrack.BuildConfig
import kr.ac.snu.hcil.omnitrack.R
import kr.ac.snu.hcil.omnitrack.ui.components.dialogs.VersionCheckDialogFragment

/**
 * Created by younghokim on 2017. 1. 25..
 */
class AboutActivity : MaterialAboutActivity() {

    companion object {
        const val REQUEST_CODE_SEND_REPORT = 0
    }

    override fun getActivityTitle(): CharSequence {
        return resources.getString(R.string.msg_about)
    }

    override fun getMaterialAboutList(context: Context): MaterialAboutList {
        val builder = MaterialAboutList.Builder()

        builder.addCard(
                MaterialAboutCard.Builder()
                        .addItem(MaterialAboutTitleItem.Builder()
                                .icon(R.drawable.icon)
                                .text(BuildConfig.APP_NAME)
                                .build()
                        )
                        .addItem(MaterialAboutActionItem.Builder()
                                .showIcon(true)
                                .icon(R.drawable.icon_info_circle)
                                .iconTintRes(R.color.buttonIconColorDark)
                                .text(R.string.msg_version)
                                .textColorOverrideRes(R.color.textColorMidDark)
                                .subText("${this.versionName()} (source ver: ${BuildConfig.SOURCE_VERSION_NAME})")
                                .setOnClickListener {
                                    val versionCheckDialog = VersionCheckDialogFragment()
                                    versionCheckDialog.show(supportFragmentManager, "VersionCheck")
                                }
                                .build()
                        )
                        .addItem(
                                MaterialAboutActionItem.Builder()
                                        .icon(R.drawable.icon_clipnote)
                                        .iconTintRes(R.color.buttonIconColorDark)
                                        .textColorOverrideRes(R.color.textColorMidDark)
                                        .text(R.string.msg_open_source_license)
                                        .setOnClickListener {

                                            val licenseScreenFragment = WebviewScreenDialogFragment.makeInstance(resources.getString(R.string.msg_open_source_license), "$ANDROID_ASSET_PATH/licenses.html"/*, "${ANDROID_ASSET_PATH}/licenses/style.css"*/)

                                            licenseScreenFragment.show(supportFragmentManager, "Open Source License Fragment")
                                        }
                                        .build()
                        )
                        .build()
        )

        builder.addCard(
                MaterialAboutCard.Builder()
                        .title(R.string.msg_about_about_us)
                        .addItem(
                                MaterialAboutActionItem.Builder()
                                        .icon(R.drawable.icon_plask)
                                        .iconTintRes(R.color.buttonIconColorDark)
                                        .subText(R.string.msg_about_text)
                                        .textColorOverrideRes(R.color.textColorMidDark)
                                        .build()
                        )
                        .addItem(
                                MaterialAboutActionItem.Builder()
                                        .icon(R.drawable.icon_mail)
                                        .iconTintRes(R.color.buttonIconColorDark)
                                        .text(R.string.msg_contact_us)
                                        .textColorOverrideRes(R.color.textColorMidDark)
                                        .subText(R.string.msg_contact_us_message)
                                        .setOnClickListener {
                                            startActivityForResult(Intent(this, SendReportActivity::class.java), REQUEST_CODE_SEND_REPORT)
                                        }
                                        .build()
                        )
                        .addItem(
                                MaterialAboutActionItem.Builder()
                                        .icon(R.drawable.icon_world)
                                        .iconTintRes(R.color.buttonIconColorDark)
                                        .text("Visit Website")
                                        .subText("https://omnitrack.github.io")
                                        .setOnClickListener {
                                            val webpage = Uri.parse("https://omnitrack.github.io")
                                            val intent = Intent(Intent.ACTION_VIEW, webpage)
                                            if (intent.resolveActivity(packageManager) != null) {
                                                startActivity(intent)
                                            }

                                        }
                                        .build()
                        )
                        .build()
        )

        return builder.build()
    }


}