<script setup lang="ts">
import {ref} from "vue";
import {invoke} from "@tauri-apps/api/core";
import {ask, confirm, message, open, save} from '@tauri-apps/plugin-dialog';

const greetMsg = ref("");
const name = ref("");

async function greet() {
  // Learn more about Tauri commands at https://tauri.app/develop/calling-rust/
  greetMsg.value = await invoke("greet", {name: name.value});
}

// 对话框的使用参考 https://tauri.app/zh-cn/plugin/dialog/
// https://v2.tauri.org.cn/reference/javascript/dialog/

// 创建 Yes/No 中文为 是/否 对话框
const answer_dialog = async () => {
  // 创建 Yes/No 对话框
  const answer = await ask('此作无法恢复。是否确定?', {
    title: 'Tauri',
    kind: 'warning',
  });

  // 将布尔值打印到控制台
  console.log(answer);

  if (answer) {
    // 根据用户的选择执行不同的操作
    alert('You clicked Yes!');
  }
}

// 创建 Ok/Cancel 中文为 取消/确定 对话框
async function confirmation_dialog() {
// 创建确认确定/取消对话框
  const confirmation = await confirm(
      '此作无法恢复。是否确定?',
      {title: 'Tauri', kind: 'warning'}
  );

  // 将布尔值打印到控制台
  console.log(confirmation);
}

// 创建 Message 对话框
async function message_dialog() {
  // 显示消息
  await message('此作无法恢复。是否确定?', {title: 'Tauri', kind: 'info'});
  await message('找不到文件', {title: 'Tauri', kind: 'error'});
  await message('此作无法恢复。是否确定?', {title: 'Tauri', kind: 'warning'});
}

// 打开一个文件选择对话框
async function open_file_dialog() {
// Open a dialog
  const file = await open({
    multiple: false,
    directory: false,
  });

  // 将文件路径和名称打印到控制台
  console.log(file);
}

// 保存到文件对话框
async function save_file_dialog() {
// 提示保存带有扩展名 .png 或 .jpeg 的 “我的过滤器”
  const path = await save({
    filters: [
      {
        name: 'My Filter',
        extensions: ['png', 'jpeg'],
      },
    ],
  });

  // 打印所选路径
  console.log(path);
}


// 你现在应该会看到一个启动画面窗口弹出，前端和后端将各自执行耗时 3 秒的初始化任务，完成后启动画面会消失，并显示主窗口
// 参考 https://v2.tauri.org.cn/learn/splashscreen/

// 在 TypeScript 中实现的一个 sleep 函数
// Promise 是 JavaScript 中用于处理异步操作的一种核心机制，它的出现极大地改善了过去“回调地狱”（Callback Hell）的问题，使异步代码更清晰、可读、易于管理。
// 这样就可以使用 .then、.catch、.finally 等方法来处理异步操作的结果。
const sleep = (seconds: number): Promise<void> => {
  return new Promise(resolve => {
    console.log('使用 setTimeout 模拟执行一个很重的前端设置任务...');
    setTimeout(resolve, seconds * 1000);
  });
};

// 设置函数
async function setup() {
  // 模拟执行一个很重的前端设置任务
  console.log('执行非常繁重的前端设置任务...')
  await sleep(3);
  console.log('前端设置任务完成!')
  // 设置前端任务为完成
  await invoke('set_complete', {task: 'frontend'})
}

onMounted(async () => {
  // 组件的模板已经被渲染到 DOM 中
  console.log('组件已挂载，DOM 可访问')
  await setup()
})


// // 移动端扫码测试
// import { scan, Format } from '@tauri-apps/plugin-barcode-scanner';
// // 当使用 '“withGlobalTauri”： true' 时，您可以使用
// // const { scan, Format } = window.__TAURI__.barcodeScanner;
//
// const barcode_scanner = async () => {
//   // 'windowed： true' 实际上将 WebView 设置为透明，而不是为相机打开单独的视图
//   // 确保您的用户界面已准备好使用透明元素显示下面的内容
//   // 开始扫描。
//   await scan(
//       {
//         // 创建一个窗口
//         windowed: true,
//         // 扫描二维码，还可以选择码，例如：扫描条形码
//         formats: [Format.QRCode],
//         // 使用后置摄像头
//         cameraDirection: 'back'
//       }
//   ).then((response) => {
//     console.log("扫描二维码成功，扫描结果为：" + response);
//     alert('扫码失败或取消');
//   }).catch((error) => {
//     console.log("扫描二维码失败、错误：" + JSON.stringify(error));
//     alert('扫码出错');
//   });
// }


</script>

<template>
  <main class="container">
    <h1>Welcome to Tauri + Vue</h1>

    <div class="row">
      <a href="https://vite.dev" target="_blank">
        <img src="/vite.svg" class="logo vite" alt="Vite logo"/>
      </a>
      <a href="https://tauri.app" target="_blank">
        <img src="/tauri.svg" class="logo tauri" alt="Tauri logo"/>
      </a>
      <a href="https://vuejs.org/" target="_blank">
        <img src="./assets/vue.svg" class="logo vue" alt="Vue logo"/>
      </a>
    </div>
    <p>Click on the Tauri, Vite, and Vue logos to learn more.</p>

    <form class="row" @submit.prevent="greet">
      <input id="greet-input" v-model="name" placeholder="Enter a name..."/>
      <button type="submit">Greet</button>
    </form>
    <p>{{ greetMsg }}</p>

    <div class="grid grid-cols-4 gap-3">
      <button @click="answer_dialog" class="mt-3 w-4">创建 Yes/No 对话框</button>
      <button @click="confirmation_dialog" class="mt-3 w-4">创建 Ok/Cancel 对话框</button>
      <button @click="message_dialog" class="mt-3 w-4">创建 Message 对话框</button>
      <button @click="open_file_dialog" class="mt-3 w-4">打开一个文件选择对话框</button>
      <button @click="save_file_dialog" class="mt-3 w-4">保存到文件对话框</button>

      <!--      <button @click="barcode_scanner" class="mt-3 w-4">移动端扫码测试</button>-->
    </div>

    <barcode-scanner></barcode-scanner>
  </main>
</template>

<style scoped lang="scss">
.logo.vite:hover {
  filter: drop-shadow(0 0 2em #747bff);
}

.logo.vue:hover {
  filter: drop-shadow(0 0 2em #249b73);
}

</style>
<style>
:root {
  font-family: Inter, Avenir, Helvetica, Arial, sans-serif;
  font-size: 16px;
  line-height: 24px;
  font-weight: 400;

  color: #0f0f0f;
  background-color: #f6f6f6;

  font-synthesis: none;
  text-rendering: optimizeLegibility;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  -webkit-text-size-adjust: 100%;
}

.container {
  margin: 0;
  padding-top: 10vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  text-align: center;
}

.logo {
  height: 6em;
  padding: 1.5em;
  will-change: filter;
  transition: 0.75s;
}

.logo.tauri:hover {
  filter: drop-shadow(0 0 2em #24c8db);
}

.row {
  display: flex;
  justify-content: center;
}

a {
  font-weight: 500;
  color: #646cff;
  text-decoration: inherit;
}

a:hover {
  color: #535bf2;
}

h1 {
  text-align: center;
}

input,
button {
  border-radius: 8px;
  border: 1px solid transparent;
  padding: 0.6em 1.2em;
  font-size: 1em;
  font-weight: 500;
  font-family: inherit;
  color: #0f0f0f;
  background-color: #ffffff;
  transition: border-color 0.25s;
  box-shadow: 0 2px 2px rgba(0, 0, 0, 0.2);
}

button {
  cursor: pointer;
}

button:hover {
  border-color: #396cd8;
}

button:active {
  border-color: #396cd8;
  background-color: #e8e8e8;
}

input,
button {
  outline: none;
}

#greet-input {
  margin-right: 5px;
}

@media (prefers-color-scheme: dark) {
  :root {
    color: #f6f6f6;
    background-color: #2f2f2f;
  }

  a:hover {
    color: #24c8db;
  }

  input,
  button {
    color: #ffffff;
    background-color: #0f0f0f98;
  }

  button:active {
    background-color: #0f0f0f69;
  }
}

</style>