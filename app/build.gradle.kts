import org.jetbrains.kotlin.konan.properties.Properties

//val appVersionCode: Int by rootProject.extra
//val appVersionName: String by rootProject.extra

plugins {
    id("com.android.application")
    kotlin("android") // 相当于 id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    kotlin("plugin.parcelize")

    id("androidx.navigation.safeargs.kotlin")

    // Secrets Gradle Plugin for Android: https://github.com/google/secrets-gradle-plugin
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin") version "2.0.1"

    id("com.google.dagger.hilt.android")

    // Easylauncher gradle plugin for Android: https://github.com/usefulness/easylauncher-gradle-plugin
    // id("com.starter.easylauncher") version "6.1.0"
}

kapt {
    correctErrorTypes = true
}

android {
    namespace = "com.bqliang.running.diary"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.bqliang.running.diary"
        minSdk = 29
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        javaCompileOptions {
            annotationProcessorOptions {
                argument("room.schemaLocation", "$projectDir/schemas")
                argument("room.incremental", true.toString())
            }
        }

        val abiFlavorDimensionName = "abi"
        flavorDimensions.add(abiFlavorDimensionName)
        productFlavors {
            val abis = setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64", "armeabi")
            // 为每个架构创建一个 flavor
            abis.forEach { abi ->
                create(abi) {
                    dimension = abiFlavorDimensionName
                    ndk.abiFilters.add(abi)
                }
            }
            // 创建一个 "universal" flavor，用于支持所有架构
            create("universal") {
                dimension = abiFlavorDimensionName
                ndk.abiFilters.addAll(abis)
            }
        }
    }

    signingConfigs {
        signingConfigs.create("release") {
            val properties = Properties()
            properties.load(rootProject.file("local.properties").inputStream())
            storeFile = rootProject.file("signing_key.jks")
            storePassword = properties["SIGNING_KEY_STORE_PASSWORD"] as String
            keyAlias = properties["SIGNING_KEY_ALIAS"] as String
            keyPassword = properties["SIGNING_KEY_PASSWORD"] as String
        }
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = ".debug"
        }

        getByName("release") {
            isDebuggable = true
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName(name)
        }
    }

    applicationVariants.configureEach {
        outputs.configureEach {
            (this as? com.android.build.gradle.internal.api.ApkVariantOutputImpl)?.outputFileName =
                "${rootProject.name}-${versionName}-${/* variant name */name}.apk"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    buildFeatures {
        dataBinding = true
    }

    sourceSets {
        getByName("main") {
            val srcDirs = listOf(
                "intro", // 应用介绍
                "main", // main activity
                "base",
                "authentication", // 登录注册
                "run", // 跑步记录
                "personal", // 个人中心
                "news", // 运动资讯
                "course", // 运动课程
                "tracking", // 跑步
                "detail", // 跑步详情
                "body", // 身高体重
                "statistics", // 统计
                "settings", // 设置
                "document", // 帮助与支持
            ).map { "src/main/kotlin/com/bqliang/running/diary/ui/$it/res" }
                .plus("src/main/res")
            res.setSrcDirs(srcDirs)
        }
    }
}

dependencies {
    // implement jar file in libs directory
    implementation(
        fileTree(
            mapOf(
                "dir" to "libs",
                "include" to listOf("*.jar")
            )
        )
    )
    // CodeLocator
    implementation("com.bytedance.tools.codelocator:codelocator-core:2.0.2")
    // AppIntro
    implementation("com.github.AppIntro:AppIntro:6.2.0")
    // markdown
    implementation("io.noties.markwon:core:4.6.2")
    // PictureSelector 基础
    implementation("io.github.lucksiege:pictureselector:v3.10.7")
    // 图片压缩
    implementation("io.github.lucksiege:compress:v3.10.7")
    // 图片裁剪
    implementation("io.github.lucksiege:ucrop:v3.10.7")
    // ProgressView
    implementation("com.github.skydoves:progressview:1.1.3")
    // Radial Progress Bar
    implementation("com.github.MindorksOpenSource:RadialProgressBar:v1.3") {
        exclude(group = "com.android.support")
    }
    // ExoPlayer
    implementation("com.google.android.exoplayer:exoplayer:2.18.5")
    // Glide
    val glide = "4.15.1"
    implementation("com.github.bumptech.glide:glide:$glide")
    kapt("com.github.bumptech.glide:compiler:$glide")
    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.8.1")
    // view pager2
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    // auto fill
    implementation("androidx.autofill:autofill:1.1.0")
    // androidX browser
    implementation("androidx.browser:browser:1.5.0")
    // hilt
    val hiltVersion = "2.45"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    kapt("com.google.dagger:hilt-android-compiler:$hiltVersion")
    // 高德地图 3D 地图 so及 jar
    implementation("com.amap.api:3dmap:9.6.0")
    // app center
    val appCenter = "5.0.1"
    implementation("com.microsoft.appcenter:appcenter-crashes:$appCenter")
    implementation("com.microsoft.appcenter:appcenter-analytics:$appCenter")
    implementation("com.microsoft.appcenter:appcenter-distribute:$appCenter")
    // lean could
    implementation("cn.leancloud:storage-android:8.2.18")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    // lottie
    implementation("com.airbnb.android:lottie:6.0.0")
    // retrofit & moshi converter
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    // mmkv
    implementation("com.tencent:mmkv:1.2.15")
    // timber
    implementation("com.jakewharton.timber:timber:5.0.1")
    // bottom sheet menu
    implementation("com.github.Kennyc1012:BottomSheetMenu:4.2")
    // about page
    implementation("com.drakeet.about:about:2.5.2")
    // bottom sheet menu
    implementation("com.github.Kennyc1012:BottomSheetMenu:4.2")
    // permissionX
    implementation("com.guolindev.permissionx:permissionx:1.7.1")
    // chart library
    implementation("com.github.PhilJay:MPAndroidChart:3.1.0")
    // kotlinX coroutines android
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    // splashScreen
    implementation("androidx.core:core-splashscreen:1.0.0")
    // activity
    implementation("androidx.activity:activity-ktx:1.7.0")
    // fragment
    implementation("androidx.fragment:fragment-ktx:1.5.6")
    // androidX preference
    implementation("androidx.preference:preference-ktx:1.2.0")
    // lifeCycle
    val lifeCycle = "2.6.1"
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifeCycle")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifeCycle")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifeCycle")
    implementation("androidx.lifecycle:lifecycle-service:$lifeCycle")
    // navigation
    val navigation = "2.5.3"
    implementation("androidx.navigation:navigation-fragment-ktx:$navigation")
    implementation("androidx.navigation:navigation-ui-ktx:$navigation")
    testImplementation("androidx.navigation:navigation-testing:$navigation")
    // room
    val jetpackRoom = "2.5.1"
    implementation("androidx.room:room-runtime:$jetpackRoom")
    implementation("androidx.room:room-ktx:$jetpackRoom")
    kapt("androidx.room:room-compiler:$jetpackRoom")
    // rikkaX
    implementation("dev.rikka.rikkax.layoutinflater:layoutinflater:1.3.0")
    implementation("dev.rikka.rikkax.lifecycle:lifecycle-shared-viewmodel:1.0.1") // ViewModel that shares across activities.
    implementation("dev.rikka.rikkax.insets:insets:1.3.0") // Handle window insets with XML only: https://github.com/RikkaApps/RikkaX/tree/master/insets
    implementation("dev.rikka.rikkax.lifecycle:lifecycle-viewmodel-lazy:2.0.0") // An easier-to-use version of ViewModelLazy than Androidx
    implementation("dev.rikka.rikkax.html:html:1.1.2")
    implementation("dev.rikka.rikkax.html:html-ktx:1.1.2")
    implementation("dev.rikka.rikkax.preference:simplemenu-preference:1.0.3") // A version of ListPreference that use Simple Menus from Material Design 1
    implementation("dev.rikka.rikkax.recyclerview:recyclerview-ktx:1.3.1")
    // androidx core ktx
    implementation("androidx.core:core-ktx:1.9.0")
    // androidX appCompat
    implementation("androidx.appcompat:appcompat:1.6.1")
    // google material
    implementation("com.google.android.material:material:1.8.0")
    // constraint layout
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    // recycler view
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    // test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}