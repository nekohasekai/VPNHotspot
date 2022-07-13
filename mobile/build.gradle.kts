plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("kotlin-parcelize")
}

android {
    val javaVersion = JavaVersion.VERSION_11
    buildToolsVersion = "32.0.0"
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    compileSdk = 32
    kotlinOptions.jvmTarget = javaVersion.toString()
    defaultConfig {
        applicationId = "be.mygod.vpnhotspot.foss"
        minSdk = 21
        @android.annotation.SuppressLint("ExpiredTargetSdkVersion")
        targetSdk = 29
        resourceConfigurations.addAll(arrayOf("it", "ru", "zh-rCN", "zh-rTW"))
        versionCode = 292
        versionName = "2.14.4"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        javaCompileOptions.annotationProcessorOptions.arguments.apply {
            put("room.expandProjection", "true")
            put("room.incremental", "true")
            put("room.schemaLocation", "$projectDir/schemas")
        }
        buildConfigField("boolean", "DONATIONS", "true")
        buildConfigField("int", "TARGET_SDK", "29")
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
    buildTypes {
        getByName("debug") {
            isPseudoLocalesEnabled = true
        }
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
    packagingOptions.resources.excludes.add("**/*.kotlin_*")
    flavorDimensions.add("freedom")
    productFlavors {
        create("freedom") {
            dimension = "freedom"
        }
        create("google") {
            dimension = "freedom"
            targetSdk = 32
            versionNameSuffix = "-g"
            buildConfigField("boolean", "DONATIONS", "false")
            buildConfigField("int", "TARGET_SDK", "32")
        }
    }
    sourceSets.getByName("androidTest").assets.srcDir("$projectDir/schemas")
}

dependencies {
    val lifecycleVersion = "2.5.0-rc01"
    val roomVersion = "2.5.0-alpha02"

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.2.0")
    kapt("androidx.room:room-compiler:$roomVersion")
    implementation(kotlin("stdlib-jdk8"))
    implementation("androidx.browser:browser:1.4.0")
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.fragment:fragment-ktx:1.5.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation("androidx.preference:preference:1.2.0")
    implementation("androidx.room:room-ktx:$roomVersion")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.google.android.material:material:1.7.0-alpha02")
    implementation("com.google.zxing:core:3.5.0")
    implementation("com.jakewharton.timber:timber:5.0.1")
    implementation("com.linkedin.dexmaker:dexmaker:2.28.1")
    implementation("com.takisoft.preferencex:preferencex-simplemenu:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.3")
    add("googleImplementation", "com.github.tiann:FreeReflection:3.1.0")
    add("googleImplementation", "com.google.android.play:core:1.10.3")
    add("googleImplementation", "com.google.android.play:core-ktx:1.8.1")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.room:room-testing:$roomVersion")
    androidTestImplementation("androidx.test:runner:1.4.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.test.ext:junit-ktx:1.1.3")
}
