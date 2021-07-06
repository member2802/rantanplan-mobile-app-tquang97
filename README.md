# Rantanplan Mobile Application

![Rantanplan](img/logo_rantanplan_01_medium.jpg)

## Preamble

Most mobile device (mobile devices, tablets) in the market have a Global Positioning System (GPS) receiver to determine the ground position of the mobile device.

The accuracy of the position the mobile device's GPS reports is influenced by a number of factors, such as the number of satellites the mobile device can get signals from, satellite signal blockage due to buildings, signals reflected off buildings or walls, atmospheric effects, etc If the GPS receiver is only able to get signals from 3 satellites, it can still get the position of the mobile device, but it will be less accurate. GPS receivers are typically accurate to within a 5 m radius under open sky. However, their accuracy worsens near buildings, bridges, and trees, or nil when the user is inside a building.

![Saigon Shopping Mall](img/shopping_mall.jpg)

How to determine the place where a user is having a lunch in a shopping mall located twenty meters under the ground? Most of the restaurants, cafe places, and various shops, are equipped with [Wi-Fi access points](<(https://medium.com/@Meela349588204/wireless-access-point-vs-wireless-router-8fc36e14b)>) ([Wireless Access Points (WAP)](https://en.wikipedia.org/wiki/Wireless_access_point) or [Wi-Fi routers](https://www.youtube.com/watch?v=OxiY4yf6GGg)).

A mobile device is able to detect Wi-Fi access points nearby. For example, when a user accesses the Wi-Fi settings of his mobile device to connect to the Internet, the mobile device displays a list of available networks:

![Android Wi-Fi Networks](img/android_available_wifi_networks.jpg)

The list of Wi-Fi access points that a mobile device detects nearby depends on the distance of the Wi-Fi access points from the mobile device. The closest, the best the signal level. The list of Wi-Fi access points and their respective signal level that are detected from a given location don't drastically change over the time: Wi-Fi access points are rarely replaced, and they rarely moved around once installed. Hence the list of Wi-Fi access points and their signal level, detected by a mobile device, is a pretty accurate clue of the place where the mobile device is located.

## Basic Principles

`Rantanplan` is an **Android** mobile application used to sniff Wi-Fi access points nearby, to collect their information, to ping the user when the location is not known yet, and to send the collected data to a server platform, which aggregates all the data that mobile devices transmit over the time, and it maintains a huge database of places and the Wi-Fi access point signals that one is expected to detect when located at these places.

![Rantanplan Solution Architecture](img/rantanplan_solution_architecture.png)

---

## Waypoint 1: Detect Mobile Device Stationary Position

It would be totally pointless that the user moves around with his mobile device to scan Wireless Access Points: the signal levels would drastically change from a scan to another, and moreover, consecutive scan results would not help in identifying one specific place. Rantanplan mobile application MUST wait that the mobile device is in a stationary position for a while (**Minimum Idle Motion Position Duration**) before starting to scan Wi-Fi access point signals around.

Find a method to programmatically check whether the mobile device is in motion or in stationary position and implement a very simple Android [activity](https://developer.android.com/guide/components/activities/intro-activities) to test your code.

## Waypoint 2: Integrate Background Service

An Android [activity](https://developer.android.com/reference/android/app/Activity) can be paused and destroyed for several reasons, like [screen rotation](https://www.youtube.com/watch?v=TcTgbVudLyQ), or when the user switches from your application to another application. You need to move your stationary position detection code from your activity to a [Android service](https://developer.android.com/guide/components/services).

## Waypoint 3: Detect Wi-Fi Access Points

A Wi-Fi access point device is uniquely identified with **Basic Service Set Identifier** (BSSID). A Wi-Fi access point is also defined with a human-readable name also known as **Service Set Identifier** (SSID); this name may not be unique.

A Wi-Fi access point uses a radio frequency channel, expressed in MHz, to send and receive data with mobile devices. The frequency channel of a Wi-Fi access point rarely changes, although it can change.

The signal level of a Wi-Fi access point received by the user's mobile device depends on the distance from the user's mobile device to a Wi-Fi access point, and obstacles (furniture, walls, home appliances, etc.) between the mobile device and the Wi-Fi access point. The signal level is expressed in dBm. The quality of a signal level can be interpreted as follows:

- Excellent: -57 to -45 dBms (75 to 100%)
- Good: -75 to -58 dBms (40 to 74%)
- Fair: -85 to -76 dBms (20 to 39%)
- Poor: -95 to -86 dBms (0 to 19%)

_Note: Signal level is commonly correlated with the received signal strength indication (RSSI), which is a measurement of the power present in a received radio signal._

Use the Wi-Fi scanning capabilities provided by the [WifiManager API](https://developer.android.com/guide/topics/connectivity/wifi-scan) to get a list of Wi-Fi access points that are visible from the mobile device.

## Waypoint 4: Complete Wi-Fi Access Points Detection

Wi-Fi access points are generally not all returned after the first scan. Consecutive scans could return other Wi-Fi access points visible from the mobile device. The more Wi-Fi access points scanned, the more accurate the signature of the place where the user's mobile device is located at.

Scanning nearby Wi-Fi access points takes time and this operation drains the battery of the user's mobile device. We would like to limit the maximum number of scans (**Maximum Scan Rounds**) that our application can consecutively performs.

We also would like to stop scanning Wi-Fi access points, if no more new Wi-Fi access points have been scanned. **Empty-handed Scan Rounds** defines the number of rounds that don't return any new Wi-Fi access points. When this number is reached, the mobile application automatically stop scanning.

_Note: The mobile application automatically stop scanning when of the two numbers **Maximum Scan Rounds** and **Empty-handed Scan Rounds** is reached._

Our application needs to aggregate the information it collects:

- Median value of the signal level of a Wi-Fi access point that has been scanned several times.
- Number of times a Wi-Fi access point has been returned in the result of a scan operation.

## Waypoint 5: Send Wi-Fi Access Points Scan Result

Once the mobile application has completed a Wi-Fi scanning operation, the mobile application needs to send the result to the Rantanplan RESTful API server.

The request must pass a body message as a JSON expression:

```json
{
  "start_time": timestamp,
  "end_time": timestanp,
  "round_count": integer,
  "signals": [
    {
      "bssid": string,
      "ssid": string,
      "frequency": integer,
      "signal_level": integer,
      "sample_count": integer
    },
    ...
  ]
}
```

where:

- `place_name` (optional): The name of the place. This attribute must be passed if the place is not identified.
- `place_address` (optional): The formatted address of the place. This attribute must be passed if the place is not identified.
- `start_time` (required): Time when this scan operation has started.
- `end_time` (required): Time when this scan operation has completed.
- `round_count` (required): Number of rounds performed during this scan operation.
- `signals` (required): An array of Wi-Fi access points that have been scanned.
  - `bssid` (required): Basic Service Set Identifier (BSSID) that uniquely identifies the Wi-Fi access point.
  - `ssid` (required): Service Set Identifier (SSID) of the Wi-Fi access point, a human-readable name.
  - `frequency` (required): Frequency in MHz of the channel over which the mobile device was communicating with the Wi-Fi access point.
  - `signal_level` (required): The median value of the detected signal levels in dBm of the Wi-Fi access point.
  - `sample_count` (required): Number of times the Wi-Fi access point has been detected during the scan operation.

For example:

```json
{
  "start_time": "2017-09-26T16:44:53.231+02",
  "end_time": "2017-09-26T16:45:05.191+02",
  "round_count": 5,
  "signals": [
    {
      "bssid": "c4:6e:1f:95:46:84",
      "ssid": "LongHang",
      "frequency": 2462,
      "signal_level": -90,
      "sample_count": 5
    },
    {
      "bssid": "88:d2:74:fa:a2:1b",
      "ssid": "Alex",
      "frequency": 2462,
      "signal_level": -81,
      "sample_count": 5
    },
    {
      "bssid": "10:fe:ed:0f:f8:49",
      "ssid": "N3T",
      "frequency": 2412,
      "signal_level": -52,
      "sample_count": 5
    },
    {
      "bssid": "c4:6e:1f:0d:41:50",
      "ssid": "AN PHU CORP",
      "frequency": 2462,
      "signal_level": -95,
      "sample_count": 2
    },
    {
      "bssid": "88:d2:74:e5:90:f9",
      "ssid": "Hawon",
      "frequency": 2447,
      "signal_level": -97,
      "sample_count": 3
    }
  ]
}
```

The mobile application sends a HTTP `POST` request to one of the following endpoints:

- `/place/signal`: The place is not identified yet. The Rantanplan server platform will create a new place and return the identification to the mobile application. The mobile application MUST have passed the attributes `place_name` and `place_address` in the JSON expression.
- `/place/(place_id:string)/signal`: The place is identified with `place_id`. The Rantanplan server platform will update the signature of the place with the scan result sent by the mobile application.

For examples :

```bash
$
curl -X POST /place/signal \
     -H "Content-Type: application/json"
     -H "X-API-Key: f52ce42ac87f11e98e0d0008a20c190f" \
     -H "X-API-Sig: AIzaSyANQixZzt6a_p30YpJ9WY4rkg_svc3IbMU" \
     -d "{{json}}"
```

or:

```bash
$ curl -X POST /place/10a4ff56253d46499a7eef353d3e5a83/signal \
     -H "Content-Type: application/json"
     -H "X-API-Key: f52ce42ac87f11e98e0d0008a20c190f" \
     -H "X-API-Sig: AIzaSyANQixZzt6a_p30YpJ9WY4rkg_svc3IbMU" \
     -d "{{json}}"
```

The RESTful API returns a JSON expression:

```json
{
  "place_id": string
}
```

where:

- `place_id`: Identification of the place that the mobile has scanned Wi-Fi access points nearby.

For example:

```json
{
  "place_id": "10a4ff56253d46499a7eef353d3e5a83"
}
```

_Note: Indeed, the mobile application MUST persists the result of scans (in case of the mobile shuts down), it asynchronously send the scan result to the Rantanplan server platform, it MUST handle network outages._

## Waypoint 6: Home Screen User Interface

The [home screen of the mobile application](https://www.figma.com/file/AISeKYLkHN1WYzCQziCZ6t/Rantanplan?node-id=9%3A4) corresponds to the settings of this application. It is composed of the logo of the application and a few sliders that allows the user to define some thresholds:

- The minimum idle motion position duration, expressed in minutes, before Rantanplan starts scanning Wi-Fi access point signals around. The minimum value is `1` minutes. The maximum value is `10` minutes. The default value is `5` minutes.
- The maximum number of consecutive scans that Rantanplan performs before it sends a result. The list of available values is: `1`, `2`, `3`, `5`, `8`, and `infinite`.
- The maximum number of consecutive empty-handed scans that Rantanplan performs before it sends a result. The minimum value is `1`. The maximum value is `5`. The default value is `2`.

![Screen Settings](./img/screen_rantanplan_settings.png)

## Waypoint 7: Alert User on Scan Result

When Rantanplan completes scanning Wi-Fi access points near its location, it needs to [alert the user](<https://developer.android.com/reference/android/app/Notification.Builder#setFullScreenIntent(android.app.PendingIntent,%20boolean)>) to allow him to enter the name and the address of the place to register to the server platform among the signals that have been collected.

![Screen New Place](./img/screen_rantanplan_alert_new_place.png)

## Waypoint 8: Find Registered Place

When Rantanplan completes the scan of Wi-Fi access points near its location, it tries to find whether these signals match a known place.

### Local Search

Rantanplan tries first to find this place in its local database of places. If Rantanplan finds a place that may match the collected signals, it alerts the user by displaying the name and the address of the place, requesting the user to confirm this result.

![Screen Discovered Place](./img/screen_rantanplan_alert_found_place.png)

If the user indicates this is a wrong place, Rantanplan redirects the user to the screen that allows him to enter the name and the address of the place.

### Remote Search

If Rantanplan doesn't find any places that match the collected signals, Rantanplan sends a request to the RESTful API to search a place with the collected signals.

The mobile application sends a HTTP `POST` request to the endpoint `/place/search`, passing the following JSON structure in the body message of this request:

```json
[
  {
    "bssid": string,
    "signal_level": integer
  },
  ...
]
```

For example:

```json
[
  {
    "bssid": "c4:6e:1f:95:46:84",
    "signal_level": -90
  },
  {
    "bssid": "88:d2:74:fa:a2:1b",
    "signal_level": -81
  },
  {
    "bssid": "10:fe:ed:0f:f8:49",
    "signal_level": -52
  },
  {
    "bssid": "c4:6e:1f:0d:41:50",
    "signal_level": -95
  },
  {
    "bssid": "88:d2:74:e5:90:f9",
    "signal_level": -97
  }
]
```

```bash
$ curl -X GET /place/search
     -H "Content-Type: application/json"
     -H "X-API-Key: f52ce42ac87f11e98e0d0008a20c190f" \
     -H "X-API-Sig: AIzaSyANQixZzt6a_p30YpJ9WY4rkg_svc3IbMU" \
     -d '[{"bssid":"c4:6e:1f:95:46:84","signal_level":-90},{"bssid":"88:d2:74:fa:a2:1b","signal_level":-81},{"bssid":"10:fe:ed:0f:f8:49","signal_level":-52},{"bssid":"c4:6e:1f:0d:41:50","signal_level":-95},{"bssid":"88:d2:74:e5:90:f9","signal_level":-97}]'
```

The RESTful API returns a list of places that best match the collected signals:

```json
[
  {
    "place_id": string,
    "place_name": string,
    "place_address": string,
    "score": decimal
  },
  ...
]
```

Where `score` is an estimation of the probability that the place is relevant to the search. A perfect precision score of `1.0` means that the place returned is totally relevant.
