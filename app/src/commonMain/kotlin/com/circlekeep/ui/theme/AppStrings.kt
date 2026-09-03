package com.circlekeep.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

interface AppStrings {
    val appName: String
    val settings: String
    val theme: String
    val light: String
    val dark: String
    val system: String
    val language: String
    val eventNotifications: String
    val eventNotificationsDesc: String
    val purchaseApp: String
    val restorePurchases: String
    val exportData: String
    val importData: String
    val sendFeedback: String
    val deleteAllRecords: String
    val appVersion: String
    val developer: String
    val status: String
    val proVersionActive: String
    val cancel: String
    val ok: String
    val delete: String
    val home: String
    val favorites: String
    val groups: String
    val events: String
    val search: String
    val all: String
    val noResults: String
    val noContacts: String
    val selected: String
    val marriageAnniversary: String
    val birthday: String
    val nickname: String
    val notes: String
}

object EnStrings : AppStrings {
    override val appName = "CircleKeep"
    override val settings = "Settings"
    override val theme = "Theme"
    override val light = "Light"
    override val dark = "Dark"
    override val system = "System"
    override val language = "Language"
    override val eventNotifications = "Event Notifications"
    override val eventNotificationsDesc = "Notify for birthdays and marriage anniversaries"
    override val purchaseApp = "Purchase App (Remove Ads)"
    override val restorePurchases = "Restore Purchases"
    override val exportData = "Export Data"
    override val importData = "Import Data"
    override val sendFeedback = "Send Feedback"
    override val deleteAllRecords = "Delete All Records"
    override val appVersion = "App Version"
    override val developer = "Developer"
    override val status = "Status"
    override val proVersionActive = "Pro Version Active"
    override val cancel = "Cancel"
    override val ok = "OK"
    override val delete = "Delete"
    override val home = "Home"
    override val favorites = "Favorites"
    override val groups = "Groups"
    override val events = "Events"
    override val search = "Search"
    override val all = "All"
    override val noResults = "No results found"
    override val noContacts = "No contacts found"
    override val selected = "selected"
    override val marriageAnniversary = "Marriage Anniversary"
    override val birthday = "Birthday"
    override val nickname = "Nickname"
    override val notes = "Notes"
}

object HiStrings : AppStrings {
    override val appName = "सर्कल कीप"
    override val settings = "सेटिंग्स"
    override val theme = "थीम"
    override val light = "लाइट"
    override val dark = "डार्क"
    override val system = "सिस्टम"
    override val language = "भाषा"
    override val eventNotifications = "ईवेंट सूचनाएं"
    override val eventNotificationsDesc = "जन्मदिन और शादी की सालगिरह के लिए सूचित करें"
    override val purchaseApp = "ऐप खरीदें (विज्ञापन हटाएं)"
    override val restorePurchases = "खरीद पुनर्स्थापित करें"
    override val exportData = "डेटा निर्यात करें"
    override val importData = "डेटा आयात करें"
    override val sendFeedback = "प्रतिक्रिया भेजें"
    override val deleteAllRecords = "सभी रिकॉर्ड हटाएं"
    override val appVersion = "ऐप वर्शन"
    override val developer = "डेवलपर"
    override val status = "स्थिति"
    override val proVersionActive = "प्रो वर्शन सक्रिय"
    override val cancel = "रद्द करें"
    override val ok = "ठीक है"
    override val delete = "हटाएं"
    override val home = "होम"
    override val favorites = "पसंदीदा"
    override val groups = "समूह"
    override val events = "ईवेंट्स"
    override val search = "खोजें"
    override val all = "सभी"
    override val noResults = "कोई परिणाम नहीं मिला"
    override val noContacts = "कोई संपर्क नहीं मिला"
    override val selected = "चयनित"
    override val marriageAnniversary = "शादी की सालगिरह"
    override val birthday = "जन्मदिन"
    override val nickname = "उपनाम"
    override val notes = "नोट्स"
}

object EsStrings : AppStrings {
    override val appName = "CircleKeep"
    override val settings = "Ajustes"
    override val theme = "Tema"
    override val light = "Claro"
    override val dark = "Oscuro"
    override val system = "Sistema"
    override val language = "Idioma"
    override val eventNotifications = "Notificaciones de eventos"
    override val eventNotificationsDesc = "Notificar cumpleaños y aniversarios de boda"
    override val purchaseApp = "Comprar aplicación (Quitar anuncios)"
    override val restorePurchases = "Restaurar compras"
    override val exportData = "Exportar datos"
    override val importData = "Importar datos"
    override val sendFeedback = "Enviar comentarios"
    override val deleteAllRecords = "Eliminar todos los registros"
    override val appVersion = "Versión de la aplicación"
    override val developer = "Desarrollador"
    override val status = "Estado"
    override val proVersionActive = "Versión Pro activa"
    override val cancel = "Cancelar"
    override val ok = "Aceptar"
    override val delete = "Eliminar"
    override val home = "Inicio"
    override val favorites = "Favoritos"
    override val groups = "Grupos"
    override val events = "Eventos"
    override val search = "Buscar"
    override val all = "Todos"
    override val noResults = "No se encontraron resultados"
    override val noContacts = "No se encontraron contactos"
    override val selected = "seleccionado"
    override val marriageAnniversary = "Aniversario de matrimonio"
    override val birthday = "Cumpleaños"
    override val nickname = "Apodo"
    override val notes = "Notas"
}

internal val LocalAppStrings = staticCompositionLocalOf<AppStrings> { EnStrings }

object AppStringsProvider {
    val current: AppStrings
        @Composable
        @ReadOnlyComposable
        get() = LocalAppStrings.current
}
