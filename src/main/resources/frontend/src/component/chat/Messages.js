import React, { Component } from "react";
import { Row, Col, Card } from "reactstrap";
import { Message } from "./Message";

export class Messages extends Component {
  scrollToBottom = () => {
    let el = document.querySelector(".messages div.card");
    if (el) {
      el.scrollTop = el.scrollHeight;
    }
  };

  componentDidMount() {
    this.scrollToBottom();
  }

  componentDidUpdate() {
    this.scrollToBottom();
  }

  render() {
    return (
      <Row>
        <Col md={{ size: 6, offset: 3 }} className="messages">
          <Card body>
            {this.props.messages.map(message => {
              return <Message message={message} key={message.id} />;
            })}
          </Card>
        </Col>
      </Row>
    );
  }
}
