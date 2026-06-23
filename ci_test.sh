#!/bin/bash
set -e

ANDROID_HOME="${ANDROID_HOME:-/usr/local/lib/android/sdk}"
WORKSPACE="${GITHUB_WORKSPACE:-$(pwd)}"

echo 'KERNEL=="kvm", GROUP="kvm", MODE="0666", OPTIONS+="static_node=kvm"' | sudo tee /etc/udev/rules.d/99-kvm4all.rules
sudo udevadm control --reload-rules
sudo udevadm trigger --name-match=kvm
ls -l /dev/kvm

rm -rf "${WORKSPACE}/model_files"
mkdir -p "${WORKSPACE}/model_files"
pip install -q huggingface_hub
python3 -c "
import os, sys, time, random, shutil
from huggingface_hub import hf_hub_download
mirrors = ['https://huggingface.co', 'https://hf-mirror.com']
repo_id = 'onnx-community/DeepSeek-R1-Distill-Qwen-1.5B-ONNX'
files = {'onnx/model_quantized.onnx': 800000000, 'tokenizer.json': 1000000, 'tokenizer_config.json': 100}
for fname, min_size in files.items():
    for mirror in mirrors:
        os.environ['HF_ENDPOINT'] = mirror
        for _ in range(20):
            try:
                time.sleep(random.uniform(10, 30))
                path = hf_hub_download(repo_id, fname, local_dir='${WORKSPACE}/model_files')
                if os.path.getsize(path) >= min_size:
                    break
            except: pass
        else: continue
        break
    else: sys.exit(1)
os.rename('${WORKSPACE}/model_files/onnx/model_quantized.onnx', '${WORKSPACE}/model_files/model.onnx')
shutil.rmtree('${WORKSPACE}/model_files/onnx')
"

cd "${WORKSPACE}"
sed -i 's/abiFilters += "arm64-v8a"/abiFilters += "x86_64"/g' app/build.gradle.kts
gradle assembleDebug --no-build-cache -Dorg.gradle.jvmargs="-Xmx6g"

yes | ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager "platform-tools" "emulator" "system-images;android-29;google_apis;x86_64" 2>&1 | tail -3
export PATH="${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/emulator:${ANDROID_HOME}/cmdline-tools/latest/bin:$PATH"
export ANDROID_AVD_HOME="$HOME/.config/.android/avd"
mkdir -p "$ANDROID_AVD_HOME"
rm -rf "${ANDROID_AVD_HOME}/test_avd.avd" "${ANDROID_AVD_HOME}/test_avd.ini"
echo "no" | avdmanager create avd -n test_avd -k "system-images;android-29;google_apis;x86_64" -d "pixel" -f 2>&1
${ANDROID_HOME}/emulator/emulator -avd test_avd -no-window -no-audio -no-boot-anim -gpu swiftshader_indirect -netdelay none -netspeed full -no-snapshot -memory 4096 &
EMULATOR_PID=$!

for i in $(seq 1 60); do
  if ! kill -0 $EMULATOR_PID 2>/dev/null; then echo "Emulator died"; exit 1; fi
  if adb devices | grep -w "emulator-5554" | grep -q "device"; then break; fi
  sleep 5
done
for i in $(seq 1 30); do
  if [ "$(adb shell getprop sys.boot_completed 2>/dev/null)" = "1" ]; then break; fi
  sleep 5
done
sleep 5

APK="${WORKSPACE}/app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK" ]; then echo "APK not found!"; exit 1; fi

adb shell input keyevent 82
adb shell wm dismiss-keyguard
sleep 2
adb install -g "$APK"
adb shell pm grant com.eternal.ai android.permission.RECORD_AUDIO
adb shell pm grant com.eternal.ai android.permission.CAMERA

adb shell run-as com.eternal.ai mkdir -p files/model
for f in ${WORKSPACE}/model_files/*; do
  local_file=$(basename "$f")
  adb push "$f" "/data/local/tmp/$local_file"
  adb shell run-as com.eternal.ai cp "/data/local/tmp/$local_file" files/model/
  adb shell rm "/data/local/tmp/$local_file"
done
adb shell run-as com.eternal.ai ls -l files/model/

adb shell am force-stop com.eternal.ai
sleep 2
adb shell am start -n com.eternal.ai/.SplashActivity

echo "Waiting for engine activation..."
activated=0
for i in $(seq 1 60); do
  if adb logcat -d | grep -q "神格已激活"; then
    echo "Engine activated!"; activated=1; break
  fi
  sleep 2
done

if [ $activated -eq 0 ]; then
  echo "=== Engine not activated ==="
  adb shell run-as com.eternal.ai cat files/eternal_log.txt 2>/dev/null
  adb logcat -d | grep -i "InferenceEngine\|神格\|CoreEngine" | tail -30
  exit 1
fi

questions=("hello" "who are you" "1+1" "what is love" "how big is the universe" "meaning of life" "weather today" "write a poem" "recommend a book" "how to be happy")
success=0
for q in "${questions[@]}"; do
  echo "Sending: $q"
  adb shell input tap 200 1800
  sleep 0.5
  for ((i=0; i<${#q}; i++)); do
    char="${q:$i:1}"
    case "$char" in
      [a-z]) keycode=$(( 29 + $(printf '%d' "'$char") - 97 )) ;;
      [A-Z]) keycode=$(( 29 + $(printf '%d' "'$char") - 65 )) ;;
      [0-9]) keycode=$(( 7 + $(printf '%d' "'$char") - 48 )) ;;
      ' ') keycode=62 ;;
      '-') keycode=69 ;;
      '?') keycode=76 ;;
      *) keycode=0 ;;
    esac
    [ $keycode -ne 0 ] && adb shell input keyevent $keycode
  done
  sleep 0.5
  adb shell input tap 900 1800
  sleep 20
  log=$(adb logcat -d | tail -50 | grep -i "永恒之神\|推理成功\|assistant" || true)
  if [ -n "$log" ]; then
    echo "Reply found for '$q'"; success=$((success + 1))
  else
    echo "No reply for '$q'"
  fi
done

echo "Success count: $success/10"
[ $success -lt 10 ] && exit 1
kill $EMULATOR_PID || true
