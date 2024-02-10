package com.raveline.mail

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.raveline.mail.mlkit.TextTranslator
import com.raveline.mail.ui.navigation.HomeNavHost
import com.raveline.mail.ui.theme.MAILTheme
import com.raveline.mail.util.FileUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
     val TAG: String = "TAG_TRANSLATE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MAILTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    HomeNavHost(navController = navController)

                    val textTranslator = TextTranslator(FileUtil(this))
                    val text = "What the fuck is this?"
                    textTranslator.getDetectedLanguage(
                        text = text,
                        onSuccess = {
                            textTranslator.verifyDownloadModel(
                                modelCode = it.code,
                                onSuccess = {
                                    Log.i(TAG, "Available model for ${it.name}")
                                },
                                onFailure = { error ->
                                    Log.e(TAG, error.toString())
                                }
                            )
                        },
                        onFailure = {
                            Log.e(TAG, it)
                        }
                    )
                }
            }
        }
    }

}

