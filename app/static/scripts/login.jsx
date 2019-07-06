import React from 'react';
import ReactDOM from 'react-dom';

class LoginForm extends React.Component {
  render() {
    return(
      <div class="login-form">
        <img src="/static/img/logo.png" height="280px"/>
        <center><h1>Back Office</h1></center>
        <form method="post" action="/admin/">
          <div>
            <input type="text" name="name" required />
            <label>Nombre</label>
          </div>
          <div>
            <input type="password" name="password" required/>
            <label>Contraseña</label>
          </div>
          <input type="submit" value="INICIAR SESIÓN"/>
        </form>
      </div>
    );
  }
}

ReactDOM.render(
  <LoginForm/>,
  document.getElementById('content')
);
