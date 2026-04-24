<script setup>
import { ref, nextTick, computed, onMounted } from 'vue'

const isDebating = ref(false)
const socket = ref(null)
const chatContainer = ref(null)

const config = ref({
  topic: 'AI是否会取代人类？',
  proApiKey: '',
  conApiKey: '',
  proModel: 'deepseek-chat',
  conModel: 'qwen-turbo',
  proSystemPrompt: '作为正方，你认为技术发展无法阻挡且会带来巨变，你的反驳需要逻辑严密且富有攻击性。',
  conSystemPrompt: '作为反方，你认为人类独特的情感与创造力不可替代，你的反驳需要强调人文关怀和谨慎悲观。',
  rounds: 3
})

// messages item: { id, role: 'PRO'|'CON'|'SYSTEM', text: '' }
const messages = ref([])

onMounted(() => {
  const proKey = localStorage.getItem('proApiKey')
  if (proKey) config.value.proApiKey = proKey
  const conKey = localStorage.getItem('conApiKey')
  if (conKey) config.value.conApiKey = conKey
  
  const pModel = localStorage.getItem('proModel')
  if (pModel) config.value.proModel = pModel
  const cModel = localStorage.getItem('conModel')
  if (cModel) config.value.conModel = cModel
})

const scrollToBottom = async () => {
  await nextTick()
  if (chatContainer.value) {
    chatContainer.value.scrollTop = chatContainer.value.scrollHeight
  }
}

const startDebate = () => {
  if (!config.value.proApiKey || !config.value.conApiKey) {
    alert("请正确填写双方的 API Key！")
    return
  }
  
  // 保存到 LocalStorage
  localStorage.setItem('proApiKey', config.value.proApiKey)
  localStorage.setItem('conApiKey', config.value.conApiKey)
  localStorage.setItem('proModel', config.value.proModel)
  localStorage.setItem('conModel', config.value.conModel)
  
  messages.value = []
  isDebating.value = true
  
  // 动态判断 WebSocket 地址
  // 开发环境连 localhost:8080，生产环境（合并打包后）直接连当前主机的 /ws/debate
  const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  const wsUrl = import.meta.env.DEV 
    ? 'ws://localhost:8080/ws/debate' 
    : `${wsProtocol}//${window.location.host}/ws/debate`;
    
  socket.value = new WebSocket(wsUrl)
  
  socket.value.onopen = () => {
    socket.value.send(JSON.stringify(config.value))
  }
  
  socket.value.onmessage = (event) => {
    const data = JSON.parse(event.data)
    
    if (data.type === 'START_TURN' || data.type === 'INFO') {
      messages.value.push({
        id: Date.now() + Math.random(),
        role: data.role,  // SYSTEM, PRO, CON
        text: data.content
      })
      scrollToBottom()
    } else if (data.type === 'TOKEN') {
      // 找到最后一条对应角色的消息并追加文本
      const lastMsg = messages.value[messages.value.length - 1]
      if (lastMsg && lastMsg.role === data.role) {
        lastMsg.text += data.content
        scrollToBottom()
      }
    } else if (data.type === 'FINISH' || data.type === 'ERROR') {
       messages.value.push({
        id: Date.now() + Math.random(),
        role: 'SYSTEM',
        text: data.content
      })
      scrollToBottom()
    }
  }
  
  socket.value.onclose = () => {
    isDebating.value = false
  }
}

const stopDebate = () => {
  if (socket.value) {
    socket.value.close()
  }
  isDebating.value = false
}

const formatText = (txt) => {
  return txt.replace(/\n/g, '<br/>')
}
</script>

<template>
  <div class="min-h-screen bg-slate-900 text-slate-100 p-4 md:p-8 font-sans flex flex-col items-center">
    
    <header class="w-full max-w-4xl text-center mb-8">
      <h1 class="text-4xl md:text-5xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-blue-400 to-emerald-400 drop-shadow-sm pb-2">
        AI 世纪大辩论
      </h1>
      <p class="text-slate-400 text-sm mt-2">让大语言模型为你展开脑洞大开的思想碰撞</p>
    </header>

    <div class="w-full max-w-4xl grid grid-cols-1 lg:grid-cols-3 gap-6">
      
      <!-- 左侧配置区 -->
      <aside v-if="!isDebating" class="lg:col-span-3 glass-dark p-6 rounded-2xl">
        <h2 class="text-xl font-bold mb-4 flex items-center gap-2">
          <span class="w-2 h-6 bg-blue-500 rounded-full"></span>
          赛前配置
        </h2>
        
        <div class="space-y-5">
          <div>
            <label class="block text-sm font-medium text-slate-300 mb-1">辩论主题题</label>
            <input v-model="config.topic" type="text" class="w-full bg-slate-800 border border-slate-700 rounded-lg px-4 py-3 focus:outline-none focus:ring-2 focus:ring-blue-500 transition" placeholder="输入辩论话题"/>
          </div>
          <div>
             <label class="block text-sm font-medium text-slate-300 mb-1">辩论回合数</label>
             <input v-model="config.rounds" type="number" min="1" max="10" class="w-full bg-slate-800 border border-slate-700 rounded-lg px-4 py-3 focus:outline-none focus:ring-2 focus:ring-blue-500 transition"/>
          </div>

          <hr class="border-slate-700"/>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
             <!-- 正方 DeepSeek -->
            <div class="bg-blue-900/20 p-4 rounded-xl border border-blue-800/50 relative overflow-hidden">
              <div class="absolute top-0 right-0 bg-blue-600 text-xs px-2 py-1 rounded-bl-lg font-bold">正方 API (兼容OpenAI)</div>
              
              <label class="block text-sm font-medium text-blue-200 mb-1 mt-4">偏好机型</label>
              <select v-model="config.proModel" class="w-full bg-slate-800/80 border border-slate-700 rounded-lg px-4 py-2 mb-3 text-sm focus:outline-none focus:ring-1 focus:ring-blue-500">
                <option value="deepseek-chat">deepseek-chat</option>
                <option value="deepseek-reasoner">deepseek-reasoner</option>
                <option value="qwen-turbo">qwen-turbo</option>
                <option value="qwen-plus">qwen-plus</option>
                <option value="qwen-max">qwen-max</option>
              </select>

              <label class="block text-sm font-medium text-blue-200 mb-1">API Key</label>
              <input v-model="config.proApiKey" type="password" class="w-full bg-slate-800/80 border border-slate-700 rounded-lg px-4 py-2 mb-3 text-sm" placeholder="sk-..."/>
              
              <label class="block text-sm font-medium text-blue-200 mb-1">体系 Prompt 指引</label>
              <textarea v-model="config.proSystemPrompt" rows="3" class="w-full bg-slate-800/80 border border-slate-700 rounded-lg px-4 py-2 text-sm text-slate-300"></textarea>
            </div>

            <!-- 反方 Qwen -->
            <div class="bg-emerald-900/20 p-4 rounded-xl border border-emerald-800/50 relative overflow-hidden">
              <div class="absolute top-0 right-0 bg-emerald-600 text-xs px-2 py-1 rounded-bl-lg font-bold">反方 API (兼容OpenAI)</div>
              
              <label class="block text-sm font-medium text-emerald-200 mb-1 mt-4">偏好机型</label>
              <select v-model="config.conModel" class="w-full bg-slate-800/80 border border-slate-700 rounded-lg px-4 py-2 mb-3 text-sm focus:outline-none focus:ring-1 focus:ring-emerald-500">
                <option value="qwen-turbo">qwen-turbo</option>
                <option value="qwen-plus">qwen-plus</option>
                <option value="qwen-max">qwen-max</option>
                <option value="deepseek-chat">deepseek-chat</option>
                <option value="deepseek-reasoner">deepseek-reasoner</option>
              </select>

              <label class="block text-sm font-medium text-emerald-200 mb-1">API Key</label>
              <input v-model="config.conApiKey" type="password" class="w-full bg-slate-800/80 border border-slate-700 rounded-lg px-4 py-2 mb-3 text-sm" placeholder="sk-..."/>
              
              <label class="block text-sm font-medium text-emerald-200 mb-1">体系 Prompt 指引</label>
              <textarea v-model="config.conSystemPrompt" rows="3" class="w-full bg-slate-800/80 border border-slate-700 rounded-lg px-4 py-2 text-sm text-slate-300"></textarea>
            </div>
          </div>

          <button @click="startDebate" class="w-full mt-6 bg-gradient-to-r from-blue-600 to-emerald-600 hover:from-blue-500 hover:to-emerald-500 text-white font-bold py-4 rounded-xl shadow-lg transition-all transform active:scale-[0.98]">
            🔥 启动世纪辩论
          </button>
        </div>
      </aside>

      <!-- 辩论展示区/聊天气泡区 -->
      <main v-if="isDebating" class="lg:col-span-3 glass-dark p-4 rounded-2xl flex flex-col h-[75vh]">
        <div class="flex justify-between items-center mb-4 px-2 pb-2 border-b border-slate-700">
           <h2 class="text-lg font-bold flex items-center gap-2 w-full truncate text-slate-300">
             议题：<span class="text-white">{{config.topic}}</span>
           </h2>
           <button @click="stopDebate" class="ml-4 shrink-0 text-sm bg-red-500/20 text-red-400 hover:bg-red-500/40 px-4 py-2 rounded-lg transition">中止辩论</button>
        </div>
        
        <div ref="chatContainer" class="flex-1 overflow-y-auto pr-2 space-y-6 scroll-smooth">
          
          <div v-for="msg in messages" :key="msg.id" class="flex w-full" 
               :class="{
                 'justify-center': msg.role === 'SYSTEM',
                 'justify-start': msg.role === 'PRO',
                 'justify-end': msg.role === 'CON'
               }">
            
            <!-- SYSTEM Notice -->
            <div v-if="msg.role === 'SYSTEM'" class="bg-slate-800/60 px-4 py-2 rounded-full text-xs text-slate-400 my-2">
              {{ msg.text }}
            </div>

            <!-- PRO Bubble -->
            <div v-if="msg.role === 'PRO'" class="flex max-w-[85%] md:max-w-[70%] items-start gap-3">
              <div class="w-10 h-10 rounded-full bg-blue-600 flex items-center justify-center shrink-0 border-2 border-blue-400 font-bold shadow-[0_0_15px_rgba(37,99,235,0.5)]">
                D
              </div>
              <div class="bg-slate-800 border border-blue-900/50 p-4 rounded-2xl rounded-tl-sm shadow-md">
                 <div class="text-blue-400 text-xs font-bold mb-1">正方 / {{config.proModel}}</div>
                 <div class="text-slate-200 text-sm leading-relaxed" v-html="formatText(msg.text)"></div>
              </div>
            </div>

            <!-- CON Bubble -->
            <div v-if="msg.role === 'CON'" class="flex max-w-[85%] md:max-w-[70%] items-start gap-3 flex-row-reverse">
              <div class="w-10 h-10 rounded-full bg-emerald-600 flex items-center justify-center shrink-0 border-2 border-emerald-400 font-bold shadow-[0_0_15px_rgba(16,185,129,0.5)]">
                Q
              </div>
              <div class="bg-emerald-900/40 border border-emerald-800/50 p-4 rounded-2xl rounded-tr-sm shadow-md">
                 <div class="text-emerald-400 text-xs text-right font-bold mb-1">反方 / {{config.conModel}}</div>
                 <div class="text-slate-200 text-sm leading-relaxed" v-html="formatText(msg.text)"></div>
              </div>
            </div>

          </div>
          
        </div>
      </main>

    </div>
  </div>
</template>
