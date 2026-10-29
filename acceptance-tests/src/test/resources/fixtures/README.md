# Exemple de referință — contract v1 decis

Aceste fișiere sunt date reutilizabile; regulile complete sunt în docs/contracts-v1.md. Căile sunt relative la acest director.

| Input | Rezultat-țintă S1 |
| --- | --- |
| commands/light-on.json, light-off.json | 200, executedCommands=1, starea cerută |
| commands/temperature-22.json | 200, targetTemperature=22; momentan 501 în starter |
| scenes/leave-home.json | 200, executedCommands=2, bec oprit și temperatură 18; momentan 501 în starter |
| scenes/invalid-middle.json | 422 INVALID_TEMPERATURE, commandIndex=1, fără efecte |
| errors/unknown-device.json | 404 UNKNOWN_DEVICE, fără efecte |
| errors/incompatible-device.json | 422 INCOMPATIBLE_DEVICE după #5; momentan 501 în starter |
| errors/invalid-temperature.json, invalid-precision.json | 422 INVALID_TEMPERATURE |
| errors/missing-power.json | 400 INVALID_REQUEST |
| errors/empty-scene.json | 400 INVALID_SCENE |

responses/s1-initial-home.json este inventarul final S1 după #3–#5, NU output pretins al starterului. Nu modificați golden-ul ca să ascundeți un comportament lipsă. Testele existente trebuie actualizate explicit când se înlocuiește 501 cu implementarea feature-ului.
