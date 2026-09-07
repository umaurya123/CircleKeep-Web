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
    val visitWebsite: String
    val hideQr: String
    val hideQrDesc: String
    val eventNotifications: String
    val eventNotificationsDesc: String
    val purchaseApp: String
    val restorePurchases: String
    val exportData: String
    val exportAndEmailData: String
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
    val addContact: String
    val editContact: String
    val firstName: String
    val middleName: String
    val lastName: String
    val address: String
    val cellPhone: String
    val officePhone: String
    val email: String
    val workEmail: String
    val partner: String
    val partnerDob: String
    val children: String
    val addChild: String
    val companyName: String
    val collegeName: String
    val siblings: String
    val petName: String
    val selectImage: String
    val removeImage: String
    val memoryPhoto: String
    val selectPicture: String
    val day: String
    val month: String
    val dob: String
    val marriageDate: String
    val save: String
    val import: String
    val showMore: String
    val showLess: String
    val savingIn: String
    val seconds: String
    val purchaseProToSkip: String
    val selectGroups: String
    val importContactTo: String
    val chooseWhereToImport: String
    val mainFriendSection: String
    val partnerSection: String
    val newChildRecord: String
    val updateExistingChild: String
    val selectChildToUpdate: String
    val shareContact: String
    val scanToAdd: String
    val close: String
    val viewFull: String
    val created: String
    val lastModified: String
    val months: List<String>
    val partnerTypes: Map<String, String>
    val childTypes: Map<String, String>
    val none: String
    val child: String
    val childsPartner: String
    val invalidDay: String
    val invalidEmail: String
    val deleteFriendConfirm: String
    val age: String
    val yearUnit: String
    val monthUnit: String
    val today: String
    val tomorrow: String
    val inXDays: String
    val thisWeek: String
    val laterThisMonth: String
    val noEvents: String
    val sortFirstLast: String
    val sortLastFirst: String
    val sortGroup: String
    val sortBirthday: String
    val sortAnniversary: String
    val sortCreationDate: String
    val sortLastModified: String
}

object EnStrings : AppStrings {
    override val appName = "CircleKeep"
    override val settings = "Settings"
    override val theme = "Theme"
    override val light = "Light"
    override val dark = "Dark"
    override val system = "System"
    override val language = "Language"
    override val visitWebsite = "Visit CircleKeep Website"
    override val hideQr = "Hide QR Features"
    override val hideQrDesc = "Hide QR scanning and sharing options"
    override val eventNotifications = "Event Notifications"
    override val eventNotificationsDesc = "Notify for birthdays and marriage anniversaries"
    override val purchaseApp = "Purchase App (Remove Ads)"
    override val restorePurchases = "Restore Purchases"
    override val exportData = "Export Data"
    override val exportAndEmailData = "Export and Email Data"
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
    override val addContact = "Add Contact"
    override val editContact = "Edit Contact"
    override val firstName = "First Name"
    override val middleName = "Middle Name"
    override val lastName = "Last Name"
    override val address = "Address"
    override val cellPhone = "Cell Phone"
    override val officePhone = "Office Phone"
    override val email = "Email"
    override val workEmail = "Work Email"
    override val partner = "Partner"
    override val partnerDob = "Partner DOB"
    override val children = "Children"
    override val addChild = "Add Child"
    override val companyName = "Company Name"
    override val collegeName = "College Name"
    override val siblings = "Siblings"
    override val petName = "Pet Name"
    override val selectImage = "Select Image"
    override val removeImage = "Remove Image"
    override val memoryPhoto = "Memory Photo"
    override val selectPicture = "Select Picture"
    override val day = "Day"
    override val month = "Month"
    override val dob = "DOB"
    override val marriageDate = "Marriage Date"
    override val save = "Save"
    override val import = "Import"
    override val showMore = "Show More"
    override val showLess = "Show Less"
    override val savingIn = "Saving in"
    override val seconds = "seconds"
    override val purchaseProToSkip = "Purchase Pro version to skip ads and saving delay!"
    override val selectGroups = "Select Groups"
    override val importContactTo = "Import Contact To..."
    override val chooseWhereToImport = "Choose where to import this contact's details."
    override val mainFriendSection = "Main Friend Section"
    override val partnerSection = "Partner Section"
    override val newChildRecord = "New Child Record"
    override val updateExistingChild = "Update Existing Child..."
    override val selectChildToUpdate = "Select Child to Update"
    override val shareContact = "Share Contact"
    override val scanToAdd = "Scan to add to contacts"
    override val close = "Close"
    override val viewFull = "View Full"
    override val created = "Created"
    override val lastModified = "Last Modified"
    override val months = listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    override val partnerTypes = mapOf("" to "Select Type", "Spouse" to "Spouse", "Fiance" to "Fiance", "Boyfriend" to "Boyfriend", "Girlfriend" to "Girlfriend", "Husband" to "Husband", "Wife" to "Wife", "Other" to "Other")
    override val childTypes = mapOf("" to "Select Type", "Daughter" to "Daughter", "Son" to "Son")
    override val none = "None"
    override val child = "Child"
    override val childsPartner = "Child's Partner"
    override val invalidDay = "Invalid day"
    override val invalidEmail = "Invalid email format"
    override val deleteFriendConfirm = "Are you sure you want to delete this friend?"
    override val age = "Age"
    override val yearUnit = "Year(s)"
    override val monthUnit = "Month(s)"
    override val today = "Today"
    override val tomorrow = "Tomorrow"
    override val inXDays = "In %d days"
    override val thisWeek = "This Week"
    override val laterThisMonth = "Later this Month"
    override val noEvents = "No events in the next 30 days"
    override val sortFirstLast = "First, Last Name"
    override val sortLastFirst = "Last, First Name"
    override val sortGroup = "Group"
    override val sortBirthday = "Birthday"
    override val sortAnniversary = "Marriage Anniversary"
    override val sortCreationDate = "Creation Date"
    override val sortLastModified = "Last Modified"
}

object HiStrings : AppStrings {
    override val appName = "सर्कल कीप"
    override val settings = "सेटिंग्स"
    override val theme = "थीम"
    override val light = "लाइट"
    override val dark = "डार्क"
    override val system = "सिस्टम"
    override val language = "भाषा"
    override val visitWebsite = "CircleKeep वेबसाइट देखें"
    override val hideQr = "QR फीचर्स छिपाएं"
    override val hideQrDesc = "QR स्कैनिंग और शेयरिंग विकल्प छिपाएं"
    override val eventNotifications = "ईवेंट सूचनाएं"
    override val eventNotificationsDesc = "जन्मदिन और शादी की सालगिरह के लिए सूचित करें"
    override val purchaseApp = "ऐप खरीदें (विज्ञापन हटाएं)"
    override val restorePurchases = "खरीद पुनर्स्थापित करें"
    override val exportData = "डेटा निर्यात करें"
    override val exportAndEmailData = "निर्यात करें और ईमेल भेजें"
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
    override val addContact = "संपर्क जोड़ें"
    override val editContact = "संपर्क संपादित करें"
    override val firstName = "पहला नाम"
    override val middleName = "मध्य नाम"
    override val lastName = "अंतिम नाम"
    override val address = "पता"
    override val cellPhone = "मोबाइल फोन"
    override val officePhone = "कार्यालय फोन"
    override val email = "ईमेल"
    override val workEmail = "कार्य ईमेल"
    override val partner = "साथी"
    override val partnerDob = "पार्टनर की जन्म तिथि"
    override val children = "बच्चे"
    override val addChild = "बच्चा जोड़ें"
    override val companyName = "कंपनी का नाम"
    override val collegeName = "कॉलेज का नाम"
    override val siblings = "भाई-बहन"
    override val petName = "पालतू जानवर का नाम"
    override val selectImage = "फोटो चुनें"
    override val removeImage = "फोटो हटाएं"
    override val memoryPhoto = "यादगार फोटो"
    override val selectPicture = "फोटो चुनें"
    override val day = "दिन"
    override val month = "महीना"
    override val dob = "जन्म तिथि"
    override val marriageDate = "शादी की तारीख"
    override val save = "सहेजें"
    override val import = "आयात करें"
    override val showMore = "अधिक दिखाएं"
    override val showLess = "कम दिखाएं"
    override val savingIn = "सहेजा जा रहा है"
    override val seconds = "सेकंड में"
    override val purchaseProToSkip = "विज्ञापन और देरी से बचने के लिए प्रो वर्शन खरीदें!"
    override val selectGroups = "समूह चुनें"
    override val importContactTo = "संपर्क आयात करें..."
    override val chooseWhereToImport = "चुनें कि इस संपर्क का विवरण कहाँ आयात करना है।"
    override val mainFriendSection = "मुख्य मित्र अनुभाग"
    override val partnerSection = "साथी अनुभाग"
    override val newChildRecord = "नया बच्चा रिकॉर्ड"
    override val updateExistingChild = "मौजूदा बच्चे को अपडेट करें..."
    override val selectChildToUpdate = "अपडेट करने के लिए बच्चे का चयन करें"
    override val shareContact = "संपर्क साझा करें"
    override val scanToAdd = "संपर्क में जोड़ने के लिए स्कैन करें"
    override val close = "बंद करें"
    override val viewFull = "पूरा देखें"
    override val created = "बनाया गया"
    override val lastModified = "अंतिम संशोधन"
    override val months = listOf("जनवरी", "फरवरी", "मार्च", "अप्रैल", "मई", "जून", "जुलाई", "अगस्त", "सितंबर", "अक्टूबर", "नवंबर", "दिसंबर")
    override val partnerTypes = mapOf("" to "प्रकार चुनें", "Spouse" to "जीवनसाथी", "Fiance" to "मंगेतर", "Boyfriend" to "बॉयफ्रेंड", "Girlfriend" to "गर्लफ्रेंड", "Husband" to "पति", "Wife" to "पत्नी", "Other" to "अन्य")
    override val childTypes = mapOf("" to "प्रकार चुनें", "Daughter" to "बेटी", "Son" to "बेटा")
    override val none = "कोई नहीं"
    override val child = "बच्चा"
    override val childsPartner = "बच्चे का साथी"
    override val invalidDay = "अमान्य दिन"
    override val invalidEmail = "ईमेल का प्रारूप अमान्य है"
    override val deleteFriendConfirm = "क्या आप वाकई इस मित्र को हटाना चाहते हैं?"
    override val age = "उम्र"
    override val yearUnit = "साल"
    override val monthUnit = "महीना"
    override val today = "आज"
    override val tomorrow = "कल"
    override val inXDays = "%d दिनों में"
    override val thisWeek = "इस सप्ताह"
    override val laterThisMonth = "इस महीने बाद में"
    override val noEvents = "अगले 30 दिनों में कोई ईवेंट नहीं है"
    override val sortFirstLast = "पहला, अंतिम नाम"
    override val sortLastFirst = "अंतिम, पहला नाम"
    override val sortGroup = "समूह"
    override val sortBirthday = "जन्मदिन"
    override val sortAnniversary = "शादी की सालगिरह"
    override val sortCreationDate = "निर्माण तिथि"
    override val sortLastModified = "अंतिम संशोधन"
}

object EsStrings : AppStrings {
    override val appName = "CircleKeep"
    override val settings = "Ajustes"
    override val theme = "Tema"
    override val light = "Claro"
    override val dark = "Oscuro"
    override val system = "Sistema"
    override val language = "Idioma"
    override val visitWebsite = "Visitar el sitio web de CircleKeep"
    override val hideQr = "Ocultar funciones QR"
    override val hideQrDesc = "Ocultar opciones de escaneo y uso compartido de QR"
    override val eventNotifications = "Notificaciones de eventos"
    override val eventNotificationsDesc = "Notificar cumpleaños y aniversarios de boda"
    override val purchaseApp = "Comprar aplicación (Quitar anuncios)"
    override val restorePurchases = "Restaurar compras"
    override val exportData = "Exportar datos"
    override val exportAndEmailData = "Exportar y enviar por correo"
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
    override val addContact = "Agregar contacto"
    override val editContact = "Editar contacto"
    override val firstName = "Nombre"
    override val middleName = "Segundo nombre"
    override val lastName = "Apellido"
    override val address = "Dirección"
    override val cellPhone = "Teléfono celular"
    override val officePhone = "Teléfono de oficina"
    override val email = "Correo electrónico"
    override val workEmail = "Correo electrónico de trabajo"
    override val partner = "Pareja"
    override val partnerDob = "Fecha de nacimiento de la pareja"
    override val children = "Hijos"
    override val addChild = "Agregar hijo"
    override val companyName = "Nombre de la empresa"
    override val collegeName = "Nombre de la universidad"
    override val siblings = "Hermanos"
    override val petName = "Nombre de la mascota"
    override val selectImage = "Seleccionar imagen"
    override val removeImage = "Quitar imagen"
    override val memoryPhoto = "Foto de recuerdo"
    override val selectPicture = "Seleccionar foto"
    override val day = "Día"
    override val month = "Mes"
    override val dob = "Fecha de nacimiento"
    override val marriageDate = "Fecha de matrimonio"
    override val save = "Guardar"
    override val import = "Importar"
    override val showMore = "Mostrar más"
    override val showLess = "Mostrar menos"
    override val savingIn = "Guardando en"
    override val seconds = "segundos"
    override val purchaseProToSkip = "¡Compre la versión Pro para omitir anuncios y el retraso de guardado!"
    override val selectGroups = "Seleccionar grupos"
    override val importContactTo = "Importar contacto a..."
    override val chooseWhereToImport = "Elija dónde importar los detalles de este contacto."
    override val mainFriendSection = "Sección principal de amigos"
    override val partnerSection = "Sección de pareja"
    override val newChildRecord = "Nuevo registro de hijo"
    override val updateExistingChild = "Actualizar hijo existente..."
    override val selectChildToUpdate = "Seleccionar hijo para actualizar"
    override val shareContact = "Compartir contacto"
    override val scanToAdd = "Escanear para agregar a contactos"
    override val close = "Cerrar"
    override val viewFull = "Ver completo"
    override val created = "Creado"
    override val lastModified = "Última modificación"
    override val months = listOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
    override val partnerTypes = mapOf("" to "Seleccionar tipo", "Spouse" to "Cónyuge", "Fiance" to "Prometido/a", "Boyfriend" to "Novio", "Girlfriend" to "Novia", "Husband" to "Esposo", "Wife" to "Esposa", "Other" to "Otro")
    override val childTypes = mapOf("" to "Seleccionar tipo", "Daughter" to "Hija", "Son" to "Hijo")
    override val none = "Ninguno"
    override val child = "Hijo"
    override val childsPartner = "Pareja del hijo"
    override val invalidDay = "Día no válido"
    override val invalidEmail = "Formato de correo electrónico no válido"
    override val deleteFriendConfirm = "¿Estás seguro de que quieres eliminar a este amigo?"
    override val age = "Edad"
    override val yearUnit = "Año(s)"
    override val monthUnit = "Mes(es)"
    override val today = "Hoy"
    override val tomorrow = "Mañana"
    override val inXDays = "En %d días"
    override val thisWeek = "Esta semana"
    override val laterThisMonth = "Más tarde este mes"
    override val noEvents = "No hay eventos en los próximos 30 días"
    override val sortFirstLast = "Nombre, Apellido"
    override val sortLastFirst = "Apellido, Nombre"
    override val sortGroup = "Grupo"
    override val sortBirthday = "Cumpleaños"
    override val sortAnniversary = "Aniversario de matrimonio"
    override val sortCreationDate = "Fecha de creación"
    override val sortLastModified = "Última modificación"
}

internal val LocalAppStrings = staticCompositionLocalOf<AppStrings> { EnStrings }

object AppStringsProvider {
    val current: AppStrings
        @Composable
        @ReadOnlyComposable
        get() = LocalAppStrings.current
}
