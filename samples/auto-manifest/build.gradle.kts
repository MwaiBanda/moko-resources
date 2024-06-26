/*
 * Copyright 2023 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

buildscript {
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
        mavenLocal()
    }
    dependencies {
        classpath(moko.resourcesGradlePlugin)
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
        classpath("com.android.tools.build:gradle:8.0.1")
        classpath("com.gradleup:auto-manifest-plugin:2.0")
    }
}
