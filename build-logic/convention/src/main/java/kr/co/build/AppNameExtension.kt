package kr.co.build

import org.gradle.api.Project

fun Project.setNamespace(name: String) {
    androidExtension.apply {
        namespace = "${project.groupId}.$name"
    }
}