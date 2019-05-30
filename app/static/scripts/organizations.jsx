import React from 'react';
import ReactDOM from 'react-dom';
import Menu from './menu'

class Organizations extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      errorMessage: '',
      showForm: true,
      showInfo: false,
      organizationName: '',
      owner: '',
      ubication: '',
      image: '',
      description: '',
      welcomeMessage: '',
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
    this.setState({organizationName: event.target.value});
  }

  handleSubmit(event) {
    fetch("/organization/" + this.state.organizationName)
      .then(res => res.json())
      .then(
        (result) => {
          this.setState({
            owner: result.owner,
            ubication: result.ubication,
            image: result.image,
            description: result.description,
            welcomeMessage: result.welcome_message,
            banDate: result.ban_date,
            banReason: result.ban_reason,
            showForm: false,
            showInfo: true,
            errorMessage: ''
          });
        },
        (error) => {
          this.setState({
            errorMessage: 'La organización no existe'
          });
        }
      )
    event.preventDefault();
  }

  searchAgain() {
    this.setState({
      showForm: true,
      showInfo: false,
      showDate: false
    });
  }

  showBanDate() {
    this.setState({
      showDate: true
    });
  }

  render() {
    let form, info, selectDate, dateField, reason;
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
      form = (<form class="organization-search" onSubmit={this.handleSubmit}>
                <label>
                  Nombre de organización:
                  <input type="text" value={this.state.organizationName} onChange={this.handleChange} />
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

    if(this.state.showInfo) {
      info = (<div class="organization-info">
                <img src={"data:image/png;base64," + this.state.image} height="150" />
                <br/>
                <h2>Nombre de la organización: </h2><p>{this.state.organizationName}</p>
                <br/>
                <h2>Creador: </h2><p>{this.state.owner}</p>
                <br/>
                <h2>Ubicación: </h2><p>{this.state.ubication}</p>
                <br/>
                <h2>Descripción: </h2><p>{this.state.description}</p>
                <br/>
                <h2>Mensaje de bienvenida: </h2><p>{this.state.welcomeMessage}</p>
                <br/>
                <br/>
                {dateField}
                {reason}
                <button class="back-button" onClick={this.searchAgain}>Volver</button>
                <button class="ban-button" onClick={this.showBanDate}>Suspender</button>
              </div>)
    }

    if(this.state.showDate) {
      selectDate = (<form class="ban-form" method="post" action="/admin/ban">
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
      <div class="organizations">
        <Menu/>
        <center>
          <h1>Administración de Organizaciones</h1>
          {form}
          <p>{this.state.errorMessage}</p>
          {info}
          <br/>
          {selectDate}
        </center>
      </div>
    );
  }
}

ReactDOM.render(
  <Organizations/>,
  document.getElementById('content')
);
