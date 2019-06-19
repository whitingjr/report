import React from 'react';
import ReactDOM from 'react-dom';

import '@patternfly/patternfly/patternfly.css'; //have to use this import to customize scss-variables.scss
//import "@patternfly/react-core/dist/styles/base.css";

import { Provider } from 'react-redux'
import { Route, Switch } from 'react-router'
import { ConnectedRouter } from 'connected-react-router'

import './index.css';
import App from './App';
import configureStore, { history } from './configureStore';

const store = configureStore({})

ReactDOM.render(
    <Provider store={store}>
        <ConnectedRouter history={history}>
            <App />
        </ConnectedRouter>
    </Provider>
    
, document.getElementById('root'));
