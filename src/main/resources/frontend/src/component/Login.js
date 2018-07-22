import React, { Component } from "react";
import { Button, Input } from "reactstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Link as RouteLink, Redirect } from "react-router-dom";

export default class Login extends Component {
  constructor(props) {
    super(props);
    this.state = {
      username: "",
      password: "",
      error: null,
      newAuth: this.props.auth
    };
    this.handleClick = this.handleClick.bind(this);
    this.handleUsername = this.handleUsername.bind(this);
    this.handlePassword = this.handlePassword.bind(this);
  }

  componentDidMount() {}

  handleUsername(event) {
    this.setState({ username: event.target.value });
  }

  handlePassword(event) {
    this.setState({ password: event.target.value });
  }

  handleClick() {
    this.setState({ error: "" });
    // eslint-disable-next-line
    if (this.state.username == "") {
      this.setState({ error: "Specify username" });
      return;
    }
    // eslint-disable-next-line
    if (this.state.password == "") {
      this.setState({ error: "Specify password" });
      return;
    }

    var responseUrl = null;
    const body = "username=" + this.state.username + "&password=" + this.state.password;
    fetch("/auth/authenticate", {
      method: "POST",
      mode: "same-origin",
      credentials: "same-origin",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
        Accept: "application/json"
      },
      body: body
    })
      .then(response => { responseUrl = response.url; return response.json() }) // parses response to JSON
      .catch(error => {
        console.error(`Fetch Error =\n`, error);
        this.setState({ error: error.message });
      })
      .then(data => {
        console.log("Got data from url " + responseUrl, data)
        if (!data) {
          this.setState({ error: "Invalid response from server" })
        } else
        if (data.error) {
          this.setState({ error: data.error })
        } else {
          this.props.handler();
          this.setState({newAuth: data})
        }
      });
  }

  render() {
    return this.state.newAuth.login ? (
      <Redirect to={this.props.redirect} />
    ) : (
      <div className="container-fluid">
        <div className="row">
          <div className="col-sm">&nbsp;</div>
          <div className="col-sm text-center">
            <div className="bg-secondary clearfix px-2 py-2 rounded-top">
              <h3>My application</h3>
            </div>

            <div className="bg-primary text-left px-3 py-3">
              <div className="form-group">
                <label htmlFor="loginUsername">Username</label>
                <Input
                  type="username"
                  className="form-control initial-focus"
                  id="loginUsername"
                  value={this.state.username}
                  onChange={this.handleUsername}
                  placeholder="Enter username"
                />
              </div>
              <div className="form-group">
                <label htmlFor="loginPassword">Password</label>
                <Input
                  type="password"
                  className="form-control"
                  id="loginPassword"
                  placeholder="Password"
                  value={this.state.password}
                  onChange={this.handlePassword}
                />
              </div>

              {this.state.error ? (
                <div className="bg-danger px-2 rounded">{this.state.error}</div>
              ) : (
                <div />
              )}
            </div>

            <div className="bg-info clearfix px-2 py-2 rounded-bottom">
              <Button
                className="btn btn-secondary float-left"
                onClick={this.handleClick}>
                <FontAwesomeIcon icon="sign-in-alt" /> Login
              </Button>
              <Button
                tag={RouteLink}
                to="/"
                className="btn btn-danger float-right"
              >
                Cancel
              </Button>
            </div>
          </div>
          <div className="col-sm">&nbsp;</div>
        </div>
      </div>
    );
  }
}
