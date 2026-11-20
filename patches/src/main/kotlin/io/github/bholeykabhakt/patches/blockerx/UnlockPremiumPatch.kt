package io.github.bholeykabhakt.patches.blockerx

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.mutableClassDefBy
import app.morphe.patcher.patch.patch
import io.github.bholeykabhakt.patches.utils.returnEarly

/**
 * Adaptación de Unlock Premium para BlockerX 5.1.10 (versionCode 5110).
 *
 * Verificado estáticamente sobre el XAPK recibido:
 * - clase BlockerXAppSharePref localizada en classes.dex
 * - 12 métodos presentes, todos devuelven boolean (Z)
 *
 * NO verificado en un dispositivo ni compilado con Morphe.
 * A diferencia del parche original, NO altera la navegación inicial:
 * openMainActivityWithPassword(...) ya no existe con ese nombre.
 */
private val UNLOCK_FLAGS = setOf(
    "getSUB_STATUS",
    "getSUB_STATUS_LITE",
    "getIS_ACTIVE_PREMIUM_PLUS",
    "getIS_ACTIVE_CODI_MODE_PREMIUM",
    "getIS_ACTIVE_DESKTOP_PREMIUM",
    "getIS_ACTIVE_ED_COURSE_PREMIUM",
    "getIS_ACTIVE_URGES_MODE_PREMIUM",
    "getIS_COINS_PURCHASED_FOR_LOGO",
    "getIS_COINS_PURCHASED_FOR_CUSTOMIZE_BLOCK_SCREEN",
    "getIS_COINS_PURCHASED_FOR_APP_NAME",
    "getIS_COINS_PURCHASED_FOR_SILENCE_NOTIFICATIONS",
    "getIS_DONATION_PURCHASED",
)

@Suppress("unused")
val unlockPremiumPatch = patch(
    name = "Unlock Premium",
) {
    compatibleWith(
        Compatibility(
            name = "BlockerX",
            packageName = "io.funswitch.blocker",
            targets = listOf(AppTarget(version = "5.1.10")),
        ),
    )

    execute {
        val preferences = mutableClassDefBy(
            "Lio/funswitch/blocker/utils/sharePrefUtils/BlockerXAppSharePref;"
        )
        val matches = preferences.methods.filter {
            it.name in UNLOCK_FLAGS && it.returnType == "Z"
        }
        val matchedNames = matches.map { it.name }.toSet()
        check(matches.size == UNLOCK_FLAGS.size && matchedNames == UNLOCK_FLAGS) {
            "BlockerX 5.1.10: expected ${UNLOCK_FLAGS.size} boolean getters, " +
                "found ${matches.size}: $matchedNames. Missing: ${UNLOCK_FLAGS - matchedNames}"
        }
        matches.forEach { it.returnEarly(true) }
    }
}
