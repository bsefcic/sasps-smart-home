package ro.upb.sasps.smarthome.contracts;

/** Public request DTO, not the executable GoF Command. */
public sealed interface HomeCommand permits SetLightPower {}
