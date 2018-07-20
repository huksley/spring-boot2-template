const config = {
    dev: process.env.NODE_ENV !== 'production',
    production: process.env.NODE_ENV === 'production',
    offlineFirst: process.env.REACT_APP_OFFLINE_FIRST !== undefined ? process.env.REACT_APP_OFFLINE_FIRST : false
}

export default config;
