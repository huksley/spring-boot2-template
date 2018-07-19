import 'bootstrap/dist/css/bootstrap.css';

import React from 'react';
import ReactDOM from 'react-dom';
import { BrowserRouter as Router } from 'react-router-dom';
import './index.css';
import App from './App';
import registerServiceWorker from './registerServiceWorker';
import config from './config';

class Root extends React.Component {
    render() {
        return (
            <Router>
                <App />
            </Router>
        )
    }
}
  
ReactDOM.render(<Root />, document.getElementById('root'));

if (config.dev) {
    // Don`t enable service working to avoid caching
    // https://goo.gl/KwvDNy
} else 
if (config.production) {
    registerServiceWorker();
}

