import React, { Component } from 'react';
import './App.css';

import { library } from '@fortawesome/fontawesome-svg-core'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faUser, faCogs, faSignInAlt, faSignOutAlt, faTrash } from '@fortawesome/free-solid-svg-icons'

import { Route, Link as RouteLink, Redirect } from 'react-router-dom';

import {
  Collapse,
  Card, CardBody, Navbar,
  NavbarToggler,
  NavbarBrand,
  Nav,
  NavLink
} from 'reactstrap';

import Login from './component/Login'
import Logout from './component/Logout'
import Home from './component/Home'
import Todo from './component/Todo'
import ProtectedRoute from './component/ProtectedRoute'

library.add(faUser, faCogs, faSignInAlt, faSignOutAlt, faTrash)

class App extends Component {
  constructor(props) {
    super(props);
    this.toggle = this.toggle.bind(this);
    this.state = {
      isOpen: false,
      auth: {}
    };
    this.loggedIn = this.loggedIn.bind(this);
    this.loggedOut = this.loggedOut.bind(this);
  }

  componentDidMount() {
    this.getData();
  }

  getData() {
    fetch('/auth/info', { mode: 'no-cors', credentials: 'same-origin', headers: { "Accept": "application/json" } })
      .then(response => response.json())
      .then(data => this.setState({ auth: data }));
  }

  loggedIn() {
    console.log("Logged in")
    this.getData();
  }

  loggedOut() {
    console.log("Logged out")
    this.getData();
  }

  toggle() {
    this.setState({
      isOpen: !this.state.isOpen
    });
  }

  authorized() {
    // eslint-disable-next-line
    return this.state.auth.login != null && this.state.auth.login != '';
  }

  render() {
    return (
      <div>
        <Navbar color="light" light expand="md">
          <NavbarBrand tag={RouteLink}  to="/" id="linkBrand">
            <FontAwesomeIcon icon="cogs" />
            &nbsp;
            Sample Web App
          </NavbarBrand>
          <NavbarToggler onClick={this.toggle} />
          <Collapse isOpen={this.state.isOpen} navbar>
            <Nav className="ml-auto" navbar>
              
              { this.authorized() ? 
                <span><NavLink tag={RouteLink} to="/logout" id="linkLogout"><FontAwesomeIcon icon="sign-out-alt" /> Logout ({this.state.auth.login})</NavLink></span> :
                <span><NavLink tag={RouteLink} to="/login" id="linkLogin"><FontAwesomeIcon icon="sign-in-alt" /> Login</NavLink></span>
              }
            </Nav>
          </Collapse>
        </Navbar>

        <Card className="rounded-0">
          <CardBody className="content">
              <Route exact path="/index.html" render={() => {
                  return <Redirect to={'/'} />
              }} />

              <Route exact path="/" render={() => {
                  return <Home auth={this.state.auth} authorized={this.authorized()} />
              }} />

              <Route path="/login" render={() => {
                  return <Login auth={this.state.auth} redirect="/" handler={this.loggedIn} />
              }} />

              <Route path="/logout" render={() => {
                  return <Logout auth={this.state.auth} redirect="/" handler={this.loggedOut} />
              }} />

              { this.authorized() ?
              <ProtectedRoute path="/todo" authorized={this.authorized()} component={Todo} />
              : <span/> 
              }
          </CardBody>
        </Card>
      </div>
    );
  }
}

export default App;
