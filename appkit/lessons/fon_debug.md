# 📱 fon debug — PC ⬄ fon wo a cable (o w 1)
_appkit 🪞 · ⬅ cali `fon_debug.md` @1122 · y: `apptool deploy -a` puts a DEV build on th fon i ~1 min · pair once, then it finds th fon by itself · Pixel menu names_

1. **dev options on** (once): Settings ➡ About phone ➡ tap **Build number** 7× ➡ PIN ➡ Settings ➡ System ➡ **Developer options**
2. **wireless pair** (once per PC): Developer options ➡ **Wireless debugging** ON (home wifi) ➡ tap th row ➡ **Pair device with pairing code** ➡ keep th dialog open
3. on th PC: `adb pair <IP:PORT fr th dialog>` ➡ type th 6-digit code ➡ "Successfully paired" · ⚠️ pair port ≠ connect port, both change evy toggle
4. **connect** (evy time aftr): Wireless debugging ON + same wifi ➡ `apptool deploy -a` finds th fon via mDNS · by hand: `adb mdns services` ➡ `adb connect <_adb-tls-connect IP:PORT>`
5. ck: `adb devices` ➡ `…  device` · then `apptool deploy -a` (build + install + open) · `-a -q` = don't open (🦠 is using th fon)
6. 🚧 "failed to connect" = not paired w **this** PC ➡ step 2 · 🚧 toggle turns itself off = wifi changed / fon rebooted ➡ ON again (Quick Settings tile "Wireless debugging": Developer options ➡ Quick settings developer tiles)
7. **usb**: Developer options ➡ **USB debugging** ON ➡ plug i ➡ popup "Allow USB debugging?" ➡ ✔ Always allow ➡ `adb devices`
8. 🚧 **y th allow popup may not show**: fon locked (unlock, replug) · charge-only cable (no data wires ➡ `lsusb` shows no fon) · USB debugging off · stale key ➡ Developer options ➡ **Revoke USB debugging authorisations** + `adb kill-server` + replug · `unauthorized` = popup waits on th fon · `no permissions` = udev (`adb kill-server`, replug, user i `plugdev`)
9. **no adb at all** (away fr home): `apptool deploy` (no -a) ➡ `<web>/dev/` ➡ th DEV app's own updater (notification ➡ Install) · o Obtainium ➡ pull 2 refresh · 1st time: `apptool deploy -r` ➡ scan th QR
10. logs fr th fon: `adb logcat --pid=$(adb shell pidof <pkg>.dev)` · screenshot: `adb exec-out screencap -p > /tmp/s.png` (⚠️ shows 🦠's private screen ➡ ask 1st)
