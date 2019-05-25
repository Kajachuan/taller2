import React from 'react';
import ReactDOM from 'react-dom';
import Menu from './menu';
import { AreaChart, Area, CartesianGrid, XAxis, YAxis, Tooltip } from 'recharts';

class Statistics extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      count: 0,
      data: [],
      type: '',
      typeSelected: false,
      period: '',
      periodSelected: false,
      users: {},
      organizations: {},
      channels: {},
      messages: {}
    };

    this.changeToUsers = this.changeToUsers.bind(this);
    this.changeToOrganizations = this.changeToOrganizations.bind(this);
    this.changeToChannels = this.changeToChannels.bind(this);
    this.changeToMessages = this.changeToMessages.bind(this);
    this.changeToWeek = this.changeToWeek.bind(this);
    this.changeToMonth = this.changeToMonth.bind(this);
  }

  componentDidMount() {
    fetch("/statistics/users")
      .then(res => res.json())
      .then(
        (result) => {
          this.setState({
            users: result
          });
        }
      )
  }

  changeToUsers() {
    this.setState({
      count: this.state.users.count,
      typeSelected: true,
      type: 'usuarios'
    });
  }

  changeToOrganizations() {
    this.setState({
      count: this.state.organizations.count,
      typeSelected: true,
      type: 'organizaciones'
    });
  }

  changeToChannels() {
    this.setState({
      count: this.state.channels.count,
      typeSelected: true,
      type: 'canales'
    });
  }

  changeToMessages() {
    this.setState({
      count: this.state.messages.count,
      typeSelected: true,
      type: 'mensajes'
    });
  }

  changeToWeek() {
    if(this.state.type === 'usuarios')
      this.setState({data: this.state.users.week});

    else if(this.state.type === 'organizaciones')
      this.setState({data: this.state.organizations.week});

    else if(this.state.type === 'canales')
      this.setState({data: this.state.channels.week});

    else
      this.setState({data: this.state.messages.week});

    this.setState({period: 'la semana', periodSelected: true})
  }

  changeToMonth() {
    if(this.state.type === 'usuarios')
      this.setState({data: this.state.users.month});

    else if(this.state.type === 'organizaciones')
      this.setState({data: this.state.organizations.month});

    else if(this.state.type === 'canales')
      this.setState({data: this.state.channels.month});

    else
      this.setState({data: this.state.messages.month});

    this.setState({period: 'el mes', periodSelected: true})
  }

  render() {
    let count, title, graph;

    if(this.state.typeSelected) {
      count = <h2>La cantidad total de {this.state.type} es {this.state.count}</h2>
    }

    if(this.state.periodSelected) {
      title = <h3>Cantidad de {this.state.type} por dia en {this.state.period}</h3>
      graph = (<AreaChart width={600} height={300} data={this.state.data} margin={{ top: 5, right: 20, bottom: 5, left: 0 }}>
                 <Area name={this.state.type} type="monotone" dataKey="count" stroke="#303841" fill="#303841" />
                 <CartesianGrid strokeDasharray="5 5" />
                 <XAxis dataKey="_id" />
                 <YAxis />
                 <Tooltip />
               </AreaChart>)
    }

    return(
      <div class="statistics">
        <Menu/>
        <center>
          <button class="statistics-button" onClick={this.changeToUsers}>Usuarios</button>
          <button class="statistics-button" onClick={this.changeToOrganizations}>Organizaciones</button>
          <button class="statistics-button" onClick={this.changeToChannels}>Canales</button>
          <button class="statistics-button" onClick={this.changeToMessages}>Mensajes</button>
          <br/>
          <button class="period-button" onClick={this.changeToWeek}>Semana</button>
          <button class="period-button" onClick={this.changeToMonth}>Mes</button>
          {count}
          {title}
          {graph}
        </center>
      </div>
    );
  }
}

ReactDOM.render(
  <Statistics/>,
  document.getElementById('content')
);
