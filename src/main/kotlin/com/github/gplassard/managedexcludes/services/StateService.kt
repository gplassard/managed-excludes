package com.github.gplassard.managedexcludes.services

import com.github.gplassard.managedexcludes.state.PluginState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@Service(Service.Level.PROJECT)
@State(name = "StateService", storages = [Storage("managed-excludes.xml")])
class StateService : SimplePersistentStateComponent<PluginState>(PluginState())  {

    override fun loadState(state: PluginState) {
        super.loadState(state)
        println("loading state" + state.excludedPaths)
    }

    override fun initializeComponent() {
        super.initializeComponent()
        println("initializeComponent")

    }

    override fun noStateLoaded() {
        super.noStateLoaded()
        println("noStateLoaded")
    }
}
