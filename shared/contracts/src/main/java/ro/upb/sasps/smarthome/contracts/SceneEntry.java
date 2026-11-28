package ro.upb.sasps.smarthome.contracts;

/** One element of a nested scene list: a command, a local reference or a rejected element. */
public sealed interface SceneEntry permits HomeCommand, SceneReference, InvalidSceneEntry {}
