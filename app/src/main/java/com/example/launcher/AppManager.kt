package com.example.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.Collator
import java.util.Locale

class AppManager(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val collator = Collator.getInstance(Locale("pt", "BR")).apply {
        strength = Collator.PRIMARY
    }

    suspend fun getInstalledApps(
        favoritePackages: Set<String> = emptySet(),
        hiddenPackages: Set<String> = emptySet()
    ): List<AppItem> = withContext(Dispatchers.IO) {
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = try {
            packageManager.queryIntentActivities(intent, 0)
        } catch (e: Exception) {
            emptyList()
        }

        val apps = mutableListOf<AppItem>()
        val myPackageName = context.packageName

        for (info in resolveInfos) {
            val pkg = info.activityInfo.packageName
            // Hide own launcher from app drawer to avoid recursive loops
            if (pkg == myPackageName) continue

            val label = try {
                info.loadLabel(packageManager).toString()
            } catch (e: Exception) {
                pkg
            }

            val icon = try {
                info.loadIcon(packageManager)
            } catch (e: Exception) {
                null
            }

            apps.add(
                AppItem(
                    packageName = pkg,
                    label = label,
                    className = info.activityInfo.name,
                    icon = icon,
                    isFavorite = favoritePackages.contains(pkg),
                    isHiddenInFocus = hiddenPackages.contains(pkg)
                )
            )
        }

        apps.sortedWith { a, b -> collator.compare(a.label, b.label) }
    }

    fun launchApp(app: AppItem): Boolean {
        return try {
            val launchIntent = packageManager.getLaunchIntentForPackage(app.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                Toast.makeText(context, "Não foi possível abrir esse aplicativo.", Toast.LENGTH_SHORT).show()
                false
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Não foi possível abrir esse aplicativo.", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun openAppInfo(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Não foi possível abrir informações do app.", Toast.LENGTH_SHORT).show()
        }
    }

    fun uninstallApp(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
                data = Uri.fromParts("package", packageName, null)
                putExtra(Intent.EXTRA_RETURN_RESULT, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback for older/newer APIs
            try {
                val intent = Intent(Intent.ACTION_DELETE).apply {
                    data = Uri.fromParts("package", packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Não foi possível iniciar desinstalação.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openDefaultLauncherSettings() {
        try {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Abra Configurações do Android > Apps > App Padrão", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun openSystemSettings() {
        try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Não foi possível abrir configurações.", Toast.LENGTH_SHORT).show()
        }
    }
}
