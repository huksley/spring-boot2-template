const config = {
    chatMockApi: process.env.REACT_APP_CHAT_MOCK_API !== undefined ? process.env.REACT_APP_CHAT_MOCK_API : undefined,
    chatUrl: process.env.REACT_APP_CHAT_URL !== undefined ? process.env.REACT_APP_CHAT_URL : "ws://localhost:3000/api/chat/events",
    dev: process.env.NODE_ENV !== 'production',
    production: process.env.NODE_ENV === 'production',
    offlineFirst: process.env.REACT_APP_OFFLINE_FIRST !== undefined ? process.env.REACT_APP_OFFLINE_FIRST : false
}

console.log(process.env)

export default config;
