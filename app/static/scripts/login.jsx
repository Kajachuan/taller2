import React from 'react';
import ReactDOM from 'react-dom';

class LoginForm extends React.Component {
  constructor(props) {
    super(props);
    this.state = {name: ''};

    this.handleChange = this.handleChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  handleChange(event) {
    this.setState({name: event.target.value});
  }

  handleSubmit(event) {
    if (this.state.name == '') {
      alert('El nombre no puede estar en blanco');
      event.preventDefault();
    }
  }

  render() {
    return(
      <div class="login-form">
        <img src="/static/img/logo.png" height="280px"/>
        <h1>Back Office</h1>
        <form onSubmit={this.handleSubmit} method="post" action="/admin/">
          <div>
            <input type="text" name="name" value={this.state.name}
              onChange={this.handleChange} required onInvalid={this.handleInvalid}/>
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
