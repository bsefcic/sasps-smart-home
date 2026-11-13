# Exemple de referință — contract v1 decis

Aceste fișiere sunt date reutilizabile; regulile complete sunt în docs/contracts-v1.md. Căile sunt relative la acest director.

| Input | Rezultat-țintă S1 |
| --- | --- |
| commands/light-on.json, light-off.json | 200, executedCommands=1, starea cerută |
| commands/temperature-22.json | 200, targetTemperature=22 |
| scenes/leave-home.json | 200, executedCommands=2, bec oprit și temperatură 18 |
| scenes/invalid-middle.json | 422 INVALID_TEMPERATURE, commandIndex=1, fără efecte |
| errors/unknown-device.json | 404 UNKNOWN_DEVICE, fără efecte |
| errors/incompatible-device.json | 422 INCOMPATIBLE_DEVICE |
| errors/invalid-temperature.json, invalid-precision.json | 422 INVALID_TEMPERATURE |
| errors/missing-power.json | 400 INVALID_REQUEST |
| errors/empty-scene.json | 400 INVALID_SCENE |
| scenes/evening.json | 200 executedCommands=3, bec pornit și temperatură 23 |
| scenes/unknown-device-after-valid.json | 404 UNKNOWN_DEVICE, commandIndex=2, fără efecte |
| scenes/incompatible-device-after-valid.json | 422 INCOMPATIBLE_DEVICE, commandIndex=1, fără efecte |
| scenes/missing-power-after-valid.json | 400 INVALID_REQUEST, commandIndex=1, fără efecte |

`responses/s1-initial-home.json` descrie inventarul inițial S1. Suita HTTP folosește aceste fișiere pentru ambele engines, iar exemplele REST trimit aceleași cereri JSON.
