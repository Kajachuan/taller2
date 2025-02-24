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
      lastName: '',
      email: '',
      image: '',
      banDate: '',
      banReason: '',
      showDate: false
    };

    this.handleChange = this.handleChange.bind(this);
    this.handleSubmit = this.handleSubmit.bind(this);
    this.searchAgain = this.searchAgain.bind(this);
    this.showBanDate = this.showBanDate.bind(this);
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
            email: result.email,
            image: result.image,
            banDate: result.ban_date,
            banReason: result.ban_reason,
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

  searchAgain() {
    this.setState({
      showForm: true,
      showProfile: false,
      showDate: false
    });
  }

  showBanDate() {
    this.setState({
      showDate: true
    });
  }

  render() {
    let form, profile, selectDate, dateField, reason;
    let today = new Date();
    let month = today.getMonth() + 1;
    let day = today.getDate();
    let todayString = [today.getFullYear(),
                       (month > 9 ? '' : '0') + month,
                       (day > 9 ? '' : '0') + day].join('-');
    let date = new Date(this.state.banDate);
    let dateMonth = date.getMonth() + 1;
    let dateDay = date.getDate();
    let dateString = [date.getFullYear(),
                      (dateMonth > 9 ? '' : '0') + dateMonth,
                      (dateDay > 9 ? '' : '0') + dateDay].join('-');

    if(this.state.showForm) {
      form = (<form class="user-search" onSubmit={this.handleSubmit}>
                <label>
                  Nombre de usuario:
                  <input type="text" value={this.state.username} onChange={this.handleChange} />
                </label>
                <input type="submit" value="Buscar"/>
              </form>)
    }

    if(this.state.banDate != null) {
      dateField = (<div><h2>Suspendido hasta: </h2><p>{dateString}</p></div>);
    }

    if(this.state.banReason != null) {
      reason = (<div><h2>Motivo de la suspensión: </h2><p>{this.state.banReason}</p></div>);
    }

    if(this.state.showProfile) {
      profile = (<div class="user-profile">
                   <img src={"data:image/png;base64," + this.state.image} height="150" />
                   <br/>
                   <h2>Nombre de usuario: </h2><p>{this.state.username}</p>
                   <br/>
                   <h2>Email: </h2><p>{this.state.email}</p>
                   <br/>
                   <h2>Nombre: </h2><p>{this.state.firstName}</p>
                   <br/>
                   <h2>Apellido: </h2><p>{this.state.lastName}</p>
                   <br/>
                   <br/>
                   {dateField}
                   {reason}
                   <button class="back-button" onClick={this.searchAgain}>Volver</button>
                   <button class="ban-button" onClick={this.showBanDate}>Suspender</button>
                 </div>)
    }

    if(this.state.showDate) {
      selectDate = (<form class="ban-form" method="post" action="/admin/ban/user">
                      <input type="hidden" name="username" value={this.state.username}/>
                      <label>
                        Suspender hasta
                        <input type="date" name="ban_date" min={todayString} required/>
                      </label>
                      <br/>
                      <label>
                        Motivo: <textarea name="ban_reason" required/>
                      </label>
                      <br/>
                      <input class="ban-button" type="submit" value="Confirmar suspensión"/>
                    </form>)
    }

    return(
      <div class="users">
        <Menu/>
        <center>
          <h1>Administración de Usuarios</h1>
          {form}
          <p>{this.state.errorMessage}</p>
          {profile}
          <br/>
          {selectDate}
        </center>
      </div>
    );
  }
}

ReactDOM.render(
  <Users/>,
  document.getElementById('content')
);
