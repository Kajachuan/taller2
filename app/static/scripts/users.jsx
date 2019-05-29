import React from 'react';
import ReactDOM from 'react-dom';
import Menu from './menu'

class Users extends React.Component {
  render() {
    return(
      <div class="users">
        <Menu/>
      </div>
    );
  }
}

ReactDOM.render(
  <Users/>,
  document.getElementById('content')
);
