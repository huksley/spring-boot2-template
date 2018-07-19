import React, { Component } from 'react';
import { } from 'reactstrap';
import { Redirect } from 'react-router-dom'

export default class Home extends Component {
  constructor(props) {
      super(props);
      this.state = {
        loggedOut: false
      }
  }

  componentDidMount() {
    fetch("/auth/logout", {
      method: "POST",
      mode: "same-origin",
      credentials: "same-origin",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
        "Accept": "application/json"
      },
      body: "username=" + this.state.username + "&password=" + this.state.password
    })
    .then(response => response.json()) // parses response to JSON
    .catch(error => { console.error(`Fetch Error =\n`, error); this.setState({ error: error.message }); } )
    .then(data => { this.props.handler(); this.setState({ loggedOut: true }); });
  }

  render() {
    return (
            this.state.loggedOut ? 
            <Redirect to={this.props.redirect} /> : 
            <div className="container-fluid">
              Logging you out....
            </div>
    );
  }
}