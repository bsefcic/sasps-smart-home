"""Replay the Sprint 1 demo against two freshly started localhost servers."""

import argparse
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


def replay(port, extended):
    transcript = []
    away = snapshot(False, 18.0)

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

    def check_success(label, path, fixture, count, state):
        wanted = {"ok": True, "executedCommands": count, "snapshot": state}
        require(request(label, path, 200, fixture), wanted, label)

    def check_rejection(label, path, fixture, status, code, index, state_label):
        body = request(label, path, status, fixture)
        require(set(body), {"ok", "error"}, f"{label}: câmpuri răspuns")
        require(body["ok"], False, f"{label}: ok")
        require(body["error"]["code"], code, f"{label}: cod")
        if index is None:
            require("commandIndex" in body["error"], False, f"{label}: index absent")
        else:
            require(body["error"]["commandIndex"], index, f"{label}: index")
        require(request(state_label, "/home", 200), away, f"{label}: stare")

    if extended:
        require(request("health", "/health", 200), {"status": "UP"}, "health")
    require(request("inventar", "/home", 200), snapshot(False, 20.0), "inventar")
    if extended:
        check_success("light-on", "/commands", "commands/light-on.json", 1, snapshot(True, 20.0))
        check_success(
            "temperature-22", "/commands", "commands/temperature-22.json", 1, snapshot(True, 22.0)
        )
    check_success("leave-home", "/scenes/execute", "scenes/leave-home.json", 2, away)
    check_rejection(
        "incompatible-device-after-valid",
        "/scenes/execute",
        "scenes/incompatible-device-after-valid.json",
        422,
        "INCOMPATIBLE_DEVICE",
        1,
        "inventar după scena incompatibilă",
    )
    if extended:
        for label, path, fixture, status, code, index in (
            ("unknown-device", "/commands", "errors/unknown-device.json", 404, "UNKNOWN_DEVICE", None),
            (
                "invalid-middle", "/scenes/execute", "scenes/invalid-middle.json",
                422, "INVALID_TEMPERATURE", 1,
            ),
            (
                "unknown-device-after-valid", "/scenes/execute", "scenes/unknown-device-after-valid.json",
                404, "UNKNOWN_DEVICE", 2,
            ),
        ):
            check_rejection(label, path, fixture, status, code, index, f"stare după {label}")
    return transcript


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--extended", action="store_true", help="include extra commands and errors")
    args = parser.parse_args()
    print("=== patterns :8080 ===", flush=True)
    patterns = replay(8080, args.extended)
    print("=== baseline :8081 ===", flush=True)
    baseline = replay(8081, args.extended)
    require(baseline, patterns, "echivalență HTTP")
    print("OK: răspunsuri echivalente; starea a rămas neschimbată după fiecare eroare.")
