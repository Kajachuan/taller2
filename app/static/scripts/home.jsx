import React from 'react';
import ReactDOM from 'react-dom';
import Menu from './menu';

class Home extends React.Component {
  constructor(props) {
    super(props);
  }

  render() {
    return(
      <div>
        <Menu/>
        <center>
          <img src="/static/img/logo.png" height="280px"/>
        </center>
      </div>
    );
  }
}

ReactDOM.render(
  <Home/>,
  document.getElementById('content')
);
