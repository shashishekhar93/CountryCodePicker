plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
}

android {
    namespace = "com.smcoding.countrycodepicker"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures{
        buildConfig = true
        viewBinding = true
    }
    publishing{
        singleVariant("release"){
            withSourcesJar()
            withJavadocJar()
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release"){
                groupId = "com.smcoding"
                artifactId = "countrycodepicker"
                version = "1.0.0"

                from(components["release"])
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    implementation("io.michaelrocks:libphonenumber-android:9.0.32")
}