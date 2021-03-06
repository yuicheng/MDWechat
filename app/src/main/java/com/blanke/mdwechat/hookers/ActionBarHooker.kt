package com.blanke.mdwechat.hookers

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.blanke.mdwechat.Classes.ActionBarContainer
import com.blanke.mdwechat.Objects.Main.LauncherUI_mActionBarContainer
import com.blanke.mdwechat.WeChatHelper.colorPrimaryDrawable
import com.blanke.mdwechat.WeChatHelper.defaultImageRippleBorderDrawable
import com.blanke.mdwechat.config.HookConfig
import com.blanke.mdwechat.util.ViewUtils
import com.gh0u1l5.wechatmagician.spellbook.base.Hooker
import com.gh0u1l5.wechatmagician.spellbook.base.HookerProvider
import com.gh0u1l5.wechatmagician.spellbook.mirror.com.tencent.mm.ui.Classes.LauncherUI
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import java.lang.ref.WeakReference

object ActionBarHooker : HookerProvider {

    override fun provideStaticHookers(): List<Hooker>? {
        return listOf(actionBarHooker)
    }

    private val actionBarHooker = Hooker {
        findAndHookMethod(ActionBarContainer, "setPrimaryBackground", Drawable::class.java, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val drawable = param.args[0]
                var needHook = true
                if (drawable is ColorDrawable) {
                    if (drawable.color == Color.parseColor("#F2F2F2")
                            || drawable.color == Color.TRANSPARENT) {
                        needHook = false
                    }
                }
                if (needHook) {
                    param.args[0] = colorPrimaryDrawable
                }
                val actionBar = param.thisObject as ViewGroup
                actionBar.elevation = 5F
                val context = actionBar.context
                if (context?.javaClass == LauncherUI) {
                    if (LauncherUI_mActionBarContainer.get() == null) {
                        LauncherUI_mActionBarContainer = WeakReference(actionBar)
                        if (HookConfig.is_hook_tab) {
                            actionBar.elevation = 0F
                        }
                    }
                }
            }
        })
        // remove split ImageView in ActionBar
        findAndHookMethod(ImageView::class.java, "onAttachedToWindow", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam?) {
                val iv = param?.thisObject as ImageView
                val frameLayout = ViewUtils.getParentView(iv, 4)
                val ivParent = ViewUtils.getParentView(iv, 1)
                if (frameLayout != null
                        && frameLayout is FrameLayout
                        && ivParent is LinearLayout
                        && ivParent.childCount == 2
                        && ivParent.indexOfChild(iv) == 1
                        && ivParent.getChildAt(0) is ImageView) {
//                    log("actionbar frameLayout =$frameLayout")
                    iv.visibility = View.GONE

                    val backIv = ivParent.getChildAt(0) as ImageView
                    backIv.background = defaultImageRippleBorderDrawable
                    ivParent.background = null
                }
            }
        })
    }
}