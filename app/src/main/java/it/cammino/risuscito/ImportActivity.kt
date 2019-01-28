package it.cammino.risuscito

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.afollestad.materialdialogs.MaterialDialog
import it.cammino.risuscito.services.XmlImportService
import it.cammino.risuscito.ui.ThemeableActivity
import java.util.*

class ImportActivity : AppCompatActivity() {

    private val importFinishBRec = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //Implement UI change code here once notification is received
            Log.d(javaClass.name, "ACTION_FINISH")
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = intent.data
        if (data != null) {
            Log.d(TAG, "onCreate: data = $data")
            Log.d(TAG, "onCreate: schema = " + data.scheme)
            intent.data = null
//            val dialog = MaterialDialog.Builder(this)
//                    .title(R.string.app_name)
//                    .content(R.string.dialog_import)
//                    .positiveText(android.R.string.yes)
//                    .negativeText(android.R.string.no)
//                    .onPositive { _, _ ->
//                        val i = Intent(this@ImportActivity, XmlImportService::class.java)
//                        i.action = XmlImportService.ACTION_URL
//                        i.data = data
//                        startService(i)
//                    }
//                    .onNegative { _, _ -> finish() }
//                    .show()
//            dialog.setOnKeyListener(DialogInterface.OnKeyListener { arg0, keyCode, event ->
//                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
//                    arg0.dismiss()
//                    finish()
//                    return@OnKeyListener true
//                }
//                false
//            })
//            dialog.setCancelable(false)
            MaterialDialog(this)
                    .show {
                        title(R.string.app_name)
                        message(R.string.dialog_import)
                        positiveButton(android.R.string.yes) {
                            val i = Intent(this@ImportActivity, XmlImportService::class.java)
                            i.action = XmlImportService.ACTION_URL
                            i.data = data
                            startService(i)
                        }
                        negativeButton(android.R.string.no) {
                            finish()
                        }
                        cancelable(false)
                        cancelOnTouchOutside(false)
                    }
                    .setOnKeyListener { arg0, keyCode, event ->
                        if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                            arg0.dismiss()
                            finish()
                            true
                        } else
                            false
                    }
            //registra un receiver per ricevere la notifica di completamento import e potersi terminare
            LocalBroadcastManager.getInstance(applicationContext).registerReceiver(importFinishBRec, IntentFilter(
                    XmlImportService.ACTION_FINISH))
        }

    }

    public override fun onDestroy() {
        try {
            LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(importFinishBRec)
        } catch (e: IllegalArgumentException) {
            Log.e(javaClass.name, e.localizedMessage, e)
        }

        super.onDestroy()
    }

    override fun attachBaseContext(newBase: Context) {
        var mNewBase = newBase

        val config = Configuration()

        //lingua
        val sp = PreferenceManager
                .getDefaultSharedPreferences(mNewBase)
        val language = sp.getString(Utility.SYSTEM_LANGUAGE, "")
        Log.d(TAG, "attachBaseContext - language: " + language!!)
        //ho settato almeno una volta la lingua --> imposto quella
        if (language.isNotEmpty()) {
            val locale = Locale(language)
            Locale.setDefault(locale)
            ThemeableActivity.setSystemLocalWrapper(config, locale)
        } else {
//            val mEditor = sp.edit()
            val mLanguage: String = when (ThemeableActivity.getSystemLocalWrapper(mNewBase.resources.configuration).language) {
                "uk" -> "uk"
                "en" -> "en"
                else -> "it"
            }
//            mEditor.putString(Utility.SYSTEM_LANGUAGE, mLanguage)
//            mEditor.apply()
            sp.edit { putString(Utility.SYSTEM_LANGUAGE, mLanguage) }
            val locale = Locale(mLanguage)
            Locale.setDefault(locale)
            ThemeableActivity.setSystemLocalWrapper(config, locale)
        }// non è ancora stata impostata nessuna lingua nelle impostazioni --> setto una lingua selezionabile oppure IT se non presente

        //fond dimension
        try {
            val actualScale = mNewBase.resources.configuration.fontScale
            Log.d(javaClass.toString(), "actualScale: $actualScale")
            val systemScale = Settings.System.getFloat(contentResolver, Settings.System.FONT_SCALE)
            Log.d(javaClass.toString(), "systemScale: $systemScale")
            if (actualScale != systemScale)
                config.fontScale = systemScale
        } catch (e: Settings.SettingNotFoundException) {
            Log.e(javaClass.toString(), "Settings.SettingNotFoundException - FUNZIONE RESIZE TESTO NON SUPPORTATA: " + e.localizedMessage)
        } catch (e: NullPointerException) {
            Log.e(javaClass.toString(), "NullPointerException - FUNZIONE RESIZE TESTO NON SUPPORTATA: " + e.localizedMessage)
        }

        if (LUtils.hasJB()) {
            mNewBase = mNewBase.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            mNewBase.resources.updateConfiguration(config, mNewBase.resources.displayMetrics)
        }

        //Calligraphy
//        super.attachBaseContext(ViewPumpContextWrapper.wrap(mNewBase))
        super.attachBaseContext(mNewBase)
    }

    companion object {
        internal val TAG = ImportActivity::class.java.canonicalName
    }

}
