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
      <form onSubmit={this.handleSubmit} method="post" action="http://127.0.0.1:8000/admin/">
        <label>
          Nombre:
          <input type="text" name="name" value={this.state.name} onChange={this.handleChange}/>
        </label>
        <br/>
        <label>
          Contraseña:
          <input type="password" name="password"/>
        </label>
        <br/>
        <input type="submit" value="Login"/>
      </form>
    );
  }
}

ReactDOM.render(
  <LoginForm/>,
  document.getElementById('form')
);
