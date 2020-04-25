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
            <div>Logged in as {this.props.auth.login}! 
            Add some todo! <Button tag={RouteLink} className="btn-primary" to="/todo">Todo</Button>
            Start chat! <Button tag={RouteLink} className="btn-primary" to="/chat">Todo</Button>
            </div>
          }
          </div>

          <p>
            <h4>Other resources</h4>
            <ul>
                <li><a href="/swagger-ui.html">Swagger UI</a></li>
                <li><a href="/api/openapi.json">OpenAPI (Swagger) specification</a></li>
                <li><a href="/management/info">Management Info (protected)</a></li>
                <li><a href="/management/health">Management Health (unprotected)</a></li>
            </ul>
          </p>
        </div>
      </div>
    );
  }
}
