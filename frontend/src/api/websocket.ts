import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { ref } from 'vue'

export const stompClient = new Client({
  webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
  reconnectDelay: 5000,
})

export const isConnected = ref(false)

const connectHandlers: (() => void)[] = []

export const onStompConnect = (handler: () => void) => {
  connectHandlers.push(handler)
  if (isConnected.value) {
    handler()
  }
}

stompClient.onConnect = () => {
  isConnected.value = true
  connectHandlers.forEach(handler => handler())
}

stompClient.onDisconnect = () => {
  isConnected.value = false
}

stompClient.onWebSocketClose = () => {
  isConnected.value = false
}