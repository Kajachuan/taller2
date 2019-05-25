import React from 'react';
import ReactDOM from 'react-dom';

class MenuButton extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      name: props.name,
      img: props.img,
      method: props.method,
      url: props.url
    };
  }

  render() {
    return(
      <div class="menu-button">
        <form action={this.state.url}>
          <button formMethod={this.state.method}>
            <img src={this.state.img} height="50" width="50" />
            <br/>
            {this.state.name}
          </button>
        </form>
      </div>
    );
  }
}

class Menu extends React.Component {
  render() {
    return(
      <div class="menu">
        <h1>Hypechat Back Office</h1>
        <MenuButton name="Administración de Equipos"
                    img="/static/img/organization.png"
                    // Faltan method y url
                  />
        <MenuButton name="Administración de Usuarios"
                    img="/static/img/user.png"
                    // Faltan method y url
                  />
        <MenuButton name="Administración de Palabras Prohibidas"
                    img="/static/img/words.png"
                    method="get"
                    url="/admin/forbidden-words/" />
        <MenuButton name="Estadísticas"
                    img="/static/img/statistics.png"
                    // Faltan method y url
                  />
        <MenuButton name="Cerrar Sesión"
                    img="/static/img/logout.png"
                     />
      </div>
    );
  }
}

ReactDOM.render(
  <Menu/>,
  document.getElementById('content')
);
