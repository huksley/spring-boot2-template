import React, { Component } from "react";

import { Table, Button } from "reactstrap";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";

export default class Todo extends Component {
  constructor(props) {
    super(props);
    this.state = {
      todos: [],
      latestTodo: null,
      newTodoDescription: ""
    };
    this.handleChangeDone = this.handleChangeDone.bind(this)
    this.handleNewTodo = this.handleNewTodo.bind(this)
    this.handleNewTodoKey = this.handleNewTodoKey.bind(this)
    this.handleDelete = this.handleDelete.bind(this)
  }

  componentDidMount() {
    fetch("/api/todo/list", {
      method: "GET",
      mode: "same-origin",
      credentials: "same-origin",
      headers: {
        Accept: "application/json"
      }
    })
      .then(response => response.json()) // parses response to JSON
      .catch(error => {
        console.error(`Fetch Error =\n`, error);
        this.setState({ error: error.message });
      })
      .then(data => {
        if (data.error) {
          this.setState({ error: data.error });
        } else {
          console.log("Got data", data)
          this.setState({ todos: data })
        }
      });
  }

  saveTodo(todo, create, callback) {
    fetch(create ? "/api/todo/" : "/api/todo/" + todo.id, {
      method: create ? "POST" : "PATCH",
      mode: "same-origin",
      credentials: "same-origin",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json"
      },
      body: JSON.stringify(todo)
    })
      .then(response => response.json()) // parses response to JSON
      .catch(error => {
        console.error(`Fetch Error =\n`, error);
        this.setState({ error: error.message });
      })
      .then(data => {
        console.log("Got data", data)
        this.setState({ latestTodo: data })
        callback(data);
      });
  }

  deleteTodo(id, callback) {
    fetch("/api/todo/" + id, {
      method: 'DELETE',
      mode: "same-origin",
      credentials: "same-origin",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json"
      },
      body: "{}"
    })
      .then(response => response.json()) // parses response to JSON
      .catch(error => {
        console.error(`Fetch Error =\n`, error);
        this.setState({ error: error.message });
      })
      .then(data => {
        console.log("Got data", data)
        callback(data);
      });
  }

  handleDelete(todo, index, event) {
    console.log("Delete event " + event.target.value + ", " + event.key);
    this.deleteTodo(todo.id, () => {
      let arr = this.state.todos;
      arr.splice(index, 1);
      this.setState({ todos: arr });
      this.forceUpdate();
    });
    event.preventDefault();
  }

  handleNewTodoKey(event) {
    console.log("NewTodo event " + event.target.value + ", " + event.key);
    if (event.key === 'Enter') {
      this.saveTodo({ done: false, description: this.state.newTodoDescription }, true, (todo) => {
        this.setState({ newTodoDescription: "" });
        this.setState({ todos: [ todo, ...this.state.todos ]});
        this.forceUpdate();
      });
      event.preventDefault();
    }
  }

  handleNewTodo(event) {
    console.log("NewTodo event " + event.target.value + ", " + event.key);
    this.setState({ newTodoDescription: event.target.value })
    event.preventDefault();
  }

  handleChangeDone(todo, index, event) {
    console.log("Done: " + todo + ", index: " + index + ", event " + event.target.value + ", " + event.target.checked);
    todo.done = event.target.checked;
    this.saveTodo(todo, false, () => {
      this.forceUpdate();
    });
    event.preventDefault();
  }

  render() {
    return (
      <div className="container-fluid">
        <h4>List of todos</h4>
        { this.state.error ? <div className="alert alert-danger" id="labelTodoError">{this.state.error}</div> : ""}
        <div class="row">
          <div class="col-sm col-md">
            &nbsp;
          </div>
          <div class="col-sm col-md">
            <Table striped>
            <tbody>

              <tr>
                  <td></td>
                  <td><input type="text" style={{width: '100%'}} value={this.state.newTodoDescription}
                    id="inputTodoNewDescription"
                    onChange={(e) => this.handleNewTodo(e)} 
                    onKeyPress={(e) => this.handleNewTodoKey(e)}
                    placeholder="Add new todo"/></td>
                  <td></td>
                </tr>
            {
              this.state.todos.map((t, index) => (
                <tr key={t.id} id={'rowTodo' + index}>
                  <td style={{width: '1em'}}><input type="checkbox" id={'checkboxTodo' + index} checked={t.done} onChange={(e) => this.handleChangeDone(t, index, e)}/></td>
                  <td>{t.description}</td>
                  <td style={{width: '1em'}}><Button onClick={(e) => this.handleDelete(t, index, e)}><FontAwesomeIcon icon="trash"/></Button></td>
                </tr>
              ))
            }
            </tbody>
            </Table>
          </div>
          <div class="col-sm col-md">
            &nbsp;
          </div>
        </div>
      </div>
    );
  }
}
