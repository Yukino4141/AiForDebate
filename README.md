# 🎙️ AI For Debate (AI 世纪大辩论)

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-blue.svg" alt="Java 17">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen.svg" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Vue.js-3.0-4FC08D.svg" alt="Vue 3">
  <img src="https://img.shields.io/badge/Vite-5.0-646CFF.svg" alt="Vite">
  <img src="https://img.shields.io/badge/TailwindCSS-3.4-38B2AC.svg" alt="TailwindCSS">
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="MIT License">
</p>

> 让大语言模型为你展开脑洞大开的思想碰撞！🔥

**AI For Debate** 是一个基于 Spring Boot 和 Vue 3 的轻量级、响应式应用，允许用户配置两个不同的 AI 模型（例如 DeepSeek 和 Qwen）就特定议题展开多回合的激烈辩论。通过 WebSocket 实现服务端向前端的实时流式输出，带来极具沉浸感的“AI 斗嘴”体验。

---

## ✨ 核心特性

- 🤖 **多模型自由对抗**：内置对主流兼容 OpenAI 格式 API 的支持，默认集成 DeepSeek 与 Qwen（通义千问）的对战环境。
- ⚡ **实时流式响应**：后端基于 OkHttp-SSE 接收大模型输出，并通过 WebSocket 实时流式推送到前端，体验丝滑流畅。
- ⚙️ **高度可定制化**：支持自定义辩论议题、辩论回合数，以及对正反双方分别设置 System Prompt 角色设定（例如：理性的技术狂热者 vs 感性的人文主义者）。
- 🎨 **极具沉浸感的现代 UI**：基于 Vue 3 和 TailwindCSS 打造的高级感界面，毛玻璃（Glassmorphism）效果与动感气泡设计，兼顾桌面与移动端完美适配。
- 🔐 **本地隐私安全**：API Key 直接在浏览器端存储（LocalStorage）并发送至你的本地后端，不会上传至任何第三方服务器。

## 🛠️ 技术栈

### 后端 (Backend)
- **Java 17**
- **Spring Boot 3.2.4** (Web, WebSocket)
- **OkHttp / OkHttp-SSE** (处理大语言模型流式 HTTP 请求)
- **Jackson** (JSON 解析)

### 前端 (Frontend)
- **Vue 3** (Composition API, `<script setup>`)
- **Vite** (急速构建)
- **TailwindCSS** (原子化 CSS 快速构建现代 UI)
- **Axios**

---

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/your-username/AiForDebate.git
cd AiForDebate
```

### 2. 启动后端 (Spring Boot)

确保你本地已经安装了 JDK 17 及以上版本和 Maven。

```bash
cd backend
mvn clean install
mvn spring-boot:run
```
> 后端服务将默认在 `localhost:8080` 启动，并暴露 `/ws/debate` WebSocket 端点。

### 3. 启动前端 (Vue 3 + Vite)

确保你本地安装了 Node.js (推荐 v18+)。

```bash
cd frontend
npm install
npm run dev
```
> 前端服务通常会在 `localhost:5173` 启动。打开浏览器访问该地址即可。

---

## 🎮 怎么玩？

1. **设定议题**：在界面左侧输入你感兴趣的辩论话题（例如：“AI是否会取代人类？”）。
2. **设定回合**：选择双方交替发言的次数。
3. **配置正反方参数**：
   - 输入你所拥有的 API Key（支持 DeepSeek 或 阿里云 DashScope）。
   - 选择模型（例如 `deepseek-reasoner` 对战 `qwen-max`）。
   - 设定人设（System Prompt）：你可以给正方设定为极其激进的乐天派，给反方设定为极其保守的悲观派。
4. **启动辩论**：点击“🔥 启动世纪辩论”，端起一杯咖啡，看两位硅基生命如何引经据典、唇枪舌剑！

---

## 📸 界面预览

*<img width="2560" height="1433" alt="image" src="https://github.com/user-attachments/assets/9a0592d8-94e6-434b-8edf-4c2ed380f17f" />
*

| 配置页面 | 辩论进行中 |
| :---: | :---: |
| <!-- <img src="./docs/config.png" width="400" /> --> `等待添加截图` | <!-- <img src="./docs/debating.png" width="400" /> --> `等待添加截图` |

---

## 🤝 参与贡献

欢迎大家提交 Issue 或者 Pull Request 来共同完善这个好玩的项目！你可以尝试添加以下特性：
- 导出辩论记录为 Markdown/PDF 功能。
- 增加更多的预设 AI 平台（如 OpenAI, Anthropic 等）。
- 增加“裁判” AI 进行最终总结评分的机制。
- 添加语音合成 (TTS)，让辩论不仅能看还能“听”。

1. Fork 本仓库
2. 创建你的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的修改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送分支 (`git push origin feature/AmazingFeature`)
5. 开启一个 Pull Request

## 📄 许可证

本项目采用 [MIT License](LICENSE) 授权。自由使用，尽情发挥你的脑洞！
