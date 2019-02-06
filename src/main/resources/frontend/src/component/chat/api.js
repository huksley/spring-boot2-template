//const WebSocketApi = {};

let mockUsers = [];
let mockListen = undefined;

const MockApi = {
  open: () => {
    console.log("open");
  },
  close: userName => {
    console.log("close", userName);
  },
  newUser: userName => {
    mockUsers.push(userName);
    console.log("newUser", userName);
  },
  listenMessage: handler => (mockListen = handler),
  send: message => {
    console.log("send", message);
    if (mockListen !== undefined) mockListen(message);
  },
  getUsers: handler => {
    if (handler !== undefined) {
      console.log("getUsers", mockUsers);
      handler(mockUsers);
    }
  }
};

export const api = MockApi;
