<!-- BarcodeScanner.vue -->
<script setup lang="ts">
import {ref} from 'vue'
import {
  scan,
  cancel,
  checkPermissions,
  requestPermissions,
  openAppSettings,
  Format,
  Scanned,
  PermissionState
} from '@tauri-apps/plugin-barcode-scanner'

// 响应式数据
const result = ref<string | null>(null)
const isScanning = ref(false)
const permissionState = ref<PermissionState>('prompt')

// 开始扫码
const startScan = async () => {
  // 1. 先检查权限，查看用户之前是否用过该功能并已经授予了权限。
  let state = await checkPermissions()

  // 2. 如果被拒绝，提示用户去设置
  if (state === 'denied') {
    const res = confirm('摄像头权限被拒绝，是否前往设置开启？')
    if (res) await openAppSettings()
    return
  }

  // Android 系统不允许二次弹窗

  // 3. 如果是首次，请求权限
  if (state === 'prompt') {
    state = await requestPermissions()
    console.log('权限状态:', state)
    if (state !== 'granted') {
      alert('权限未授权，无法使用摄像头')
      return
    }
  }

  // 4. 权限已获得，开始扫码
  result.value = null
  isScanning.value = true

  try {
    const scanned: Scanned = await scan({
      // 在当前页面显示摄像头（透明层）如果设置为 false 则会打开一个原生级别的全屏相机界面进行扫码识别
      windowed: false,
      cameraDirection: 'back',  // 可选 'front' 或 'back'
      // 扫码格式
      formats: [
        Format.QRCode,
        Format.EAN13,
        Format.Code128,
        Format.UPC_A
      ]
    }).then(result => {
      // 操作扫码结果，一般来说，是一个链接

    })
    result.value = `格式: ${scanned.format}\n内容: ${scanned.content}`
  } catch (err) {
    result.value = err instanceof Error ? err.message : '扫码失败或已取消'
  } finally {
    isScanning.value = false
  }
}

// 取消扫码（可选：提前中断）
const cancelScan = async () => {
  await cancel()
  isScanning.value = false
  result.value = '扫码已取消'
}

// 打开系统设置（权限被拒绝时使用）
const goToSettings = async () => {
  await openAppSettings()
}
</script>

<template>
  <div class="barcode-scanner">
    <h2>条形码/二维码扫描</h2>

    <!-- 权限状态 -->
    <div v-if="permissionState === 'denied'" class="alert">
      <p>❌ 摄像头权限被拒绝。请在系统设置中手动开启。</p>
      <button @click="goToSettings">🔧 打开应用设置</button>
    </div>

    <div v-else-if="permissionState === 'granted' || permissionState === 'prompt'">
      <button @click="startScan" :disabled="isScanning">
        {{ isScanning ? '📸 扫码中...' : '📷 开始扫码' }}
      </button>

      <button v-if="isScanning" @click="cancelScan" class="cancel-btn">
        取消
      </button>
    </div>

    <!-- 扫码结果 -->
    <div v-if="result" class="result">
      <h3>扫码结果：</h3>
      <pre>{{ result }}</pre>
    </div>

    <!-- 提示：windowed: true 时，摄像头会全屏覆盖 -->
    <div v-if="isScanning" class="overlay-hint">
      <p>💡 将条形码对准摄像头中心区域</p>
      <p><small>点击“取消”可停止扫码</small></p>
    </div>
  </div>
</template>

<style scoped>
.barcode-scanner {
  padding: 20px;
  font-family: Arial, sans-serif;
}

button {
  margin: 10px 5px;
  padding: 10px 15px;
  font-size: 16px;
  cursor: pointer;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.cancel-btn {
  background-color: #e74c3c;
  color: white;
  border: none;
}

.alert {
  background-color: #fdf6ed;
  color: #e65d21;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.result {
  margin-top: 20px;
  padding: 15px;
  background-color: #f0f0f0;
  border-radius: 8px;
  white-space: pre-wrap;
}

.overlay-hint {
  margin-top: 20px;
  font-size: 14px;
  color: #666;
}
</style>