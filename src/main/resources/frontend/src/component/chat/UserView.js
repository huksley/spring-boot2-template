import React, { Component } from "react";
import { Row, Col, Button } from "reactstrap";
import { Messages } from "./Messages";
import NewMessage from "./NewMessage";
import { ActiveUsers } from "./ActiveUsers";

export default class UserView extends Component {
  render() {
    return (
      <Row className="userview-row align-items-center">
        <Col>
          <Messages messages={this.props.messages} />
          <NewMessage
            addNewMessage={this.props.addNewMessage}
            userName={this.props.userName}
          />
          <ActiveUsers userName={this.props.userName} />
          <Row>
            <Col md={{ size: 6, offset: 3 }}>
              <Button onClick={this.props.resetMessages}>
                Clear chat
              </Button>
            </Col>
          </Row>
        </Col>
      </Row>
    );
  }
}
