import React, { Component } from "react";
import { Row, Col, Badge } from "reactstrap";
import { api } from "./api";
import { StringColor, TextColor } from "./StringColor";

export class ActiveUsers extends Component {
  constructor(props) {
    super(props);
    this.state = {
      usersList: []
    };
  }

  componentDidMount() {
    api.getUsers(usersList => this.setState({ usersList }));
  }

  showUsersName = () => {
    let arr = [];

    this.state.usersList.map(user => {
      return arr.push(<Badge color="custom" style={{backgroundColor: StringColor(user), color: TextColor(StringColor(user))}} key={user}>{user}</Badge>);
    });

    return arr;
  };

  render() {
    return (
      <Row>
        <Col md={{ size: 6, offset: 3 }}>
          <p className="activeusers">
            Active users {this.state.usersList && this.showUsersName()}
          </p>
        </Col>
      </Row>
    );
  }
}
