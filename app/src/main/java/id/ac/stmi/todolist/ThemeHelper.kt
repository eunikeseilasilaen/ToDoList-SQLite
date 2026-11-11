
package id.ac.stmi.todolist

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.TextView

object ThemeHelper {

    // WARNA LIGHT MODE
    const val LIGHT_BACKGROUND = 0xFFF3F6FC.toInt()
    private const val LIGHT_SURFACE = 0xFFFFFFFF.toInt()
    private const val LIGHT_TEXT = 0xFF203653.toInt()
    private const val LIGHT_HINT = 0xFF8996A8.toInt()
    private const val LIGHT_BUTTON = 0xFF315D9B.toInt()

    // WARNA DARK MODE
    const val DARK_BACKGROUND = 0xFF101827.toInt()
    const val DARK_SURFACE = 0xFF1E293B.toInt()
    const val DARK_BUTTON = 0xFF426DA8.toInt()
    private const val DARK_TEXT = 0xFFF8FAFC.toInt()
    private const val DARK_HINT = 0xFFB8C5D8.toInt()

    // WARNA AKSEN
    private const val DARK_ACCENT = 0xFF9BB9E8.toInt()

    // MEMERIKSA TEMA YANG DIPILIH
    fun isDark(context: Context): Boolean {
        val preferences = context.getSharedPreferences(
            "AppSettings",
            Context.MODE_PRIVATE
        )

        return preferences.getString("theme", "Light Mode") == "Dark Mode"
    }

    // TEMA DATE PICKER
    fun datePickerTheme(context: Context): Int {
        return if (isDark(context)) {
            android.R.style.Theme_Material_Dialog_Alert
        } else {
            android.R.style.Theme_Material_Light_Dialog_Alert
        }
    }

    // MENERAPKAN TEMA PADA SELURUH ACTIVITY
    fun apply(activity: Activity) {

        val dark = isDark(activity)

        val root = activity.window.decorView.findViewById<ViewGroup>(
            android.R.id.content
        )

        root.setBackgroundColor(
            if (dark) DARK_BACKGROUND else LIGHT_BACKGROUND
        )

        applyViews(root, dark)

        activity.window.statusBarColor =
            if (dark) DARK_BACKGROUND else LIGHT_BACKGROUND

        activity.window.navigationBarColor =
            if (dark) DARK_BACKGROUND else Color.WHITE

        if (android.os.Build.VERSION.SDK_INT >= 23) {
            activity.window.decorView.systemUiVisibility =
                if (dark) {
                    0
                } else {
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }
        }
    }

    // MEMERIKSA APAKAH WARNA BACKGROUND TERLALU TERANG
    private fun isLightColor(color: Int): Boolean {
        val brightness =
            (Color.red(color) * 0.299 +
                    Color.green(color) * 0.587 +
                    Color.blue(color) * 0.114) / 255

        return brightness >= 0.78
    }

    // MENGATUR WARNA SELURUH KOMPONEN LAYOUT
    fun applyViews(
        view: View,
        dark: Boolean = isDark(view.context)
    ) {

        val textColor = if (dark) DARK_TEXT else LIGHT_TEXT
        val hintColor = if (dark) DARK_HINT else LIGHT_HINT

        // BACKGROUND KHUSUS DAILY OVERVIEW DAN CATEGORIES
        val cardColor = when (view.id) {

            R.id.cardDailyOverview ->
                if (dark) 0xFF263957.toInt() else 0xFFE5EEFF.toInt()

            R.id.cardToday ->
                if (dark) 0xFFCFE3FF.toInt() else 0xFFDCEBFF.toInt()

            R.id.cardPlanned ->
                if (dark) 0xFFE7D4FF.toInt() else 0xFFE8E0FF.toInt()

            R.id.cardPersonal ->
                if (dark) 0xFFD2F3E2.toInt() else 0xFFDDF4E8.toInt()

            R.id.cardWork ->
                if (dark) 0xFFFFE0C2.toInt() else 0xFFFFE8D5.toInt()

            R.id.cardShopping ->
                if (dark) 0xFFFAD6E3.toInt() else 0xFFFFE0E8.toInt()

            else -> null
        }

        if (cardColor != null) {

            if (dark) {
                val radius =
                    18f * view.resources.displayMetrics.density

                view.background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = radius
                    setColor(cardColor)

                    if (view.id == R.id.cardDailyOverview) {
                        setStroke(
                            (1 * view.resources.displayMetrics.density).toInt(),
                            0xFF405477.toInt()
                        )
                    }
                }
            } else {
                view.setBackgroundColor(cardColor)
            }

        } else {

            // BACKGROUND KOMPONEN LAIN TETAP MENGGUNAKAN LOGIKA LAMA
            val background = view.background

            if (background is ColorDrawable) {
                val currentColor = background.color

                if (dark && isLightColor(currentColor)) {
                    view.setBackgroundColor(DARK_SURFACE)

                } else if (
                    !dark &&
                    (currentColor == DARK_SURFACE ||
                            currentColor == DARK_BACKGROUND)
                ) {
                    view.setBackgroundColor(LIGHT_SURFACE)
                }
            }
        }

        // MENGATUR WARNA TEKS DAN KOMPONEN
        when (view) {

            is EditText -> {
                view.setTextColor(textColor)
                view.setHintTextColor(hintColor)

                view.backgroundTintList = ColorStateList.valueOf(
                    if (dark) DARK_HINT else 0xFFB8C5D8.toInt()
                )
            }

            is CompoundButton -> {
                view.setTextColor(textColor)

                view.buttonTintList = ColorStateList.valueOf(
                    if (dark) DARK_ACCENT else LIGHT_BUTTON
                )
            }

            is Button -> {
                view.setTextColor(Color.WHITE)

                view.backgroundTintList = ColorStateList.valueOf(
                    if (dark) DARK_BUTTON else LIGHT_BUTTON
                )
            }

            is TextView -> {
                view.setTextColor(
                    if (dark) {
                        getCategoryTextColor(view) ?: DARK_TEXT
                    } else {
                        LIGHT_TEXT
                    }
                )
            }
        }

        // MENGATUR SEMUA KOMPONEN DI DALAM LAYOUT
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                applyViews(view.getChildAt(i), dark)
            }
        }
    }

    // MENGATUR WARNA TEKS KHUSUS KATEGORI
    private fun getCategoryTextColor(view: View): Int? {

        var parent = view.parent as? View

        while (parent != null) {

            val isCategory = when (parent.id) {
                R.id.cardToday,
                R.id.cardPlanned,
                R.id.cardPersonal,
                R.id.cardWork,
                R.id.cardShopping -> true

                else -> false
            }

            if (isCategory) {

                return when (view.id) {

                    R.id.tvTodayCount,
                    R.id.tvPlannedCount,
                    R.id.tvPersonalCount,
                    R.id.tvWorkCount,
                    R.id.tvShoppingCount ->
                        0xFF34465F.toInt()

                    else ->
                        0xFF1E2C40.toInt()
                }
            }

            parent = parent.parent as? View
        }

        return null
    }

    // MENGATUR TAMPILAN ALERT DIALOG
    fun applyDialog(
        dialog: AlertDialog,
        context: Context
    ) {

        val dark = isDark(context)

        dialog.window?.setBackgroundDrawable(
            ColorDrawable(
                if (dark) DARK_SURFACE else Color.WHITE
            )
        )

        dialog.window?.decorView?.let {
            applyViews(it, dark)
        }

        // MENGATUR WARNA TOMBOL DIALOG
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
            if (dark) Color.WHITE else LIGHT_BUTTON
        )

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
            if (dark) DARK_ACCENT else LIGHT_BUTTON
        )

        dialog.getButton(AlertDialog.BUTTON_NEUTRAL)?.setTextColor(
            if (dark) DARK_ACCENT else LIGHT_BUTTON
        )
    }

    // MENGATUR WARNA HALAMAN WELCOME
    fun applyWelcomeReminder(activity: Activity) {

        val dark = isDark(activity)

        val heroColor = if (dark) {
            0xFF263F5C.toInt()
        } else {
            0xFFE5EEFF.toInt()
        }

        val featureColor = if (dark) {
            0xFF263F5C.toInt()
        } else {
            0xFFE5EEFF.toInt()
        }

        val reminderColor = if (dark) {
            0xFF263F5C.toInt()
        } else {
            0xFFE5EEFF.toInt()
        }

        val titleColor = if (dark) {
            0xFFF0F6FF.toInt()
        } else {
            0xFF243F70.toInt()
        }

        val descriptionColor = if (dark) {
            0xFFD0E2F8.toInt()
        } else {
            0xFF647A9B.toInt()
        }

        val accentColor = if (dark) {
            DARK_ACCENT
        } else {
            LIGHT_BUTTON
        }

        // FUNGSI BACKGROUND DENGAN SUDUT MELENGKUNG
        fun setRoundedBackground(
            viewId: Int,
            color: Int,
            radius: Float
        ) {
            val view = activity.findViewById<View>(viewId) ?: return

            view.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = radius
                setColor(color)
            }
        }

        // BACKGROUND ILUSTRASI
        setRoundedBackground(
            R.id.welcomeHeroPanel,
            heroColor,
            30f
        )

        // BACKGROUND KARTU FITUR
        setRoundedBackground(
            R.id.featureCard1,
            featureColor,
            18f
        )

        setRoundedBackground(
            R.id.featureCard2,
            featureColor,
            18f
        )

        setRoundedBackground(
            R.id.featureCard3,
            featureColor,
            18f
        )

        // BACKGROUND REMINDER
        setRoundedBackground(
            R.id.cardWelcomeReminder,
            reminderColor,
            18f
        )

        // WARNA JUDUL REMINDER
        activity.findViewById<TextView>(
            R.id.tvWelcomeReminderTitle
        )?.setTextColor(titleColor)

        // WARNA DESKRIPSI REMINDER
        activity.findViewById<TextView>(
            R.id.tvWelcomeReminderDescription
        )?.setTextColor(descriptionColor)

        // WARNA TEKS KARTU FITUR
        activity.findViewById<TextView>(
            R.id.featureText1
        )?.setTextColor(accentColor)

        activity.findViewById<TextView>(
            R.id.featureText2
        )?.setTextColor(accentColor)

        activity.findViewById<TextView>(
            R.id.featureText3
        )?.setTextColor(accentColor)
    }
}
