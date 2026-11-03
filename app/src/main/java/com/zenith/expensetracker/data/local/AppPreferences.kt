package com.zenith.expensetracker.data.local

import android.content.Context
import android.content.SharedPreferences
import com.zenith.expensetracker.data.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class AccentTheme(val displayName: String, val primaryColorHex: String) {
    EMERALD("Emerald Green", "#10B981"),
    SAPPHIRE("Sapphire Blue", "#2563EB"),
    VIOLET("Royal Violet", "#8B5CF6"),
    SUNSET("Sunset Orange", "#F97316"),
    RUBY("Ruby Crimson", "#E11D48"),
    CYBER_TEAL("Cyber Teal", "#06B6D4"),
    ROSE_GOLD("Rose Pink", "#EC4899")
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentTheme: AccentTheme = AccentTheme.EMERALD,
    val dynamicColor: Boolean = false,
    val amoledDark: Boolean = false,
    val dateFormat: String = "dd MMM yyyy",
    val showDecimals: Boolean = true,
    val privacyMode: Boolean = false,
    val defaultTransactionType: TransactionType = TransactionType.EXPENSE,
    val budgetAlertThreshold: Int = 80,
    val defaultLowBalanceThreshold: Double = 500.0
)

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("easy_expense_user_preferences", Context.MODE_PRIVATE)

    private val _preferences = MutableStateFlow(loadPreferences())
    val preferences: StateFlow<UserPreferences> = _preferences.asStateFlow()

    private fun loadPreferences(): UserPreferences {
        val themeModeStr = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val accentThemeStr = prefs.getString(KEY_ACCENT_THEME, AccentTheme.EMERALD.name) ?: AccentTheme.EMERALD.name
        val dynamicColor = prefs.getBoolean(KEY_DYNAMIC_COLOR, false)
        val amoledDark = prefs.getBoolean(KEY_AMOLED_DARK, false)
        val dateFormat = prefs.getString(KEY_DATE_FORMAT, "dd MMM yyyy") ?: "dd MMM yyyy"
        val showDecimals = prefs.getBoolean(KEY_SHOW_DECIMALS, true)
        val privacyMode = prefs.getBoolean(KEY_PRIVACY_MODE, false)
        val defaultTxTypeStr = prefs.getString(KEY_DEFAULT_TX_TYPE, TransactionType.EXPENSE.name) ?: TransactionType.EXPENSE.name
        val budgetThreshold = prefs.getInt(KEY_BUDGET_THRESHOLD, 80)
        val lowBalThreshold = prefs.getFloat(KEY_LOW_BAL_THRESHOLD, 500.0f).toDouble()

        val themeMode = try { ThemeMode.valueOf(themeModeStr) } catch (_: Exception) { ThemeMode.SYSTEM }
        val accentTheme = try { AccentTheme.valueOf(accentThemeStr) } catch (_: Exception) { AccentTheme.EMERALD }
        val defaultTxType = try { TransactionType.valueOf(defaultTxTypeStr) } catch (_: Exception) { TransactionType.EXPENSE }

        return UserPreferences(
            themeMode = themeMode,
            accentTheme = accentTheme,
            dynamicColor = dynamicColor,
            amoledDark = amoledDark,
            dateFormat = dateFormat,
            showDecimals = showDecimals,
            privacyMode = privacyMode,
            defaultTransactionType = defaultTxType,
            budgetAlertThreshold = budgetThreshold,
            defaultLowBalanceThreshold = lowBalThreshold
        )
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _preferences.value = _preferences.value.copy(themeMode = mode)
    }

    fun setAccentTheme(accent: AccentTheme) {
        prefs.edit().putString(KEY_ACCENT_THEME, accent.name).apply()
        _preferences.value = _preferences.value.copy(accentTheme = accent)
    }

    fun setDynamicColor(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
        _preferences.value = _preferences.value.copy(dynamicColor = enabled)
    }

    fun setAmoledDark(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AMOLED_DARK, enabled).apply()
        _preferences.value = _preferences.value.copy(amoledDark = enabled)
    }

    fun setDateFormat(format: String) {
        prefs.edit().putString(KEY_DATE_FORMAT, format).apply()
        _preferences.value = _preferences.value.copy(dateFormat = format)
    }

    fun setShowDecimals(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_DECIMALS, show).apply()
        _preferences.value = _preferences.value.copy(showDecimals = show)
    }

    fun setPrivacyMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PRIVACY_MODE, enabled).apply()
        _preferences.value = _preferences.value.copy(privacyMode = enabled)
    }

    fun setDefaultTransactionType(type: TransactionType) {
        prefs.edit().putString(KEY_DEFAULT_TX_TYPE, type.name).apply()
        _preferences.value = _preferences.value.copy(defaultTransactionType = type)
    }

    fun setBudgetAlertThreshold(threshold: Int) {
        prefs.edit().putInt(KEY_BUDGET_THRESHOLD, threshold).apply()
        _preferences.value = _preferences.value.copy(budgetAlertThreshold = threshold)
    }

    fun setDefaultLowBalanceThreshold(threshold: Double) {
        prefs.edit().putFloat(KEY_LOW_BAL_THRESHOLD, threshold.toFloat()).apply()
        _preferences.value = _preferences.value.copy(defaultLowBalanceThreshold = threshold)
    }

    companion object {
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_ACCENT_THEME = "key_accent_theme"
        private const val KEY_DYNAMIC_COLOR = "key_dynamic_color"
        private const val KEY_AMOLED_DARK = "key_amoled_dark"
        private const val KEY_DATE_FORMAT = "key_date_format"
        private const val KEY_SHOW_DECIMALS = "key_show_decimals"
        private const val KEY_PRIVACY_MODE = "key_privacy_mode"
        private const val KEY_DEFAULT_TX_TYPE = "key_default_tx_type"
        private const val KEY_BUDGET_THRESHOLD = "key_budget_threshold"
        private const val KEY_LOW_BAL_THRESHOLD = "key_low_bal_threshold"
    }
}
