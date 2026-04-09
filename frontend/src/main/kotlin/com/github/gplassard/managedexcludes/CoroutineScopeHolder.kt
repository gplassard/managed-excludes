package com.github.gplassard.managedexcludes

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.Service.Level
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope

@Service(Level.PROJECT)
class CoroutineScopeHolder(private val projectScope: CoroutineScope) {
    companion object {
        fun getInstance(project: Project): CoroutineScopeHolder {
            return project.getService(CoroutineScopeHolder::class.java)
        }
    }

    fun getScope(): CoroutineScope = projectScope
}
