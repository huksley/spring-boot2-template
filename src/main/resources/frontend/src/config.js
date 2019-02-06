const config = {
    chatUrl: process.env.CHAT_URL !== undefined ? process.env.CHAT_URL : "http://localhost:3000/api/chat/events",
    dev: process.env.NODE_ENV !== 'production',
    production: process.env.NODE_ENV === 'production',
    offlineFirst: process.env.REACT_APP_OFFLINE_FIRST !== undefined ? process.env.REACT_APP_OFFLINE_FIRST : false
}

export default config;
