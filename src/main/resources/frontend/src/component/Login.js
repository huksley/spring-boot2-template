import React, { Component } from "react";
import { Button } from "reactstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { Link as RouteLink, Redirect } from "react-router-dom";
import url from "url";

export default class Login extends Component {
  constructor(props) {
    super(props);
    this.state = {
      username: "",
      password: "",
      newPassword: "",
      newPassword2: "",
      error: null,
      errorType: "", // CredentialsExpired
      newAuth: this.props.auth
    };
    this.handleClick = this.handleClick.bind(this);
    this.handleUsername = this.handleUsername.bind(this);
    this.handlePassword = this.handlePassword.bind(this);
    this.handleNewPassword = this.handleNewPassword.bind(this);
    this.handleNewPassword2 = this.handleNewPassword2.bind(this);
    this.refUsername = React.createRef();
    this.refPassword = React.createRef();
    this.refNewPassword = React.createRef();
    this.refNewPassword2 = React.createRef();
  }

  componentDidMount() {
    console.log("Focus on", this.refUsername.current)
    this.refUsername.current.focus();
  }

  handleUsername(event) {
    this.setState({ username: event.target.value });
  }

  handlePassword(event) {
    this.setState({ password: event.target.value });
  }

  handleNewPassword(event) {
    this.setState({ newPassword: event.target.value });
  }

  handleNewPassword2(event) {
    this.setState({ newPassword2: event.target.value });
  }

  handleClick(changePassword, event) {
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

    // eslint-disable-next-line
    if (changePassword && this.state.newPassword == "") {
      this.setState({ error: "Specify new password" });
      return;
    }
    
    // eslint-disable-next-line
    if (changePassword && this.state.newPassword2 !== this.state.newPassword) {
      this.setState({ error: "New password does not match" });
      return;
    }

    var responseUrl = null;
    let body = "username=" + encodeURIComponent(this.state.username) + "&password=" + encodeURIComponent(this.state.password);
    if (changePassword) {
      body += "&newPassword=" + encodeURIComponent(this.state.newPassword);
    }

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
        const u = responseUrl ? url.parse(responseUrl, true) : {};
        console.log("Got data from url ", u, data)
        if (!data) {
          this.setState({ error: "Invalid response from server" })
        } else
        if (data.error) {
          this.setState({ error: data.error, errorType: u.query && u.query.error ? u.query.error : "unknown" })
        } else {
          this.props.handler();
          this.setState({newAuth: data})
        }
      });
  }

  handleEnter(changePassword, focusRef, event) {
    if (event.key === 'Enter') {
      if (focusRef) {
        focusRef.current.focus();
      } else {
        this.handleClick(changePassword, event);
      }
      event.preventDefault();
    }
  }

  renderChangePassword() {
    return <div className="container-fluid">
      <div className="row">
        <div className="col-sm">&nbsp;</div>
        <div className="col-sm text-center">
          <div className="bg-secondary clearfix px-2 py-2 rounded-top">
            <h3>Change password</h3>
          </div>
          <div className="bg-primary text-left px-3 py-3">
            <div className="form-group">
              <label htmlFor="loginUsername">Old password</label>
              <input
                type="password"
                className="form-control initial-focus"
                id="loginOldPassword"
                readOnly={true}
                ref={this.refPassword}
                value={this.state.password}
                onChange={this.handlePassword}
                placeholder="Enter username"
                onKeyPress={(e) => this.handleEnter(false, this.refNewPassword, e)}
              />
            </div>
            <div className="form-group">
              <label htmlFor="loginPassword">New password</label>
              <input
                type="password"
                className="form-control"
                id="loginNewPassword"
                ref={this.refNewPassword}
                placeholder="New password"
                value={this.state.newPassword}
                onChange={this.handleNewPassword}
                onKeyPress={(e) => this.handleEnter(false, this.refNewPassword2, e)}
              />
            </div>
            <div className="form-group">
              <label htmlFor="loginPassword">Repeat</label>
              <input
                type="password"
                className="form-control"
                id="loginNewPassword2"
                ref={this.refNewPassword2}
                placeholder="Repeat new password"
                value={this.state.newPassword2}
                onChange={this.handleNewPassword2}
                onKeyPress={(e) => this.handleEnter(true, null, e)}
              />
            </div>
            {this.state.error ? (
              <div className="bg-danger px-2 rounded" id="loginErrorMessage">{this.state.error}</div>
            ) : (
              <div id="loginErrorMessage"/>
            )}
          </div>

          <div className="bg-info clearfix px-2 py-2 rounded-bottom">
            <Button
              className="btn btn-secondary float-left"
              onClick={(e) => this.handleClick(true, e)}>
              <FontAwesomeIcon icon="sign-in-alt" /> Change password and login
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
  }

  renderLoginForm() {
    return <div className="container-fluid">
      <div className="row">
        <div className="col-sm">&nbsp;</div>
        <div className="col-sm text-center">
          <div className="bg-secondary clearfix px-2 py-2 rounded-top">
            <h3>My application</h3>
          </div>

          <div className="bg-primary text-left px-3 py-3">
            <div className="form-group">
              <label htmlFor="loginUsername">Username</label>
              <input
                type="username"
                className="form-control initial-focus"
                id="loginUsername"
                ref={this.refUsername}
                value={this.state.username}
                onChange={this.handleUsername}
                placeholder="Enter username"
                onKeyPress={(e) => this.handleEnter(false, this.refPassword, e)}
              />
            </div>
            <div className="form-group">
              <label htmlFor="loginPassword">Password</label>
              <input
                type="password"
                className="form-control"
                id="loginPassword"
                ref={this.refPassword}
                placeholder="Password"
                value={this.state.password}
                onChange={this.handlePassword}
                onKeyPress={(e) => this.handleEnter(false, null, e)}
              />
            </div>

            {this.state.error ? (
              <div className="bg-danger px-2 rounded">{this.state.error}</div>
            ) : (
              <div />
            )}
          </div>
          <div className="bg-info clearfix px-2 py-2">
                      <Button
                        className="btn btn-secondary float-left"
                        id="buttonLogin"
                        onClick={(e) => this.handleClick(false, e)}>
                        <FontAwesomeIcon icon="sign-in-alt" /> Login
                      </Button>
                      <Button
                        tag={RouteLink}
                        to="/"
                        id="buttonLoginCancel"
                        className="btn btn-danger float-right"
                      >
                        Cancel
                      </Button>
                    </div>

          <div className="bg-info clearfix px-2 py-2 rounded-bottom">
            <Button
              className="btn btn-primary float-left"
              onClick={(e) => { window.location = '/oauth2/authorization/google' }}>
              <FontAwesomeIcon icon="sign-in-alt" /> Login with Google
            </Button>
            <Button
              className="btn btn-primary float-right"
              onClick={(e) => { window.location = '/oauth2/authorization/github' }}>
              <FontAwesomeIcon icon="sign-in-alt" /> Login with GitHub
            </Button>
          </div>
        </div>
        <div className="col-sm">&nbsp;</div>
      </div>
    </div>
  }

  render() {
    return ( 
      this.state.errorType === "CredentialsExpired" ? 
        this.renderChangePassword() 
        :
        (
          this.state.newAuth.login ? 
            <Redirect to={this.props.redirect} />
            : 
            this.renderLoginForm()
        )
    )
  }
}
