plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.androidx.datastore.preferences)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.ktor.client.core)
            implementation(libs.navigation.compose)
            // Added icons
            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.activity.compose)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.room.ktx)
            implementation(compose.uiTooling)
            implementation(libs.compose.tooling.preview)
            
            implementation(libs.accompanist.permissions)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.core)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)
            implementation(libs.logging.interceptor)
            implementation(libs.material)
            implementation(libs.moshi.kotlin)
            implementation(libs.okhttp)
            implementation(libs.play.services.location)
            implementation(libs.play.services.ads)
            implementation(libs.retrofit)
            implementation(libs.ucrop)
            implementation(libs.billing.ktx)
            implementation(libs.androidx.work.runtime.ktx)
            implementation(libs.mlkit.barcode.scanning)
            implementation("com.google.guava:guava:33.0.0-android")
        }
        androidUnitTest.dependencies {
            implementation(libs.junit)
        }
        androidInstrumentedTest.dependencies {
            implementation(libs.androidx.junit)
            implementation(libs.androidx.espresso.core)
            implementation("androidx.compose.ui:ui-test-junit4:1.7.3")
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    signingConfigs {
        create("release") {
            storeFile =
                file("C:\\Users\\umaur\\OneDrive\\Apps 1\\AndroidStudioProjects\\CircleKeepApp\\CircleKeepKeyFile.jks")
            storePassword = "Sahatwar$23$46@"
            keyPassword = "Sahatwar$23$46@"
            keyAlias = "key1"
        }
    }
    namespace = "com.circlekeep"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.circlekeep"
        minSdk = 24
        targetSdk = 36
        versionCode = 18
        versionName = "2.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713" // Test App ID
            buildConfigField("String", "BANNER_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/6300978111\"") // Test Banner
            buildConfigField("String", "INTERSTITIAL_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/1033173712\"") // Test Interstitial
        }
        release {
            manifestPlaceholders += mapOf()
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
            manifestPlaceholders["admobAppId"] = "ca-app-pub-9985549354765338~3888239696" // Actual for the production
            buildConfigField("String", "BANNER_AD_UNIT_ID", "\"ca-app-pub-9985549354765338/9989659328\"") // Actual for the production
            buildConfigField("String", "INTERSTITIAL_AD_UNIT_ID", "\"ca-app-pub-9985549354765338/3719764440\"")
            signingConfig = signingConfigs.getByName("release")// Actual for the production
        }
        create("beta") {
            initWith(getByName("release"))
            matchingFallbacks.add("release")
            ndk {
                debugSymbolLevel = "FULL"
            }
            // Enabled ads for Beta variant by keeping placeholders (which we set to Test IDs for safety)
            manifestPlaceholders["admobAppId"] = "ca-app-pub-3940256099942544~3347511713" // Test App ID
            buildConfigField("String", "BANNER_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/6300978111\"") // Test Banner
            buildConfigField("String", "INTERSTITIAL_AD_UNIT_ID", "\"ca-app-pub-3940256099942544/1033173712\"") // Test Interstitial
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

dependencies {
    ksp(libs.androidx.room.compiler)
    ksp(libs.moshi.kotlin.codegen)
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.7.3")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
