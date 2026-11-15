"""Replay the Sprint 1 demo against two freshly started localhost servers."""

import json
from pathlib import Path
from urllib.error import HTTPError
from urllib.request import Request, urlopen


FIXTURES = (
    Path(__file__).resolve().parents[2] / "acceptance-tests/src/test/resources/fixtures"
)


def require(actual, expected, label):
    if actual != expected:
        raise RuntimeError(f"{label}: expected {expected!r}, got {actual!r}")


def snapshot(power, temperature):
    return {
        "devices": [
            {"id": "living-light", "kind": "light", "power": power},
            {"id": "living-thermostat", "kind": "thermostat", "targetTemperature": temperature},
        ]
    }


def replay(port):
    transcript = []

    def request(label, path, status, fixture=None):
        data = (FIXTURES / fixture).read_bytes() if fixture else None
        req = Request(
            f"http://127.0.0.1:{port}/api/v1{path}",
            data=data,
            headers={"Content-Type": "application/json"} if data else {},
        )
        try:
            with urlopen(req, timeout=5) as response:
                actual, body = response.status, json.load(response)
        except HTTPError as error:
            with error:
                actual, body = error.code, json.load(error)
        require(actual, status, label)
        transcript.append((label, actual, body))
        print(f"{label}: {actual} {json.dumps(body, separators=(',', ':'))}", flush=True)
        return body

    require(request("health", "/health", 200), {"status": "UP"}, "health")
    require(request("inventar", "/home", 200), snapshot(False, 20.0), "inventar")
    for label, path, fixture, count, state in (
        ("light-on", "/commands", "commands/light-on.json", 1, snapshot(True, 20.0)),
        (
            "temperature-22", "/commands", "commands/temperature-22.json", 1,
            snapshot(True, 22.0),
        ),
        ("leave-home", "/scenes/execute", "scenes/leave-home.json", 2, snapshot(False, 18.0)),
    ):
        wanted = {"ok": True, "executedCommands": count, "snapshot": state}
        require(request(label, path, 200, fixture), wanted, label)
    before = request("stare înainte de erori", "/home", 200)
    require(before, snapshot(False, 18.0), "stare după leave-home")
    for label, path, fixture, status, code, index in (
        ("unknown-device", "/commands", "errors/unknown-device.json", 404, "UNKNOWN_DEVICE", None),
        ("invalid-middle", "/scenes/execute", "scenes/invalid-middle.json", 422, "INVALID_TEMPERATURE", 1),
        (
            "unknown-device-after-valid", "/scenes/execute", "scenes/unknown-device-after-valid.json",
            404, "UNKNOWN_DEVICE", 2,
        ),
    ):
        body = request(label, path, status, fixture)
        require(set(body), {"ok", "error"}, f"{label}: câmpuri răspuns")
        require(body["ok"], False, f"{label}: ok")
        require(body["error"]["code"], code, f"{label}: cod")
        if index is None:
            require("commandIndex" in body["error"], False, f"{label}: index absent")
        else:
            require(body["error"]["commandIndex"], index, f"{label}: index")
        require(request(f"stare după {label}", "/home", 200), before, f"{label}: stare")
    return transcript


if __name__ == "__main__":
    print("=== patterns :8080 ===", flush=True)
    patterns = replay(8080)
    print("=== baseline :8081 ===", flush=True)
    baseline = replay(8081)
    require(baseline, patterns, "echivalență HTTP")
    print("OK: răspunsuri echivalente; starea a rămas neschimbată după fiecare eroare.")
