import React, { Component } from "react";

import { Link as RouteLink } from 'react-router-dom';

import { Button } from "reactstrap";

export default class Home extends Component {
  constructor(props) {
    super(props);
    this.state = {

    };
  }

  componentDidMount() {}

  render() {
    return (
      <div className="container-fluid">
        <h4>Hello</h4>
        <div>
          This is sample Spring Boot2 application. Frontend is written using
          ReactJS + React Router + Reactstrap.
          <div className="py-4">
          { !this.props.authorized ? 
            <div>Login now using <b>username</b>: <code>test</code>, <b>password</b>: <code>123</code></div>
            :
            <div>Logged in as {this.props.auth.login}! Add some todo! <Button tag={RouteLink} className="btn-primary" to="/todo">Todo</Button></div>
          }
          </div>
        </div>
      </div>
    );
  }
}
