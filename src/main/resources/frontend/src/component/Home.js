import React, { Component } from "react";

export default class Home extends Component {
  constructor(props) {
    super(props);
    this.state = {};
  }

  componentDidMount() {}

  render() {
    return (
      <div className="container-fluid">
        <h4>Hello</h4>
        <p>
          This is sample Spring Boot2 application. Frontend is written using
          ReactJS + React Router + Reactstrap.
        </p>
      </div>
    );
  }
}
