import React from "react";
import { Row, Col, Badge } from "reactstrap";
import { StringColor, TextColor } from "./StringColor";

export const Message = props => {
  return (
    <Row className="messageRow">
      <Col className="message">
        <div>
          <Badge color="custom" style={{backgroundColor: StringColor(props.message.userName),  color: TextColor(StringColor(props.message.userName))}}>{props.message.userName}</Badge>{' '}
          <span className="date">{
            String(props.message.date).split("T")[1].substring(0, 8)
          }</span>
        </div>
        <div>{props.message.message}</div>
      </Col>
    </Row>
  );
};
