import React from 'react';
import ReactDOM from 'react-dom';
import Menu from './menu'

class Users extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      errorMessage: '',
      showForm: true,
      showProfile: false,
      username: '',
      firstName: '',
      lastName: ''
    };

    this.handleChange = this.handleChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  handleChange(event) {
    this.setState({username: event.target.value});
  }

  handleSubmit(event) {
    fetch("/profile/" + this.state.username)
      .then(res => res.json())
      .then(
        (result) => {
          this.setState({
            firstName: result.first_name,
            lastName: result.last_name,
            showForm: false,
            showProfile: true,
            errorMessage: ''
          });
        },
        (error) => {
          this.setState({
            errorMessage: 'El usuario no existe'
          });
        }
      )
    event.preventDefault();
  }

  render() {
    let form, profile;

    if(this.state.showForm) {
      form = (<form onSubmit={this.handleSubmit}>
                <label>
                  Nombre de usuario:
                  <input type="text" value={this.state.username} onChange={this.handleChange} />
                </label>
                <input type="submit" value="Buscar"/>
              </form>)
    }

    if(this.state.showProfile) {
      profile = (<div>
                   <h2>Nombre de usuario: {this.state.username}</h2>
                   <h2>Nombre: {this.state.firstName}</h2>
                   <h2>Apellido: {this.state.lastName}</h2>
                 </div>)
    }

    return(
      <div class="users">
        <Menu/>
        <center>
          <h1>Administración de Usuarios</h1>
          {form}
          {this.state.errorMessage}
          {profile}
        </center>
      </div>
    );
  }
}

ReactDOM.render(
  <Users/>,
  document.getElementById('content')
);
