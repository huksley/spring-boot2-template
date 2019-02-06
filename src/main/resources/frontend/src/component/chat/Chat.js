import React, { Component } from "react";
import { Route } from "react-router-dom";
import { Container } from "reactstrap";

import UserView from "./UserView";
import { api } from "./api";
import getRandomName from "./RandomName";
import uuidv4 from "uuid/v4";
import "./Chat.css";

class Chat extends Component {
  constructor(props) {
    super(props);

    this.state = {
      messages: [],
      userName: getRandomName(undefined, true)
    };

    api.open(_ => {
      api.newUser(this.state.userName);
      api.listenMessage(this.handleMessages);
    })
  }

  componentDidMount() {
    this.addNewMessage({
      message: this.state.userName + " enters chat",
      userName: this.state.userName,
      id: uuidv4(),
      date: new Date().toISOString()
    })
    window.onbeforeunload = () => {
      api.close(this.props.userName);
    };
  }

  checkStatus = () => false;

  addNewMessage = fullMessage => {
    api.send(fullMessage);
  };

  handleMessages = message => {
    console.log("Received new message", message);
    this.setState({ messages: this.state.messages.concat(message) });
  };

  resetMessages = () => {
    this.setState({ messages: [] });
  };

  handleUserNameValue = e => {
    this.setState({ userName: e.target.value });
  };

  render() {
    return (
      <Container>
        <Route
          path="/chat"
          render={() => (
            <UserView
              messages={this.state.messages}
              resetMessages={this.resetMessages}
              addNewMessage={this.addNewMessage}
              userName={this.state.userName}
            />
          )}
        />
      </Container>
    );
  }
}

export default Chat;
